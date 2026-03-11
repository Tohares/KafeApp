package cz.marakvaclav;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class SpravceSouboru {
    private static final String SOUBOR_DAT_KAFARI = "kafari.csv";
    private static final String SOUBOR_LOCK_KAFARI = "kafe.lock";
    private static final String SOUBOR_TMP_KAFARI = "kafari.csv.tmp";
    private static final String SOUBOR_DAT_SKLAD = "sklad.csv";
    private static final String SOUBOR_LOCK_SKLAD = "sklad.lock";
    private static final String SOUBOR_TMP_SKLAD = "sklad.csv.tmp";

    public static List<String> chybyIntegrity = new ArrayList<>();

    public static void ulozKafare(Kafar kafar, String prihlasenyUzivatel) {
        if (!ziskejZamekDatKafari(prihlasenyUzivatel)) {
            System.err.println("Soubor kafari.csv je blokovan jinym uzivatelem. Zkuste to pozdeji.");
            return;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = Paths.get(SOUBOR_DAT_KAFARI);
            
            if (Files.exists(cestaKDatum)) {
                radky = Files.readAllLines(cestaKDatum);
            }

            boolean nalezen = false;
            String novyRadek = kafar.toCsv();

            for (int i = 0; i < radky.size(); i++) {
                if (radky.get(i).startsWith(kafar.getLogin() + ";")) {
                    radky.set(i, novyRadek);
                    nalezen = true;
                    break;
                }
            }
            if (!nalezen) {
                radky.add(novyRadek);
            }

            Path cestaTMP = Paths.get(SOUBOR_TMP_KAFARI);
            Files.write(cestaTMP, radky);
            
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSouborDatKafari(cestaKDatum);

        } catch (IOException e) {
            System.err.println("Chyba pri zapisu do souboru kafari.csv: " + e.getMessage());
        } finally {
            uvolniZamekDatKafari();
        }
    }

    public static List<Kafar> nactiKafare() {
        if (!ziskejZamekDatKafari("appStart")) {
            System.err.println("Soubor kafari.csv je blokovan jinym uzivatelem. Zkuste to pozdeji.");
            return null;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = Paths.get(SOUBOR_DAT_KAFARI);
            Path cestaKPodpisu = Paths.get(SOUBOR_DAT_KAFARI + ".sig");

            if (Files.exists(cestaKDatum) && Files.exists(cestaKPodpisu)) {
                String aktualniPodpis = Uzivatel.checkSum(new String(Files.readAllBytes(cestaKDatum)));
                String ulozenyPodpis = Files.readString(cestaKPodpisu);

                if (!aktualniPodpis.equals(ulozenyPodpis)) {
                    chybyIntegrity.add("VAROVANI: Celistvost souboru kafari.csv byla narusena!");
                }
            }
            
            if (Files.exists(cestaKDatum)) {
                radky = Files.readAllLines(cestaKDatum);
            }

            List<Kafar> kafari = new ArrayList<>();

            for (int i = 0; i < radky.size(); i++) {
                String[] KafarLine = radky.get(i).split(";");
                if (KafarLine.length == 4) {
                    String login = KafarLine[0];
                    String hesloHash = KafarLine[1];
                    int pocetVypitychKav = Integer.parseInt(KafarLine[2]);
                    String checkSum = KafarLine[3];

                    if (Uzivatel.checkSum(login + ";" + hesloHash + ";" + pocetVypitychKav).equals(checkSum)) {
                        Kafar kafar = new Kafar(login, "");
                        kafar.setHesloHash(hesloHash);
                        kafar.setPocetVypitychKav(pocetVypitychKav);
                        kafari.add(kafar);
                    }
                    else {
                        chybyIntegrity.add("Nekdo manipuloval s daty u kafare " + login);
                    }
                }
            }
            return kafari;
        } catch (IOException e) {
            System.err.println("Chyba pri cteni ze souboru kafari.csv: " + e.getMessage());
        } finally {
            uvolniZamekDatKafari();
        }
        return null;
    }

    public static void ulozPolozkuNaSklad(PolozkaSkladu polozka, String prihlasenyUzivatel) {
        if (!ziskejZamekDatSklad(prihlasenyUzivatel)) {
            System.err.println("Soubor sklad.csv je blokovan jinym uzivatelem. Zkuste to pozdeji.");
            return;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = Paths.get(SOUBOR_DAT_SKLAD);
            
            if (Files.exists(cestaKDatum)) {
                radky = Files.readAllLines(cestaKDatum);
            }

            boolean nalezen = false;
            String novyRadek = polozka.toCsv();

            for (int i = 0; i < radky.size(); i++) {
                if (radky.get(i).startsWith(polozka.getId() + ";")) {
                    radky.set(i, novyRadek);
                    nalezen = true;
                    break;
                }
            }
            if (!nalezen) {
                radky.add(novyRadek);
            }

            Path cestaTMP = Paths.get(SOUBOR_TMP_SKLAD);
            Files.write(cestaTMP, radky);
            
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSouborDatSklad(cestaKDatum);
        } catch (IOException e) {
            System.err.println("Chyba pri zapisu do souboru sklad.csv: " + e.getMessage());
        } finally {
            uvolniZamekDatSklad();
        }
    }

    public static List<PolozkaSkladu> nactiSklad() {
        if (!ziskejZamekDatSklad("appStart")) {
            System.err.println("Soubor sklad.csv je blokovan jinym uzivatelem. Zkuste to pozdeji.");
            return null;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = Paths.get(SOUBOR_DAT_SKLAD);
            Path cestaKPodpisu = Paths.get(SOUBOR_DAT_SKLAD + ".sig");

            if (Files.exists(cestaKDatum) && Files.exists(cestaKPodpisu)) {
                String aktualniPodpis = Uzivatel.checkSum(new String(Files.readAllBytes(cestaKDatum)));
                String ulozenyPodpis = Files.readString(cestaKPodpisu);

                if (!aktualniPodpis.equals(ulozenyPodpis)) {
                    chybyIntegrity.add("VAROVANI: Celistvost souboru sklad.csv byla narusena!");
                }
            }

            if(Files.exists(cestaKDatum)) {
                radky = Files.readAllLines(cestaKDatum);
            }

            List<PolozkaSkladu> sklad = new ArrayList<>();

            for (int i = 0; i < radky.size(); i++) {
                String[] polozkaLine = radky.get(i).split(";");
                if (polozkaLine.length == 7) {
                    int id = Integer.parseInt(polozkaLine[0]);
                    String nazev = polozkaLine[1];
                    float koupeneMnozstvi = Float.parseFloat(polozkaLine[2]);
                    float aktualniMnozstvi = Float.parseFloat(polozkaLine[3]);
                    String jednotka = polozkaLine[4];
                    BigDecimal cenaZaKus = new BigDecimal(polozkaLine[5]);
                    String menaPenezni = polozkaLine[6];
                    PolozkaSkladu polozka = new PolozkaSkladu(id, nazev, koupeneMnozstvi, aktualniMnozstvi, jednotka, cenaZaKus, menaPenezni);
                    sklad.add(polozka);
                }
            }
            return sklad;
        } catch (IOException e) {
            System.err.println("Chyba pri cteni ze souboru sklad.csv: " + e.getMessage());
        } finally {
            uvolniZamekDatSklad();
        }
        return null;
    }

    private static boolean ziskejZamekDatKafari(String prihlasenyUzivatel) {
        int pokusy = 0;
        while (pokusy < 10) {
            try {
                Path lockPath = Paths.get(SOUBOR_LOCK_KAFARI);
                if (Files.exists(lockPath)) {
                    List<String> radky = new ArrayList<>();
                    radky = Files.readAllLines(lockPath);
                    String[] time = radky.get(0).split("@");
                    if (Long.parseLong(time[2].trim()) < System.currentTimeMillis() - 10000) {
                        Files.deleteIfExists(lockPath);
                    }
                }
                if (!Files.exists(lockPath)) {
                    Files.createFile(lockPath);
                    String lockOwnerInfo = System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName() + 
                        " user: " + prihlasenyUzivatel + " @" + System.currentTimeMillis();
                    Files.writeString(lockPath, lockOwnerInfo);
                    return true;
                }
            } catch (IOException e) {
            }
            pokusy++;
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        return false;
    }

    private static void uvolniZamekDatKafari() {
        try {
            Files.deleteIfExists(Paths.get(SOUBOR_LOCK_KAFARI));
        } catch (IOException e) {
            System.err.println("Nepodařilo se smazat kafari.lock soubor!");
        }
    }

    private static void podepisSouborDatKafari(Path cesta) throws IOException {
        byte[] dataSouboru = Files.readAllBytes(cesta);
        String hashSouboru = Uzivatel.checkSum(new String(dataSouboru)); 
        Files.writeString(Paths.get(SOUBOR_DAT_KAFARI + ".sig"), hashSouboru);
    }

    private static boolean ziskejZamekDatSklad(String prihlasenyUzivatel) {
        int pokusy = 0;
        while (pokusy < 10) {
            try {
                Path lockPath = Paths.get(SOUBOR_LOCK_SKLAD);
                if (Files.exists(lockPath)) {
                    List<String> radky = new ArrayList<>();
                    radky = Files.readAllLines(lockPath);
                    String[] time = radky.get(0).split("@");
                    if (Long.parseLong(time[2].trim()) < System.currentTimeMillis() - 10000) {
                        Files.deleteIfExists(lockPath);
                    }
                }
                if (!Files.exists(lockPath)) {
                    Files.createFile(lockPath);
                    String lockOwnerInfo = System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName() + 
                        " user: " + prihlasenyUzivatel + " @" + System.currentTimeMillis();
                    Files.writeString(lockPath, lockOwnerInfo);
                    return true;
                }
            } catch (IOException e) {}
            pokusy++;
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        return false;
    }

    private static void uvolniZamekDatSklad() {
        try {
            Files.deleteIfExists(Paths.get(SOUBOR_LOCK_SKLAD));
        } catch (IOException e) {
            System.err.println("Nepodařilo se smazat sklad.lock soubor!");
        }
    }

    private static void podepisSouborDatSklad(Path cesta) throws IOException {
        byte[] dataSouboru = Files.readAllBytes(cesta);
        String hashSouboru = Uzivatel.checkSum(new String(dataSouboru)); 
        Files.writeString(Paths.get(SOUBOR_DAT_SKLAD + ".sig"), hashSouboru);
    }

}