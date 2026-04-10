package cz.marakvaclav.entity;

import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Entita reprezentující záznam o vyúčtování. Může nabývat dvou významů:
 * 1. Globální (hlavní) vyúčtování: Patří adminovi, obsahuje seznam spotřebovaných surovin a celkovou útratu za dané období.
 * 2. Osobní vyúčtování: Patří konkrétnímu kafaři, sdílí datum hlavního vyúčtování a udává dlužnou částku uživatele.
 */
public class Vyuctovani {
    List<PolozkaSkladu> spotrebovanePolozky = null;
    String login = null;
    LocalDate datumVystaveni = null;
    int pocetUctovanychKavCelkem = 1;
    BigDecimal celkovaCena = null;
    BigDecimal cenaJedneKavy = null;
    int pocetVypitychKav = 0;
    BigDecimal cenaZaVypiteKavy = null;
    boolean stavPlatby = false;
    LocalDate datumPlatby = null;

    public Vyuctovani() {}

    public Vyuctovani(List<PolozkaSkladu> spotrebovanePolozky, String login, LocalDate datumDate, int pocetUctovanychKavCelkem) {
        this.spotrebovanePolozky = spotrebovanePolozky;
        this.login = login;
        this.datumVystaveni = datumDate;
        this.pocetUctovanychKavCelkem = pocetUctovanychKavCelkem;
        this.pocetVypitychKav = pocetUctovanychKavCelkem;
        stavPlatby = false;
        urciCelkovouCenu();
        cenaZaVypiteKavy = celkovaCena;
        cenaJedneKavy = celkovaCena.divide(BigDecimal.valueOf(pocetUctovanychKavCelkem), 2, RoundingMode.HALF_UP);
    }

    public Vyuctovani(Vyuctovani vyuctovani, String login, int pocetVypitychKav) {
        this.login = login;
        this.datumVystaveni = vyuctovani.getDatumVystaveni();
        this.pocetUctovanychKavCelkem = vyuctovani.getPocetUctovanychKavCelkem();
        this.spotrebovanePolozky = vyuctovani.getSpotrebovanePolozky();
        this.celkovaCena = vyuctovani.getCelkovaCena();
        this.cenaJedneKavy = vyuctovani.getCenaJedneKavy();
        this.pocetVypitychKav = pocetVypitychKav;
        this.cenaZaVypiteKavy = this.cenaJedneKavy.multiply(BigDecimal.valueOf(this.pocetVypitychKav));
        this.stavPlatby = false;
    }

    private void urciCelkovouCenu() {
        celkovaCena = BigDecimal.ZERO;
        for (PolozkaSkladu polozka : spotrebovanePolozky) {
            celkovaCena = celkovaCena.add(polozka.getCenaZaKus().multiply(BigDecimal.valueOf(polozka.getAktualniMnozstvi())));
        }
    }

    public String vypisPlatebniUdaje(Admin admin) {
        if (admin.getCisloUctuIBAN() == null || admin.getCisloUctuIBAN().isEmpty()) {
        return "Chyba: Admin nemá nastavené číslo účtu!";
        }

        String castkaStr = cenaZaVypiteKavy.setScale(2, RoundingMode.HALF_UP).toPlainString();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Příjemce: ").append(admin.getLogin()).append("\n");
        sb.append("Částka: ").append(castkaStr).append(" CZK\n");
        sb.append("Číslo účtu: ").append(admin.getCisloUctuCZ()).append("\n");
        sb.append("Zpráva pro příjemce: KAFE-").append(login).append("-").append(datumVystaveni).append("\n");
        
        return sb.toString();
    }

    // Vygeneruje QR kód pro mobilní bankovnictví na základě standardu SPAYD (Short Payment Descriptor)
    public BufferedImage vytvorQRKodProPlatbu(Admin admin) {
        // Sestavení platebního řetězce podle českého bankovního standardu SPAYD (Short Payment Descriptor)
        String messageQR = "SPD*1.0*ACC:" + admin.getCisloUctuIBAN() + 
                           "*AM:" + cenaZaVypiteKavy.setScale(2, RoundingMode.HALF_UP).toPlainString() + 
                           "*CC:CZK*MSG:KAFE-" + login + 
                           "-" + datumVystaveni + 
                           "*END";
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(messageQR, BarcodeFormat.QR_CODE, 250, 250);
            
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            System.err.println("Chyba při generování QR kódu: " + e.getMessage());
            return null;
        }
    } 

    public List<PolozkaSkladu> getSpotrebovanePolozky() {
        return spotrebovanePolozky;
    }

    public String getLogin() {
        return login;
    }

    public LocalDate getDatumVystaveni() {
        return datumVystaveni;
    }

    public int getPocetUctovanychKavCelkem() {
        return pocetUctovanychKavCelkem;
    }

    public BigDecimal getCelkovaCena() {
        return celkovaCena;
    }

    public BigDecimal getCenaJedneKavy() {
        return cenaJedneKavy;
    }

    public int getPocetVypitychKav() {
        return pocetVypitychKav;
    }

    public BigDecimal getCenaZaVypiteKavy() {
        return cenaZaVypiteKavy;
    }

    public boolean getStavPlatby() {
        return stavPlatby;
    }

    public LocalDate getDatumPlatby() {
        return datumPlatby;
    }

    public void setStavPlatby(boolean stavPlatby) {
        this.stavPlatby = stavPlatby;
    }

    public void setDatumPlatby(LocalDate datumPlatby) {
        this.datumPlatby = datumPlatby;
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(login).append(";");
        sb.append(datumVystaveni).append(";");
        sb.append(pocetUctovanychKavCelkem).append(";");
        sb.append(celkovaCena).append(";");
        sb.append(cenaJedneKavy).append(";");
        sb.append(pocetVypitychKav).append(";");
        sb.append(cenaZaVypiteKavy).append(";");
        sb.append(stavPlatby).append(";");
        sb.append(datumPlatby).append(";");

        for (PolozkaSkladu polozka : spotrebovanePolozky) {
            sb.append(polozka.toCsv()).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public void fromCsv(String[] line) {
        login = line[0];
        datumVystaveni = LocalDate.parse(line[1]);
        pocetUctovanychKavCelkem = Integer.parseInt(line[2]);
        if (line[3] != null && !line[3].equals("null") && !line[3].isEmpty()) {
            celkovaCena = new BigDecimal(line[3]);
        } else {
            celkovaCena = null;
        }
        if (line[4] != null && !line[4].equals("null") && !line[4].isEmpty()) {
            cenaJedneKavy = new BigDecimal(line[4]);
        } else {
            cenaJedneKavy = null;
        }
        pocetVypitychKav = Integer.parseInt(line[5]);
        if (line[6] != null && !line[6].equals("null") && !line[6].isEmpty()) {
            cenaZaVypiteKavy = new BigDecimal(line[6]);
        } else {
            cenaZaVypiteKavy = null;
        }
        stavPlatby = Boolean.parseBoolean(line[7]);
        if (line[8] != null && !line[8].equals("null") && !line[8].isEmpty()) {
            datumPlatby = LocalDate.parse(line[8]);
        } else {
            datumPlatby = null;
        }

        spotrebovanePolozky = new ArrayList<>();
        for (int i = 9; i < line.length; i += 7) {
            PolozkaSkladu polozka = new PolozkaSkladu(Integer.parseInt(line[i]), line[i+1], Integer.parseInt(line[i+2]), 
                Integer.parseInt(line[i+3]), line[i+4], new BigDecimal(line[i+5]), line[i+6]);
            spotrebovanePolozky.add(polozka);
        }
    }

}
