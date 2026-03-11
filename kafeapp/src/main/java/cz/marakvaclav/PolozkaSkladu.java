package cz.marakvaclav;

import java.math.BigDecimal;

public class PolozkaSkladu {
    private static int count = 0;
    private int id;
    private String nazev;
    private float koupeneMnozstvi;
    private float aktualniMnozstvi;
    private String jednotka;
    private BigDecimal cenaZaKus;
    private String menaPenezni;
    
    public PolozkaSkladu() {
        id = count;
        count++;
        nazev = "nezadano";
        koupeneMnozstvi = 0;
        aktualniMnozstvi = 0;
        jednotka = "nezadano";
        cenaZaKus = BigDecimal.ZERO;
        menaPenezni = "CZK";
    }

    public PolozkaSkladu(String nazev, float koupeneMnozstvi, String jednotka, BigDecimal cenaZaKus, String menaPenezni) {
        id = count;
        count++;
        this.nazev = nazev;
        this.koupeneMnozstvi = koupeneMnozstvi;
        this.aktualniMnozstvi = koupeneMnozstvi;
        this.jednotka = jednotka;
        this.cenaZaKus = cenaZaKus;
        this.menaPenezni = menaPenezni;
    }

    public PolozkaSkladu(int id, String nazev, float koupeneMnozstvi, float aktualniMnozstvi, String jednotka, BigDecimal cenaZaKus, String menaPenezni) {
        this.id = id;
        if (id > count) {
            count = id + 1;
        }
        this.nazev = nazev;
        this.koupeneMnozstvi = koupeneMnozstvi;
        this.aktualniMnozstvi = aktualniMnozstvi;
        this.jednotka = jednotka;
        this.cenaZaKus = cenaZaKus;
        this.menaPenezni = menaPenezni;
    }

    public int getId() {
        return id;
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
        return id + ";" + nazev + ";" + koupeneMnozstvi + ";" + aktualniMnozstvi + ";" +jednotka + ";" + cenaZaKus + ";" + menaPenezni;
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