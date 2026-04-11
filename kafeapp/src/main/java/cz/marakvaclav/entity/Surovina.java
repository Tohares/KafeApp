package cz.marakvaclav.entity;

/**
 * Výčtový typ reprezentující povolené druhy surovin v systému.
 * Zapouzdřuje v sobě zobrazovaný název a logiku pro povolené měrné jednotky.
 */
public enum Surovina {
    KAFE("Kafe", new String[]{"kg", "0.5kg"}, false),
    MLEKO("Mleko", new String[]{"l"}, false),
    CUKR("Cukr", new String[]{"kg"}, false),
    KYS_CITRONOVA("Kys. Citr.", new String[]{}, true);

    private final String nazev;
    private final String[] vychoziJednotky;
    private final boolean volitelnaJednotka;

    Surovina(String nazev, String[] vychoziJednotky, boolean volitelnaJednotka) {
        this.nazev = nazev;
        this.vychoziJednotky = vychoziJednotky;
        this.volitelnaJednotka = volitelnaJednotka;
    }

    public String getNazev() { return nazev; }
    public String[] getVychoziJednotky() { return vychoziJednotky; }
    public boolean isVolitelnaJednotka() { return volitelnaJednotka; }

    @Override
    public String toString() {
        return nazev; // Tímto zajistíme, že se v JComboBoxu i nadále zobrazí hezký textový název
    }

    public static Surovina fromString(String text) {
        for (Surovina s : Surovina.values()) {
            if (s.nazev.equalsIgnoreCase(text)) return s;
        }
        return null;
    }
}