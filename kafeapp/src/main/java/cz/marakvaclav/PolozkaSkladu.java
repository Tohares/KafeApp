package cz.marakvaclav;

/**Reprezentuje surovinu na sklade */

public class PolozkaSkladu {
    private String nazev;
    private float aktualniMnozstvi;
    private String jednotka;
    
    public PolozkaSkladu() {
        nazev = "nezadano";
        aktualniMnozstvi = 0;
        jednotka = "bez jednotky";
    }

    public PolozkaSkladu(String nazev, float aktualniMnozstvi, String jednotka) {
        this.nazev = nazev;
        this.aktualniMnozstvi = aktualniMnozstvi;
        this.jednotka = jednotka;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public float getAktualniMnozstvi() {
        return aktualniMnozstvi;
    }

    public void setAktualniMnozstvi(float mnozstvi) {
        this.aktualniMnozstvi = mnozstvi;
    }

    public String getJednotka() {
        return jednotka;
    }

    public void setJednotka(String jednotka) {
        this.jednotka = jednotka;
    }
}