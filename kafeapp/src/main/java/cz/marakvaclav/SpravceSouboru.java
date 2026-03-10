package cz.marakvaclav;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class SpravceSouboru {
    private static final String SOUBOR_DAT = "kafari.csv";
    private static final String SOUBOR_LOCK = "kafe.lock";
    private static final String SOUBOR_TMP = "kafari.csv.tmp";
    public static List<String> chybyIntegrity = new ArrayList<>();

    public static void ulozKafare(Kafar kafar, String prihlasenyUzivatel) {
        if (!ziskejZamek(prihlasenyUzivatel)) {
            System.err.println("Soubor je blokovan jinym uzivatelem. Zkuste to pozdeji.");
            return;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = Paths.get(SOUBOR_DAT);
            
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

            Path cestaTMP = Paths.get(SOUBOR_TMP);
            Files.write(cestaTMP, radky);
            
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum);

        } catch (IOException e) {
            System.err.println("Chyba pri zapisu do souboru: " + e.getMessage());
        } finally {
            uvolniZamek();
        }
    }

    public static List<Kafar> nactiKafare() {
        if (!ziskejZamek("appStart")) {
            System.err.println("Soubor je blokovan jinym uzivatelem. Zkuste to pozdeji.");
            return null;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = Paths.get(SOUBOR_DAT);
            Path cestaKPodpisu = Paths.get(SOUBOR_DAT + ".sig");

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
            System.err.println("Chyba pri cteni ze souboru: " + e.getMessage());
        } finally {
            uvolniZamek();
        }
        return null;
    }

    private static boolean ziskejZamek(String prihlasenyUzivatel) {
        int pokusy = 0;
        while (pokusy < 10) {
            try {
                Path lockPath = Paths.get(SOUBOR_LOCK);
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
                        " kafar: " + prihlasenyUzivatel + " @" + System.currentTimeMillis();
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

    private static void uvolniZamek() {
        try {
            Files.deleteIfExists(Paths.get(SOUBOR_LOCK));
        } catch (IOException e) {
            System.err.println("Nepodařilo se smazat lock soubor!");
        }
    }

    private static void podepisSoubor(Path cesta) throws IOException {
        byte[] dataSouboru = Files.readAllBytes(cesta);
        String hashSouboru = Uzivatel.checkSum(new String(dataSouboru)); 
        Files.writeString(Paths.get(SOUBOR_DAT + ".sig"), hashSouboru);
    }
}