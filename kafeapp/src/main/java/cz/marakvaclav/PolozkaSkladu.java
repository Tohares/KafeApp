package cz.marakvaclav;

import java.math.BigDecimal;

public class PolozkaSkladu {
    private String nazev;
    private float koupeneMnozstvi;
    private float aktualniMnozstvi;
    private String jednotka;
    private BigDecimal cenaZaKus;
    private String menaPenezni;
        
    public PolozkaSkladu() {
        nazev = "nezadano";
        koupeneMnozstvi = 0;
        aktualniMnozstvi = 0;
        jednotka = "bez jednotky";
        cenaZaKus = BigDecimal.ZERO;
        menaPenezni = "CZK";
    }

    public PolozkaSkladu(String nazev, float koupeneMnozstvi, String jednotka, BigDecimal cenaZaKus, String menaPenezni) {
        this.nazev = nazev;
        this.koupeneMnozstvi = koupeneMnozstvi;
        this.aktualniMnozstvi = koupeneMnozstvi;
        this.jednotka = jednotka;
        this.cenaZaKus = cenaZaKus;
        this.menaPenezni = menaPenezni;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public float getKoupeneMnozstvi() {
        return koupeneMnozstvi;
    }

    public void setKoupeneMnozstvi(float mnozstvi) {
        this.koupeneMnozstvi = mnozstvi;
    }

    public void setAktualniMnozstvi(float mnozstvi) {
        this.aktualniMnozstvi = mnozstvi;
    }

    public float getAktualniMnozstvi() {
        return aktualniMnozstvi;
    }

    public void spotrebujMnozstvi(float mnozstvi) {
        aktualniMnozstvi -= mnozstvi;
    }

    public String getJednotka() {
        return jednotka;
    }

    public void setJednotka(String jednotka) {
        this.jednotka = jednotka;
    }

    public String toCsv() {
        return nazev + ";" + koupeneMnozstvi + ";" + jednotka;
    }

    public BigDecimal getCenaZaKus() {
        return cenaZaKus;
    }

    public void setCenaZaKus(BigDecimal cenaZaKus) {
        this.cenaZaKus = cenaZaKus;
    }

    public String getMenaPenezni() {
        return menaPenezni;
    }

    public void setMenaPenezni(String menaPenezni) {
        this.menaPenezni = menaPenezni;
    }
}