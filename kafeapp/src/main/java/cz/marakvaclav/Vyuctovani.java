package cz.marakvaclav;

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

public class Vyuctovani {
    List<PolozkaSkladu> spotrebovanePolozky = null;
    String login = null;
    LocalDate datumVystaveni = null;
    int pocetUctovanychKav = 1;
    BigDecimal celkovaCena = null;
    BigDecimal cenaJedneKavy = null;
    int pocetVypitychKav = 0;
    BigDecimal cenaZaVypiteKavy = null;
    boolean stavPlatby = false;
    LocalDate datumPlatby = null;

    public Vyuctovani() {}

    public Vyuctovani(List<PolozkaSkladu> spotrebovanePolozky, String login, LocalDate datumDate, int pocetUctovanychKav) {
        this.spotrebovanePolozky = spotrebovanePolozky;
        this.login = login;
        this.datumVystaveni = datumDate;
        this.pocetUctovanychKav = pocetUctovanychKav;
        stavPlatby = false;
        urciCelkovouCenu();
        cenaJedneKavy = celkovaCena.divide(BigDecimal.valueOf(pocetUctovanychKav), 2, RoundingMode.HALF_UP);
    }

    public Vyuctovani(Vyuctovani vyuctovani, String login, int pocetVypitychKav) {
        this.login = login;
        this.datumVystaveni = vyuctovani.getDatumVystaveni();
        this.pocetUctovanychKav = vyuctovani.getPocetUctovanychKav();
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
        if (admin.getCisloUctu() == null || admin.getCisloUctu().isEmpty()) {
        return "Chyba: Admin nemá nastavené číslo účtu!";
        }

        String castkaStr = cenaZaVypiteKavy.setScale(2, RoundingMode.HALF_UP).toPlainString();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Příjemce: ").append(admin.getLogin()).append("\n");
        sb.append("Částka: ").append(castkaStr).append(" CZK\n");
        sb.append("Číslo účtu: ").append(admin.getCisloUctu()).append("\n");
        sb.append("Zpráva pro příjemce: KAFE-").append(login).append("-").append(datumVystaveni).append("\n");
        
        return sb.toString();
    }

    public BufferedImage vytvorQRKodProPlatbu(Admin admin) {
        String messageQR = "SPD*1.0*ACC:" + admin.getCisloUctu() + 
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

    public int getPocetUctovanychKav() {
        return pocetUctovanychKav;
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
        sb.append(pocetUctovanychKav).append(";");
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
        pocetUctovanychKav = Integer.parseInt(line[2]);
        celkovaCena = new BigDecimal(line[3]);
        cenaJedneKavy = new BigDecimal(line[4]);
        pocetVypitychKav = Integer.parseInt(line[5]);
        cenaZaVypiteKavy = new BigDecimal(line[6]);
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


