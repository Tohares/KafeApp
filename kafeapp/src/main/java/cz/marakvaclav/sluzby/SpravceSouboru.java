package cz.marakvaclav.sluzby;

import cz.marakvaclav.dialogy.*;
import cz.marakvaclav.entity.*;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;

// Třída zajišťující veškeré operace se soubory (CSV), distribuované zámky (lock) a kontrolu integrity dat (podpisy).
public class SpravceSouboru {
    public static final String HEADER_KAFARI = "===KAFARI===";
    public static final String HEADER_SKLAD = "===SKLAD===";
    public static final String HEADER_VYUCTOVANI = "===VYUCTOVANI===";
    public static final String HEADER_SIGNATURE = "===SIGNATURE===";

    private String souborDatKafari = "kafari.csv";
    private String souborLockKafari = "kafari.lock";
    private String souborTmpKafari = "kafari.csv.tmp";
    
    private String souborDatSklad = "sklad.csv";
    private String souborLockSklad = "sklad.lock";
    private String souborTmpSklad = "sklad.csv.tmp";
    
    private String souborDatVyuctovani = "vyuctovani.csv";
    private String souborLockVyuctovani = "vyuctovani.lock";
    private String souborTmpVyuctovani = "vyuctovani.csv.tmp";
    
    private String souborDatLog = "kafeapp.log";
    private String souborLockLog = "kafeapp.lock";
    
    private String souborKonfigurace;

    private List<String> chybyIntegrity = new ArrayList<>();

    private String pracovniSlozka = "";

    private static String cachedHostName = "unknown";
    // Cachování názvu PC. Volání getHostName() může na některých sítích trvat sekundy a zablokovat EDT (grafické) vlákno
    static {
        try {
            cachedHostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}
    }

    public List<String> getChybyIntegrity() {
        return java.util.Collections.unmodifiableList(chybyIntegrity);
    }

    public void vymazChybyIntegrity() {
        chybyIntegrity.clear();
    }

    // Vlastní výjimka pro rozeznání nedostupného/odpojeného disku od jiných logických chyb
    public static class DatabaseUnavailableException extends RuntimeException {
        public DatabaseUnavailableException(String message) {
            super(message);
        }
    }

    public SpravceSouboru() {
        this("kafeapp.properties");
    }

    public SpravceSouboru(String souborKonfigurace) {
        this.souborKonfigurace = souborKonfigurace;
    }

    public String getSouborKonfigurace() {
        return souborKonfigurace;
    }

    public void setPracovniSlozka(String slozka) {
        pracovniSlozka = slozka;
        ulozKonfiguraci();
    }

    public String getPracovniSlozka() {
        return pracovniSlozka;
    }

    public void nactiKonfiguraci() {
        File configFile = new File(souborKonfigurace);
        if (configFile.exists()) {
            try (Reader fr = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
                Properties props = new Properties();
                props.load(fr);
                pracovniSlozka = props.getProperty("pracovniSlozka", "");
                
                souborDatKafari = props.getProperty("souborKafari", "kafari.csv");
                souborLockKafari = souborDatKafari + ".lock";
                souborTmpKafari = souborDatKafari + ".tmp";
                
                souborDatSklad = props.getProperty("souborSklad", "sklad.csv");
                souborLockSklad = souborDatSklad + ".lock";
                souborTmpSklad = souborDatSklad + ".tmp";
                
                souborDatVyuctovani = props.getProperty("souborVyuctovani", "vyuctovani.csv");
                souborLockVyuctovani = souborDatVyuctovani + ".lock";
                souborTmpVyuctovani = souborDatVyuctovani + ".tmp";
                
                souborDatLog = props.getProperty("souborLog", "kafeapp.log");
                souborLockLog = souborDatLog + ".lock";
            } catch (IOException e) {
                System.err.println("Nelze načíst konfiguraci: " + e.getMessage());
            }
        }
    }

    public void ulozKonfiguraci() {
        try (Writer fw = Files.newBufferedWriter(Paths.get(souborKonfigurace), StandardCharsets.UTF_8)) {
            Properties props = new Properties();
            props.setProperty("pracovniSlozka", pracovniSlozka == null ? "" : pracovniSlozka);
            props.setProperty("souborKafari", souborDatKafari);
            props.setProperty("souborSklad", souborDatSklad);
            props.setProperty("souborVyuctovani", souborDatVyuctovani);
            props.setProperty("souborLog", souborDatLog);
            props.store(fw, "Nastaveni KafeApp (pokud je pracovniSlozka prazdna, uklada se k programu)");
        } catch (IOException e) {
            System.err.println("Nelze uložit konfiguraci: " + e.getMessage());
        }
    }

    private Path getCesta(String soubor) {
        if (pracovniSlozka == null || pracovniSlozka.isEmpty()) {
            return Paths.get(soubor);
        }
        return Paths.get(pracovniSlozka, soubor);
    }

    // Uloží nebo zaktualizuje záznam o vyúčtování do souboru pomocí bezpečného atomického zápisu
    public void ulozVyuctovani(Vyuctovani vyuctovani, String prihlasenyUzivatel) {
        String identifikatorUctenky = vyuctovani.getLogin() + ";" + 
                                      vyuctovani.getDatumVystaveni() + ";" + 
                                      vyuctovani.getPocetUctovanychKavCelkem() + ";" + 
                                      vyuctovani.getCelkovaCena() + ";" + 
                                      vyuctovani.getCenaJedneKavy() + ";" + 
                                      vyuctovani.getPocetVypitychKav() + ";" + 
                                      vyuctovani.getCenaZaVypiteKavy() + ";";
        aktualizujNeboPridejRadek(souborDatVyuctovani, souborTmpVyuctovani, souborLockVyuctovani, prihlasenyUzivatel, identifikatorUctenky, vyuctovani.toCsv());
    }

    public void prepisVsechnaVyuctovani(List<Vyuctovani> vyuctovaniList, String prihlasenyUzivatel) {
        if (!ziskejZamek(souborLockVyuctovani, prihlasenyUzivatel)) {
            throw new DatabaseUnavailableException("Soubor " + souborDatVyuctovani + " je dlouhodobě blokován.");
        }
        try {
            List<String> radky = new ArrayList<>();
            for (Vyuctovani v : vyuctovaniList) {
                radky.add(v.toCsv());
            }
            Path cestaTMP = getCesta(souborTmpVyuctovani);
            Files.write(cestaTMP, radky);
            Path cestaKDatum = getCesta(souborDatVyuctovani);
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum, souborDatVyuctovani + ".sig");
        } catch (IOException e) {
            throw new DatabaseUnavailableException("Chyba při přepisu vyúčtování: " + e.getMessage());
        } finally {
            uvolniZamek(souborLockVyuctovani);
        }
    }

    public List<Vyuctovani> nactiVyuctovani() {
        List<String> radky = nactiRadkyZeSouboru(souborDatVyuctovani, souborLockVyuctovani, "appStart");
        if (radky == null) return null;

        List<Vyuctovani> seznamVyuctovani = new ArrayList<>();
        for (String radek : radky) {
            String[] vyuctovaniLine = radek.split(";");
            if (vyuctovaniLine.length >= 12) {
                try {
                    Vyuctovani vyuctovani = new Vyuctovani();
                    vyuctovani.fromCsv(vyuctovaniLine);
                    seznamVyuctovani.add(vyuctovani);
                } catch (DateTimeParseException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    zaznamenejChybuIntegrity("Poškozený řádek vyúčtování (přeskočeno): " + radek);
                }
            }
        }
        return seznamVyuctovani;
    }
    
    public void ulozKafare(Kafar kafar, String prihlasenyUzivatel) {
        aktualizujNeboPridejRadek(souborDatKafari, souborTmpKafari, souborLockKafari, prihlasenyUzivatel, kafar.getLogin() + ";", kafar.toCsv());
    }

    public List<Kafar> nactiKafare() {
        List<String> radky = nactiRadkyZeSouboru(souborDatKafari, souborLockKafari, "appStart");
        if (radky == null) return null;

        List<Kafar> kafari = new ArrayList<>();
        for (String radek : radky) {
            String[] KafarLine = radek.split(";");
            if (KafarLine.length >= 6) {
                try {
                    // Pokud jde 3. sloupec převést na číslo, je to Kafař (má tam počet káv)
                    int pocetVypitychKav = Integer.parseInt(KafarLine[2]);
                    if (pocetVypitychKav < 0) throw new IllegalArgumentException("Záporný počet káv");
                    String login = KafarLine[0];
                    String hesloHash = KafarLine[1];
                    Kafar kafar = new Kafar(login, new char[0]);
                    kafar.setHesloHash(hesloHash);
                    kafar.setPocetVypitychKav(pocetVypitychKav);
                    kafar.setVyzadujeZmenuHesla(Boolean.parseBoolean(KafarLine[3]));
                    kafar.setZruseneKavy(Integer.parseInt(KafarLine[4]));
                    kafar.setAktivni(Boolean.parseBoolean(KafarLine[5]));
                    kafari.add(kafar);
                } catch (NumberFormatException e) {
                    // Není to číslo (je to IBAN), takže to je Administrátor. Přeskočí se.
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    zaznamenejChybuIntegrity("Poškozená data kafaře (přeskočeno): " + radek);
                }
            }
        }
        return kafari;
    }

    public void ulozAdmina(Admin admin, String prihlasenyUzivatel) {
        aktualizujNeboPridejRadek(souborDatKafari, souborTmpKafari, souborLockKafari, prihlasenyUzivatel, admin.getLogin() + ";", admin.toCsv());
    }

    public Admin nactiAdmina() {
        // Explicitní kontrola dostupnosti cílové složky před jakýmkoliv čtením (odpojený disk atd.)
        Path cestaSlozky = getCesta("");
        if (pracovniSlozka != null && !pracovniSlozka.isEmpty() && !Files.exists(cestaSlozky)) {
            throw new DatabaseUnavailableException("Složka s databází není dostupná: " + pracovniSlozka);
        }

        List<String> radky = nactiRadkyZeSouboru(souborDatKafari, souborLockKafari, "appStart");
        if (radky == null || radky.isEmpty()) return null; // Null/prázdné nyní znamená pouze skutečně chybějící soubor

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
                        Admin admin = new Admin(login, new char[0]);
                        admin.setHesloHash(hash);
                        admin.setCisloUctuIBAN(iban);
                        admin.setCisloUctuCZ(cz);
                        return admin;
                    } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                        zaznamenejChybuIntegrity("Poškozená data administrátora: " + radek);
                    }
                }
            }
        }
        return null;
    }

    public void prepisVsechnyUzivatele(List<Kafar> kafari, Admin admin, String prihlasenyUzivatel) {
        if (!ziskejZamek(souborLockKafari, prihlasenyUzivatel)) {
            throw new DatabaseUnavailableException("Soubor " + souborDatKafari + " je dlouhodobě blokován.");
        }
        try {
            List<String> radky = new ArrayList<>();
            if (admin != null) {
                radky.add(admin.toCsv());
            }
            for (Kafar k : kafari) {
                radky.add(k.toCsv());
            }
            Path cestaTMP = getCesta(souborTmpKafari);
            Files.write(cestaTMP, radky);
            Path cestaKDatum = getCesta(souborDatKafari);
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum, souborDatKafari + ".sig");
        } catch (IOException e) {
            throw new DatabaseUnavailableException("Chyba při přepisu kafařů (disk odpojen?): " + e.getMessage());
        } finally {
            uvolniZamek(souborLockKafari);
        }
    }

    public void ulozPolozkuNaSklad(PolozkaSkladu polozka, String prihlasenyUzivatel) {
        aktualizujNeboPridejRadek(souborDatSklad, souborTmpSklad, souborLockSklad, prihlasenyUzivatel, polozka.getId() + ";", polozka.toCsv());
    }

    public void prepisCelySklad(List<PolozkaSkladu> skladList, String prihlasenyUzivatel) {
        if (!ziskejZamek(souborLockSklad, prihlasenyUzivatel)) {
            throw new DatabaseUnavailableException("Soubor " + souborDatSklad + " je dlouhodobě blokován.");
        }
        try {
            List<String> radky = new ArrayList<>();
            for (PolozkaSkladu p : skladList) {
                radky.add(p.toCsv());
            }
            Path cestaTMP = getCesta(souborTmpSklad);
            Files.write(cestaTMP, radky);
            Path cestaKDatum = getCesta(souborDatSklad);
            Files.move(cestaTMP, cestaKDatum, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            podepisSoubor(cestaKDatum, souborDatSklad + ".sig");
        } catch (IOException e) {
            throw new DatabaseUnavailableException("Chyba při přepisu skladu (disk odpojen?): " + e.getMessage());
        } finally {
            uvolniZamek(souborLockSklad);
        }
    }

    public List<PolozkaSkladu> nactiSklad() {
        List<String> radky = nactiRadkyZeSouboru(souborDatSklad, souborLockSklad, "appStart");
        if (radky == null) return null;

        List<PolozkaSkladu> sklad = new ArrayList<>();
        for (String radek : radky) {
            String[] polozkaLine = radek.split(";");
            if (polozkaLine.length >= 7) {
                try {
                    int id = Integer.parseInt(polozkaLine[0]);
                    Surovina surovina = Surovina.fromString(polozkaLine[1]);
                    if (surovina == null) {
                        throw new IllegalArgumentException("Neznámý druh suroviny: " + polozkaLine[1]);
                    }
                    
                    int koupeneMnozstvi = Integer.parseInt(polozkaLine[2]);
                    int aktualniMnozstvi = Integer.parseInt(polozkaLine[3]);
                    String jednotka = polozkaLine[4];
                    BigDecimal cenaZaKus = new BigDecimal(polozkaLine[5]);
                    String menaPenezni = polozkaLine[6];
                    
                    if (koupeneMnozstvi < 0 || aktualniMnozstvi < 0 || cenaZaKus.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Záporné hodnoty");
                    }
                    
                    PolozkaSkladu polozka = new PolozkaSkladu(id, surovina, koupeneMnozstvi, aktualniMnozstvi, jednotka, cenaZaKus, menaPenezni);
                    sklad.add(polozka);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    zaznamenejChybuIntegrity("Poškozený řádek skladu (přeskočeno): " + radek);
                }
            }
        }
        return sklad;
    }

    // Metoda pro přidání nebo úpravu existujícího řádku (tzv. Upsert) využívající bezpečný atomický zápis přes dočasný soubor
    private void aktualizujNeboPridejRadek(String nazevSouboru, String nazevTmp, String nazevLock, String prihlasenyUzivatel, String prefixKHledani, String novyRadek) {
        if (!ziskejZamek(nazevLock, prihlasenyUzivatel)) {
            throw new DatabaseUnavailableException("Soubor " + nazevSouboru + " je blokován jiným procesem.");
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
            throw new DatabaseUnavailableException("Chyba při zápisu do " + nazevSouboru + " (odpojený disk?): " + e.getMessage());
        } finally {
            uvolniZamek(nazevLock);
        }
    }

    private List<String> nactiRadkyZeSouboru(String nazevSouboru, String nazevLock, String prihlasenyUzivatel) {
        if (!ziskejZamek(nazevLock, prihlasenyUzivatel)) {
            throw new DatabaseUnavailableException("Soubor " + nazevSouboru + " je blokován jiným procesem.");
        }

        try {
            List<String> radky = new ArrayList<>();
            Path cestaKDatum = getCesta(nazevSouboru);
            Path cestaKPodpisu = getCesta(nazevSouboru + ".sig");

            zkontrolujIntegritu(cestaKDatum, cestaKPodpisu, nazevSouboru);
            
            if (Files.exists(cestaKDatum)) {
                radky = Files.readAllLines(cestaKDatum);
            }
            return radky; // Pokud soubor neexistuje, vrací se korektně prázdný list, ne null!
        } catch (IOException e) {
            throw new DatabaseUnavailableException("Chyba při čtení ze souboru " + nazevSouboru + ": " + e.getMessage());
        } finally {
            uvolniZamek(nazevLock);
        }
    }

    // Pokusí se vytvořit .lock soubor pro zajištění exkluzivního přístupu (ochrana proti souběžnému zápisu více instancí)
    private boolean zkusZiskatZamek(String nazevZamekSouboru, String prihlasenyUzivatel) {
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

            // ATOMICKÁ OPERACE: Přeskočíme exists() a zkusíme soubor přímo vytvořit.
            // Zabráníme tak TOCTOU zranitelnosti. Pokud soubor existuje, vyhodí výjimku.
            Files.createFile(lockPath);
            String lockOwnerInfo = System.getProperty("user.name") + "@" + cachedHostName + 
                " user: " + prihlasenyUzivatel + " @" + System.currentTimeMillis();
            Files.writeString(lockPath, lockOwnerInfo);
            return true;
        } catch (FileAlreadyExistsException e) {
            // Očekávaný stav - zámek právě drží jiná instance
            return false;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Nepodařilo se získat zámek " + nazevZamekSouboru + ": " + e.getMessage());
            return false;
        }
    }

    private boolean ziskejZamek(String nazevZamekSouboru, String prihlasenyUzivatel) {
        // První rychlý pokus (většinou projde hned bez zobrazení oken)
        if (zkusZiskatZamek(nazevZamekSouboru, prihlasenyUzivatel)) {
            return true;
        }

        // Pokud to nevyšlo, zobrazí se dialog a předá se mu funkce k opakovanému zkoušení
        CekaniNaZamekDialog dialog = new CekaniNaZamekDialog(() -> zkusZiskatZamek(nazevZamekSouboru, prihlasenyUzivatel));
        dialog.setVisible(true); 
        
        return dialog.isUspech();
    }

    private void uvolniZamek(String nazevZamekSouboru) {
        try {
            Files.deleteIfExists(getCesta(nazevZamekSouboru));
        } catch (IOException e) {
            System.err.println("Nepodařilo se smazat " + nazevZamekSouboru + " soubor!");
        }
    }

    private void podepisSoubor(Path cesta, String nazevSigSouboru) throws IOException {
        byte[] dataSouboru = Files.readAllBytes(cesta);
        String hashSouboru = Uzivatel.checkSum(new String(dataSouboru, StandardCharsets.UTF_8)); 
        Files.writeString(getCesta(nazevSigSouboru), hashSouboru);
    }

    private void zkontrolujIntegritu(Path cestaKDatum, Path cestaKPodpisu, String nazevSouboru) throws IOException {
        if (Files.exists(cestaKDatum)) {
            if (Files.exists(cestaKPodpisu)) {
                String aktualniPodpis = Uzivatel.checkSum(new String(Files.readAllBytes(cestaKDatum), StandardCharsets.UTF_8));
                String ulozenyPodpis = Files.readString(cestaKPodpisu);
    
                if (!aktualniPodpis.equals(ulozenyPodpis)) {
                    zaznamenejChybuIntegrity("VAROVÁNÍ: Celistvost souboru " + nazevSouboru + " byla narušena (nesouhlasí podpis)!");
                }
            } else {
                zaznamenejChybuIntegrity("VAROVÁNÍ: Chybí podpisový soubor pro " + nazevSouboru + "! Data mohla být upravena zvenčí.");
            }
        }
    }

    public void zaznamenejChybuIntegrity(String chyba) {
        chybyIntegrity.add(chyba);
        logEvent("ERROR", chyba);
    }

    public void logEvent(String level, String zprava) {
        
        if (!ziskejZamek(souborLockLog, "appSystem")) {
            System.err.println("Soubor logu je blokován jiným procesem. Nelze zapsat událost.");
            return;
        }

        try {
            String cas = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logZaznam = String.format("%s [%-7s] %s%n", cas, level, zprava);
            
            Path cestaKLogu = getCesta(souborDatLog);
            Files.writeString(cestaKLogu, logZaznam, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            podepisSoubor(cestaKLogu, souborDatLog + ".sig");
        } catch (IOException e) {
            System.err.println("Nepodařilo se zapsat do logu: " + e.getMessage());
        } finally {
            uvolniZamek(souborLockLog);
        }
    }

    public void smazVsechnaData(String prihlasenyUzivatel) {
        if (ziskejZamek(souborLockKafari, prihlasenyUzivatel)) {
            try { Files.deleteIfExists(getCesta(souborDatKafari)); Files.deleteIfExists(getCesta(souborDatKafari + ".sig")); } 
            catch (IOException e) { System.err.println("Nelze smazat kafari.csv"); }
            uvolniZamek(souborLockKafari);
        }
        if (ziskejZamek(souborLockSklad, prihlasenyUzivatel)) {
            try { Files.deleteIfExists(getCesta(souborDatSklad)); Files.deleteIfExists(getCesta(souborDatSklad + ".sig")); } 
            catch (IOException e) { System.err.println("Nelze smazat sklad.csv"); }
            uvolniZamek(souborLockSklad);
        }
        if (ziskejZamek(souborLockVyuctovani, prihlasenyUzivatel)) {
            try { Files.deleteIfExists(getCesta(souborDatVyuctovani)); Files.deleteIfExists(getCesta(souborDatVyuctovani + ".sig")); } 
            catch (IOException e) { System.err.println("Nelze smazat vyuctovani.csv"); }
            uvolniZamek(souborLockVyuctovani);
        }
    }

}