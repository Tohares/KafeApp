package cz.marakvaclav.sluzby;

import cz.marakvaclav.dialogy.*;
import cz.marakvaclav.entity.*;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SpravceSouboru {
    private static final String SOUBOR_DAT_KAFARI = "kafari.csv";
    private static final String SOUBOR_LOCK_KAFARI = "kafari.lock";
    private static final String SOUBOR_TMP_KAFARI = "kafari.csv.tmp";
    private static final String SOUBOR_DAT_SKLAD = "sklad.csv";
    private static final String SOUBOR_LOCK_SKLAD = "sklad.lock";
    private static final String SOUBOR_TMP_SKLAD = "sklad.csv.tmp";
    private static final String SOUBOR_DAT_VYUCTOVANI = "vyuctovani.csv";
    private static final String SOUBOR_LOCK_VYUCTOVANI = "vyuctovani.lock";
    private static final String SOUBOR_TMP_VYUCTOVANI = "vyuctovani.csv.tmp";
    private static final String SOUBOR_DAT_LOG = "kafeapp.log";
    private static final String SOUBOR_LOCK_LOG = "kafeapp.lock";
    private static final String SOUBOR_KONFIGURACE = "kafeapp.properties";

    public static List<String> chybyIntegrity = new ArrayList<>();

    private static String pracovniSlozka = "";

    private static String cachedHostName = "unknown";
    // Cachování názvu PC. Volání getHostName() může na některých sítích trvat sekundy a zablokovat EDT (grafické) vlákno
    static {
        try {
            cachedHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}
    }

    public static void setPracovniSlozka(String slozka) {
        pracovniSlozka = slozka;
        ulozKonfiguraci();
    }

    public static String getPracovniSlozka() {
        return pracovniSlozka;
    }

    public static void nactiKonfiguraci() {
        File configFile = new File(SOUBOR_KONFIGURACE);
        if (configFile.exists()) {
            try (java.io.FileReader fr = new java.io.FileReader(configFile)) {
                java.util.Properties props = new java.util.Properties();
                props.load(fr);
                pracovniSlozka = props.getProperty("pracovniSlozka", "");
            } catch (Exception e) {
                System.err.println("Nelze načíst konfiguraci: " + e.getMessage());
            }
        }
    }

    public static void ulozKonfiguraci() {
        try (java.io.FileWriter fw = new java.io.FileWriter(SOUBOR_KONFIGURACE)) {
            java.util.Properties props = new java.util.Properties();
            props.setProperty("pracovniSlozka", pracovniSlozka == null ? "" : pracovniSlozka);
            props.store(fw, "Nastaveni KafeApp (pokud je pracovniSlozka prazdna, uklada se k programu)");
        } catch (Exception e) {
            System.err.println("Nelze uložit konfiguraci: " + e.getMessage());
        }
    }

    private static Path getCesta(String soubor) {
        if (pracovniSlozka == null || pracovniSlozka.isEmpty()) {
            return Paths.get(soubor);
        }
        return Paths.get(pracovniSlozka, soubor);
    }

    public static void ulozVyuctovani(Vyuctovani vyuctovani, String prihlasenyUzivatel) {
        String identifikatorUctenky = vyuctovani.getLogin() + ";" + 
                                      vyuctovani.getDatumVystaveni() + ";" + 
                                      vyuctovani.getPocetUctovanychKavCelkem() + ";" + 
                                      vyuctovani.getCelkovaCena() + ";" + 
                                      vyuctovani.getCenaJedneKavy() + ";" + 
                                      vyuctovani.getPocetVypitychKav() + ";" + 
                                      vyuctovani.getCenaZaVypiteKavy() + ";";
        aktualizujNeboPridejRadek(SOUBOR_DAT_VYUCTOVANI, SOUBOR_TMP_VYUCTOVANI, SOUBOR_LOCK_VYUCTOVANI, prihlasenyUzivatel, identifikatorUctenky, vyuctovani.toCsv());
    }

    public static void prepisVsechnaVyuctovani(List<Vyuctovani> vyuctovaniList, String prihlasenyUzivatel) {
        if (!ziskejZamek(SOUBOR_LOCK_VYUCTOVANI, prihlasenyUzivatel)) {
            System.err.println("Soubor " + SOUBOR_DAT_VYUCTOVANI + " je blokován.");
            return;
        }
        try {
            List<String> radky = new ArrayList<>();
            for (Vyuctovani v : vyuctovaniList) {
                radky.add(v.toCsv());
            }
            Path cestaTMP = getCesta(SOUBOR_TMP_VYUCTOVANI);
            Files.write(cestaTMP, radky);
            Path cestaKDatum = getCesta(SOUBOR_DAT_VYUCTOVANI);
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum, SOUBOR_DAT_VYUCTOVANI + ".sig");
        } catch (IOException e) {
            System.err.println("Chyba při přepisu vyúčtování: " + e.getMessage());
        } finally {
            uvolniZamek(SOUBOR_LOCK_VYUCTOVANI);
        }
    }

    public static List<Vyuctovani> nactiVyuctovani() {
        List<String> radky = nactiRadkyZeSouboru(SOUBOR_DAT_VYUCTOVANI, SOUBOR_LOCK_VYUCTOVANI, "appStart");
        if (radky == null) return null;

        List<Vyuctovani> seznamVyuctovani = new ArrayList<>();
        for (String radek : radky) {
            String[] vyuctovaniLine = radek.split(";");
            if (vyuctovaniLine.length > 10) {
                try {
                    Vyuctovani vyuctovani = new Vyuctovani();
                    vyuctovani.fromCsv(vyuctovaniLine);
                    seznamVyuctovani.add(vyuctovani);
                } catch (Exception e) {
                    zaznamenejChybuIntegrity("Poškozený řádek vyúčtování (přeskočeno): " + radek);
                }
            }
        }
        return seznamVyuctovani;
    }
    
    public static void ulozKafare(Kafar kafar, String prihlasenyUzivatel) {
        aktualizujNeboPridejRadek(SOUBOR_DAT_KAFARI, SOUBOR_TMP_KAFARI, SOUBOR_LOCK_KAFARI, prihlasenyUzivatel, kafar.getLogin() + ";", kafar.toCsv());
    }

    public static List<Kafar> nactiKafare() {
        List<String> radky = nactiRadkyZeSouboru(SOUBOR_DAT_KAFARI, SOUBOR_LOCK_KAFARI, "appStart");
        if (radky == null) return null;

        List<Kafar> kafari = new ArrayList<>();
        for (String radek : radky) {
            String[] KafarLine = radek.split(";");
            if (KafarLine.length >= 3) {
                try {
                    // Pokud jde 3. sloupec převést na číslo, je to Kafař (má tam počet káv)
                    int pocetVypitychKav = Integer.parseInt(KafarLine[2]);
                    if (pocetVypitychKav < 0) throw new IllegalArgumentException("Záporný počet káv");
                    String login = KafarLine[0];
                    String hesloHash = KafarLine[1];
                    Kafar kafar = new Kafar(login, "");
                    kafar.setHesloHash(hesloHash);
                    kafar.setPocetVypitychKav(pocetVypitychKav);
                    kafari.add(kafar);
                } catch (NumberFormatException e) {
                    // Není to číslo (je to IBAN), takže to je Administrátor. Přeskočí se.
                } catch (Exception e) {
                    zaznamenejChybuIntegrity("Poškozená data kafaře (přeskočeno): " + radek);
                }
            }
        }
        return kafari;
    }

    public static void ulozAdmina(Admin admin, String prihlasenyUzivatel) {
        aktualizujNeboPridejRadek(SOUBOR_DAT_KAFARI, SOUBOR_TMP_KAFARI, SOUBOR_LOCK_KAFARI, prihlasenyUzivatel, admin.getLogin() + ";", admin.toCsv());
    }

    public static Admin nactiAdmina() {
        List<String> radky = nactiRadkyZeSouboru(SOUBOR_DAT_KAFARI, SOUBOR_LOCK_KAFARI, "appStart");
        if (radky == null) return null;

        for (String radek : radky) {
            String[] parts = radek.split(";");
            if (parts.length >= 4) {
                try {
                    Integer.parseInt(parts[2]); // Pokud je to Kafař, tento parse projde a my ho přeskočíme
                } catch (NumberFormatException e) {
                // Pokud to spadlo, jedná se o účet administrátora!
                    try {
                        String login = parts[0];
                        String hash = parts[1];
                        String iban = parts[2];
                        String cz = parts[3];
                        Admin admin = new Admin(login, "");
                        admin.setHesloHash(hash);
                        admin.setCisloUctuIBAN(iban);
                        admin.setCisloUctuCZ(cz);
                        return admin;
                    } catch (Exception ex) {
                        zaznamenejChybuIntegrity("Poškozená data administrátora: " + radek);
                    }
                }
            }
        }
        return null;
    }

    public static void prepisVsechnyUzivatele(List<Kafar> kafari, Admin admin, String prihlasenyUzivatel) {
        if (!ziskejZamek(SOUBOR_LOCK_KAFARI, prihlasenyUzivatel)) {
            System.err.println("Soubor " + SOUBOR_DAT_KAFARI + " je blokován.");
            return;
        }
        try {
            List<String> radky = new ArrayList<>();
            if (admin != null) {
                radky.add(admin.toCsv());
            }
            for (Kafar k : kafari) {
                radky.add(k.toCsv());
            }
            Path cestaTMP = getCesta(SOUBOR_TMP_KAFARI);
            Files.write(cestaTMP, radky);
            Path cestaKDatum = getCesta(SOUBOR_DAT_KAFARI);
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum, SOUBOR_DAT_KAFARI + ".sig");
        } catch (IOException e) {
            System.err.println("Chyba při přepisu kafařů: " + e.getMessage());
        } finally {
            uvolniZamek(SOUBOR_LOCK_KAFARI);
        }
    }

    public static void ulozPolozkuNaSklad(PolozkaSkladu polozka, String prihlasenyUzivatel) {
        aktualizujNeboPridejRadek(SOUBOR_DAT_SKLAD, SOUBOR_TMP_SKLAD, SOUBOR_LOCK_SKLAD, prihlasenyUzivatel, polozka.getId() + ";", polozka.toCsv());
    }

    public static void prepisCelySklad(List<PolozkaSkladu> skladList, String prihlasenyUzivatel) {
        if (!ziskejZamek(SOUBOR_LOCK_SKLAD, prihlasenyUzivatel)) {
            System.err.println("Soubor " + SOUBOR_DAT_SKLAD + " je blokován.");
            return;
        }
        try {
            List<String> radky = new ArrayList<>();
            for (PolozkaSkladu p : skladList) {
                radky.add(p.toCsv());
            }
            Path cestaTMP = getCesta(SOUBOR_TMP_SKLAD);
            Files.write(cestaTMP, radky);
            Path cestaKDatum = getCesta(SOUBOR_DAT_SKLAD);
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum, SOUBOR_DAT_SKLAD + ".sig");
        } catch (IOException e) {
            System.err.println("Chyba při přepisu skladu: " + e.getMessage());
        } finally {
            uvolniZamek(SOUBOR_LOCK_SKLAD);
        }
    }

    public static List<PolozkaSkladu> nactiSklad() {
        List<String> radky = nactiRadkyZeSouboru(SOUBOR_DAT_SKLAD, SOUBOR_LOCK_SKLAD, "appStart");
        if (radky == null) return null;

        List<PolozkaSkladu> sklad = new ArrayList<>();
        for (String radek : radky) {
            String[] polozkaLine = radek.split(";");
            if (polozkaLine.length >= 7) {
                try {
                    int id = Integer.parseInt(polozkaLine[0]);
                    String nazev = polozkaLine[1];
                    int koupeneMnozstvi = Integer.parseInt(polozkaLine[2]);
                    int aktualniMnozstvi = Integer.parseInt(polozkaLine[3]);
                    String jednotka = polozkaLine[4];
                    BigDecimal cenaZaKus = new BigDecimal(polozkaLine[5]);
                    String menaPenezni = polozkaLine[6];
                    
                    if (koupeneMnozstvi < 0 || aktualniMnozstvi < 0 || cenaZaKus.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Záporné hodnoty");
                    }
                    
                    PolozkaSkladu polozka = new PolozkaSkladu(id, nazev, koupeneMnozstvi, aktualniMnozstvi, jednotka, cenaZaKus, menaPenezni);
                    sklad.add(polozka);
                } catch (Exception e) {
                    zaznamenejChybuIntegrity("Poškozený řádek skladu (přeskočeno): " + radek);
                }
            }
        }
        return sklad;
    }

    // Metoda pro přidání nebo úpravu existujícího řádku (tzv. Upsert) využívající bezpečný atomický zápis přes dočasný soubor
    private static void aktualizujNeboPridejRadek(String nazevSouboru, String nazevTmp, String nazevLock, String prihlasenyUzivatel, String prefixKHledani, String novyRadek) {
        if (!ziskejZamek(nazevLock, prihlasenyUzivatel)) {
            System.err.println("Soubor " + nazevSouboru + " je blokován jiným uživatelem. Zkuste to později.");
            return;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = getCesta(nazevSouboru);
            
            if (Files.exists(cestaKDatum)) {
                radky = Files.readAllLines(cestaKDatum);
            }

            boolean nalezen = false;
            for (int i = 0; i < radky.size(); i++) {
                if (radky.get(i).startsWith(prefixKHledani)) {
                    radky.set(i, novyRadek);
                    nalezen = true;
                    break;
                }
            }
            if (!nalezen) {
                radky.add(novyRadek);
            }

            Path cestaTMP = getCesta(nazevTmp);
            Files.write(cestaTMP, radky);
            
            // Atomický přesun .tmp na místo originálu garantuje, že nedojde k poškození dat, pokud by systém během ukládání spadl
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum, nazevSouboru + ".sig");

        } catch (IOException e) {
            System.err.println("Chyba při zápisu do souboru " + nazevSouboru + ": " + e.getMessage());
        } finally {
            uvolniZamek(nazevLock);
        }
    }

    private static List<String> nactiRadkyZeSouboru(String nazevSouboru, String nazevLock, String prihlasenyUzivatel) {
        if (!ziskejZamek(nazevLock, prihlasenyUzivatel)) {
            System.err.println("Soubor " + nazevSouboru + " je blokován jiným uživatelem. Zkuste to později.");
            return null;
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = getCesta(nazevSouboru);
            Path cestaKPodpisu = getCesta(nazevSouboru + ".sig");

            zkontrolujIntegritu(cestaKDatum, cestaKPodpisu, nazevSouboru);
            
            if (Files.exists(cestaKDatum)) {
                radky = Files.readAllLines(cestaKDatum);
            }
            return radky;
        } catch (IOException e) {
            System.err.println("Chyba při čtení ze souboru " + nazevSouboru + ": " + e.getMessage());
            return null;
        } finally {
            uvolniZamek(nazevLock);
        }
    }

    private static boolean zkusZiskatZamek(String nazevZamekSouboru, String prihlasenyUzivatel) {
        try {
            Path lockPath = getCesta(nazevZamekSouboru);
            if (Files.exists(lockPath)) {
                List<String> radky = Files.readAllLines(lockPath);
                if (!radky.isEmpty()) {
                    // Pokud starý zámek existuje déle než 10 vteřin, považuje se za "mrtvý" (vzniklý např. nečekaným pádem aplikace) a je bezpečně smazán
                    String[] time = radky.get(0).split("@");
                    if (time.length >= 3 && Long.parseLong(time[2].trim()) < System.currentTimeMillis() - 10000) {
                        Files.deleteIfExists(lockPath);
                    }
                }
            }
            if (!Files.exists(lockPath)) {
                Files.createFile(lockPath);
                String lockOwnerInfo = System.getProperty("user.name") + "@" + cachedHostName + 
                    " user: " + prihlasenyUzivatel + " @" + System.currentTimeMillis();
                Files.writeString(lockPath, lockOwnerInfo);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Nepodařilo se získat zámek " + nazevZamekSouboru + ": " + e.getMessage());
        } // Používá se Exception pro zachycení i chyby číselného formátu u Long.parseLong
        return false;
    }

    private static boolean ziskejZamek(String nazevZamekSouboru, String prihlasenyUzivatel) {
        // První rychlý pokus (většinou projde hned bez zobrazení oken)
        if (zkusZiskatZamek(nazevZamekSouboru, prihlasenyUzivatel)) {
            return true;
        }

        // Pokud to nevyšlo, zobrazí se dialog a předá se mu funkce k opakovanému zkoušení
        CekaniNaZamekDialog dialog = new CekaniNaZamekDialog(() -> zkusZiskatZamek(nazevZamekSouboru, prihlasenyUzivatel));
        dialog.setVisible(true); 
        
        return dialog.isUspech();
    }

    private static void uvolniZamek(String nazevZamekSouboru) {
        try {
            Files.deleteIfExists(getCesta(nazevZamekSouboru));
        } catch (IOException e) {
            System.err.println("Nepodařilo se smazat " + nazevZamekSouboru + " soubor!");
        }
    }

    private static void podepisSoubor(Path cesta, String nazevSigSouboru) throws IOException {
        byte[] dataSouboru = Files.readAllBytes(cesta);
        String hashSouboru = Uzivatel.checkSum(new String(dataSouboru)); 
        Files.writeString(getCesta(nazevSigSouboru), hashSouboru);
    }

    private static void zkontrolujIntegritu(Path cestaKDatum, Path cestaKPodpisu, String nazevSouboru) throws IOException {
        if (Files.exists(cestaKDatum)) {
            if (Files.exists(cestaKPodpisu)) {
                String aktualniPodpis = Uzivatel.checkSum(new String(Files.readAllBytes(cestaKDatum)));
                String ulozenyPodpis = Files.readString(cestaKPodpisu);
    
                if (!aktualniPodpis.equals(ulozenyPodpis)) {
                    zaznamenejChybuIntegrity("VAROVÁNÍ: Celistvost souboru " + nazevSouboru + " byla narušena (nesouhlasí podpis)!");
                }
            } else {
                zaznamenejChybuIntegrity("VAROVÁNÍ: Chybí podpisový soubor pro " + nazevSouboru + "! Data mohla být upravena zvenčí.");
            }
        }
    }

    public static void zaznamenejChybuIntegrity(String chyba) {
        chybyIntegrity.add(chyba);
        
        if (!ziskejZamek(SOUBOR_LOCK_LOG, "appSystem")) {
            System.err.println("Soubor logu je blokován jiným procesem. Nelze zapsat událost.");
            return;
        }

        try {
            String cas = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logZaznam = cas + " - " + chyba + System.lineSeparator();
            
            Path cestaKLogu = getCesta(SOUBOR_DAT_LOG);
            Files.writeString(cestaKLogu, logZaznam, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            podepisSoubor(cestaKLogu, SOUBOR_DAT_LOG + ".sig");
        } catch (IOException e) {
            System.err.println("Nepodařilo se zapsat do logu: " + e.getMessage());
        } finally {
            uvolniZamek(SOUBOR_LOCK_LOG);
        }
    }

    public static void smazVsechnaData(String prihlasenyUzivatel) {
        if (ziskejZamek(SOUBOR_LOCK_KAFARI, prihlasenyUzivatel)) {
            try { Files.deleteIfExists(getCesta(SOUBOR_DAT_KAFARI)); Files.deleteIfExists(getCesta(SOUBOR_DAT_KAFARI + ".sig")); } 
            catch (IOException e) { System.err.println("Nelze smazat kafari.csv"); }
            uvolniZamek(SOUBOR_LOCK_KAFARI);
        }
        if (ziskejZamek(SOUBOR_LOCK_SKLAD, prihlasenyUzivatel)) {
            try { Files.deleteIfExists(getCesta(SOUBOR_DAT_SKLAD)); Files.deleteIfExists(getCesta(SOUBOR_DAT_SKLAD + ".sig")); } 
            catch (IOException e) { System.err.println("Nelze smazat sklad.csv"); }
            uvolniZamek(SOUBOR_LOCK_SKLAD);
        }
        if (ziskejZamek(SOUBOR_LOCK_VYUCTOVANI, prihlasenyUzivatel)) {
            try { Files.deleteIfExists(getCesta(SOUBOR_DAT_VYUCTOVANI)); Files.deleteIfExists(getCesta(SOUBOR_DAT_VYUCTOVANI + ".sig")); } 
            catch (IOException e) { System.err.println("Nelze smazat vyuctovani.csv"); }
            uvolniZamek(SOUBOR_LOCK_VYUCTOVANI);
        }
    }

}