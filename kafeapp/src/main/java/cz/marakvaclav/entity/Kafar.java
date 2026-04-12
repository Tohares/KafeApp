package cz.marakvaclav.entity;

/**
 * Entita reprezentující běžného uživatele (kafaře).
 * Udržuje si stav svého aktuálního "dluhu" v podobě počtu vypitých a dosud nevyúčtovaných káv.
 */
public class Kafar extends Uzivatel {
    private int pocetVypitychKav;
    private boolean vyzadujeZmenuHesla;
    private int zruseneKavy;
    private boolean aktivni;

    public Kafar(String login, char[] heslo) {
        super(login, heslo);
        this.pocetVypitychKav = 0;
        this.vyzadujeZmenuHesla = false;
        this.zruseneKavy = 0;
        this.aktivni = true;
    }

    public int getPocetVypitychKav() {
        return pocetVypitychKav;
    }

    public void setPocetVypitychKav(int pocetVypitychKav) {
        this.pocetVypitychKav = pocetVypitychKav;
    }

    public boolean isVyzadujeZmenuHesla() {
        return vyzadujeZmenuHesla;
    }

    public void setVyzadujeZmenuHesla(boolean vyzadujeZmenuHesla) {
        this.vyzadujeZmenuHesla = vyzadujeZmenuHesla;
    }

    public void vypijKavu() {
        this.pocetVypitychKav++;
    }

    public void odeberKavu() {
        if (this.pocetVypitychKav > 0) {
            this.pocetVypitychKav--;
            this.zruseneKavy++;
        }
    }

    public int getZruseneKavy() {
        return zruseneKavy;
    }

    public void setZruseneKavy(int zruseneKavy) {
        this.zruseneKavy = zruseneKavy;
    }

    public boolean isAktivni() {
        return aktivni;
    }

    public void setAktivni(boolean aktivni) {
        this.aktivni = aktivni;
    }

    public String toCsv() {
        return login + ";" + hesloHash + ";" + pocetVypitychKav + ";" + vyzadujeZmenuHesla + ";" + zruseneKavy + ";" + aktivni;
    }

    public void zaplatit() {
        this.pocetVypitychKav = 0;
    }
}
