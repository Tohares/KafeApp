package cz.marakvaclav.entity;

import java.math.BigDecimal;

/**
 * Entita reprezentující konkrétní naskladněnou položku nebo její střípek (např. spotřebovanou část balení).
 * Udržuje informaci o tom, kolik suroviny z původního nákupu bylo pořízeno a kolik jí ještě zbývá k dispozici.
 */
public class PolozkaSkladu {
    private static int count = 0;
    private int id;
    private Surovina surovina;
    private int koupeneMnozstvi;
    private int aktualniMnozstvi;
    private String jednotka;
    private BigDecimal cenaZaKus;
    private String menaPenezni;
    
    public PolozkaSkladu() {
        id = count;
        count++;
        surovina = Surovina.KAFE;
        koupeneMnozstvi = 0;
        aktualniMnozstvi = 0;
        jednotka = "nezadano";
        cenaZaKus = BigDecimal.ZERO;
        menaPenezni = "CZK";
    }

    public PolozkaSkladu(Surovina surovina, int koupeneMnozstvi, String jednotka, BigDecimal cenaZaKus, String menaPenezni) {
        id = count;
        count++;
        this.surovina = surovina;
        this.koupeneMnozstvi = koupeneMnozstvi;
        this.aktualniMnozstvi = koupeneMnozstvi;
        this.jednotka = jednotka;
        this.cenaZaKus = cenaZaKus;
        this.menaPenezni = menaPenezni;
    }

    public PolozkaSkladu(int id, Surovina surovina, int koupeneMnozstvi, int aktualniMnozstvi, String jednotka, BigDecimal cenaZaKus, String menaPenezni) {
        this.id = id;
        if (id > count) {
            count = id + 1;
        }
        this.surovina = surovina;
        this.koupeneMnozstvi = koupeneMnozstvi;
        this.aktualniMnozstvi = aktualniMnozstvi;
        this.jednotka = jednotka;
        this.cenaZaKus = cenaZaKus;
        this.menaPenezni = menaPenezni;
    }

    public int getId() {
        return id;
    }

    public Surovina getSurovina() {
        return surovina;
    }

    public void setSurovina(Surovina surovina) {
        this.surovina = surovina;
    }

    public int getKoupeneMnozstvi() {
        return koupeneMnozstvi;
    }

    public void setKoupeneMnozstvi(int mnozstvi) {
        this.koupeneMnozstvi = mnozstvi;
    }

    public void setAktualniMnozstvi(int mnozstvi) {
        this.aktualniMnozstvi = mnozstvi;
    }

    public int getAktualniMnozstvi() {
        return aktualniMnozstvi;
    }

    public void spotrebujMnozstvi(int mnozstvi) {
        aktualniMnozstvi -= mnozstvi;
    }

    public String getJednotka() {
        return jednotka;
    }

    public void setJednotka(String jednotka) {
        this.jednotka = jednotka;
    }

    public String toCsv() {
        return id + ";" + surovina.getNazev() + ";" + koupeneMnozstvi + ";" + aktualniMnozstvi + ";" + jednotka + ";" + cenaZaKus + ";" + menaPenezni;
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