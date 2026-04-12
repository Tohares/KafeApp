package cz.marakvaclav.entity;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entita reprezentující záznam o vyúčtování. Může nabývat dvou významů:
 * 1. Globální (hlavní) vyúčtování: Patří adminovi, obsahuje seznam spotřebovaných surovin a celkovou útratu za dané období.
 * 2. Osobní vyúčtování: Patří konkrétnímu kafaři, sdílí datum hlavního vyúčtování a udává dlužnou částku uživatele.
 */
public class Vyuctovani {
    private List<PolozkaSkladu> spotrebovanePolozky = null;
    private String login = null;
    private LocalDate datumVystaveni = null;
    private int pocetUctovanychKavCelkem = 1;
    private BigDecimal celkovaCena = null;
    private BigDecimal cenaJedneKavy = null;
    private int pocetVypitychKav = 0;
    private BigDecimal cenaZaVypiteKavy = null;
    private boolean stavPlatby = false;
    private LocalDate datumPlatby = null;
    private String platebniUcetIBAN = null;
    private String platebniUcetCZ = null;
    private boolean oznamenoJakoZaplacene = false;

    public Vyuctovani() {}

    public Vyuctovani(List<PolozkaSkladu> spotrebovanePolozky, String login, LocalDate datumDate, int pocetUctovanychKavCelkem) {
        this.spotrebovanePolozky = spotrebovanePolozky;
        this.login = login;
        this.datumVystaveni = datumDate;
        this.pocetUctovanychKavCelkem = pocetUctovanychKavCelkem;
        this.pocetVypitychKav = pocetUctovanychKavCelkem;
        stavPlatby = false;
        oznamenoJakoZaplacene = false;
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
        this.oznamenoJakoZaplacene = false;
    }

    private void urciCelkovouCenu() {
        celkovaCena = BigDecimal.ZERO;
        for (PolozkaSkladu polozka : spotrebovanePolozky) {
            celkovaCena = celkovaCena.add(polozka.getCenaZaKus().multiply(BigDecimal.valueOf(polozka.getAktualniMnozstvi())));
        }
    }

    public String vypisPlatebniUdaje(Admin admin) {
        String iban = (platebniUcetIBAN != null && !platebniUcetIBAN.isEmpty()) ? platebniUcetIBAN : admin.getCisloUctuIBAN();
        String cz = (platebniUcetCZ != null && !platebniUcetCZ.isEmpty()) ? platebniUcetCZ : admin.getCisloUctuCZ();

        if (iban == null || iban.isEmpty()) {
            return "Chyba: Nebylo nalezeno číslo účtu příjemce!";
        }

        String castkaStr = cenaZaVypiteKavy.setScale(2, RoundingMode.HALF_UP).toPlainString();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Příjemce: ").append(admin.getLogin()).append("\n");
        sb.append("Částka: ").append(castkaStr).append(" CZK\n");
        sb.append("Číslo účtu (IBAN): ").append(iban).append("\n");
        if (cz != null && !cz.isEmpty()) {
            sb.append("Číslo účtu (CZ): ").append(cz).append("\n");
        }
        sb.append("Zpráva pro příjemce: KAFE-").append(login).append("-").append(datumVystaveni).append("\n");
        
        return sb.toString();
    }

    // Vygeneruje platební řetězec podle standardu SPAYD (Short Payment Descriptor)
    public String generujSpaydRetezec(Admin admin) {
        String iban = (platebniUcetIBAN != null && !platebniUcetIBAN.isEmpty()) ? platebniUcetIBAN : admin.getCisloUctuIBAN();
        if (iban == null || iban.isEmpty()) return null;

        return "SPD*1.0*ACC:" + iban + 
                           "*AM:" + cenaZaVypiteKavy.setScale(2, RoundingMode.HALF_UP).toPlainString() + 
                           "*CC:CZK*MSG:KAFE-" + login + 
                           "-" + datumVystaveni + 
                           "*END";
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

    public String getPlatebniUcetIBAN() { return platebniUcetIBAN; }
    public void setPlatebniUcetIBAN(String platebniUcetIBAN) { this.platebniUcetIBAN = platebniUcetIBAN; }
    
    public String getPlatebniUcetCZ() { return platebniUcetCZ; }
    public void setPlatebniUcetCZ(String platebniUcetCZ) { this.platebniUcetCZ = platebniUcetCZ; }

    public boolean isOznamenoJakoZaplacene() { return oznamenoJakoZaplacene; }
    public void setOznamenoJakoZaplacene(boolean oznameno) { this.oznamenoJakoZaplacene = oznameno; }

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
        sb.append(platebniUcetIBAN).append(";");
        sb.append(platebniUcetCZ).append(";");
        sb.append(oznamenoJakoZaplacene).append(";");

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
        
        platebniUcetIBAN = line[9].equals("null") ? null : line[9];
        platebniUcetCZ = line[10].equals("null") ? null : line[10];

        oznamenoJakoZaplacene = Boolean.parseBoolean(line[11]);

        spotrebovanePolozky = new ArrayList<>();
        for (int i = 12; i < line.length; i += 7) {
            Surovina surovina = Surovina.fromString(line[i+1]);
            if (surovina == null) throw new IllegalArgumentException("Neznámý druh suroviny: " + line[i+1]);
            
            PolozkaSkladu polozka = new PolozkaSkladu(Integer.parseInt(line[i]), surovina, Integer.parseInt(line[i+2]), 
                Integer.parseInt(line[i+3]), line[i+4], new BigDecimal(line[i+5]), line[i+6]);
            spotrebovanePolozky.add(polozka);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vyuctovani that = (Vyuctovani) o;
        
        return Objects.equals(login, that.login) &&
               Objects.equals(datumVystaveni, that.datumVystaveni) &&
               (cenaZaVypiteKavy == null ? that.cenaZaVypiteKavy == null : that.cenaZaVypiteKavy != null && cenaZaVypiteKavy.compareTo(that.cenaZaVypiteKavy) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, datumVystaveni);
    }
}
