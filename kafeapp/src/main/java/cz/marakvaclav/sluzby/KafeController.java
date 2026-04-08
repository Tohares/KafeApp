package cz.marakvaclav.sluzby;

import cz.marakvaclav.dialogy.VytvoreniAdminaDialog;
import cz.marakvaclav.entity.*;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

// Hlavní kontroler aplikace. Propojuje uživatelské rozhraní (GUI) s datovou vrstvou (SpravceSouboru) a udržuje stav aplikace v paměti.
public class KafeController {
    private List<Kafar> kafari;
    private List<PolozkaSkladu> sklad;
    private List<Vyuctovani> seznamVyuctovani;
    private Admin admin;
    private String prihlasenyUzivatel = null;
    private KafeGui gui;

    // Jednovláknový exekutor garantuje, že se síťové zápisy řadí za sebe, neblokují UI a neperou se o zámky (Optimistic UI)
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    // Počítadlo běžících I/O operací pro zajištění bezpečného ukončení (aby se aplikace nezavřela během zápisu)
    private final AtomicInteger pocetAktivnichZapisov = new AtomicInteger(0);
    private boolean cekaNaUkonceni = false;

    public KafeController() {
    }

    public boolean inicializujAplikaci() {
        // Rychlá inicializace prázdných datových struktur.
        // Skutečné síťové čtení se odstartuje asynchronně metodou spustNacitaniDat(), aby grafické UI naběhlo okamžitě.
        kafari = new ArrayList<>();
        sklad = new ArrayList<>();
        seznamVyuctovani = new ArrayList<>();
        
        return true;
    }

    public void setGui(KafeGui gui) {
        this.gui = gui;
        spustNacitaniDat();
    }

    // Asynchronně načte veškerá data (uživatele, sklad, účtenky) přes SpravceSouboru, aby neblokoval GUI
    public void spustNacitaniDat() {
        if (gui != null) javax.swing.SwingUtilities.invokeLater(() -> gui.nastavStavNacitani(true));
        
        ioExecutor.submit(() -> {
            Admin tempAdmin = SpravceSouboru.nactiAdmina();
            if (tempAdmin == null) {
                try {
                    // Vytvoření uživatele vyžaduje grafický dialog, proto se toto výjimečně hodí zpět do hlavního vlákna
                    javax.swing.SwingUtilities.invokeAndWait(() -> {
                        VytvoreniAdminaDialog dialog = new VytvoreniAdminaDialog(gui);
                        dialog.setVisible(true);
                        if (dialog.isSucceeded()) {
                            admin = dialog.getAdmin();
                            SpravceSouboru.ulozAdmina(admin, admin.getLogin());
                        } else {
                            System.exit(0);
                        }
                    });
                } catch (Exception e) {}
            } else {
                admin = tempAdmin;
            }

            List<Kafar> nacteniKafaru = SpravceSouboru.nactiKafare();
            kafari = (nacteniKafaru == null) ? new ArrayList<>() : nacteniKafaru;

            List<PolozkaSkladu> nacteniSkladu = SpravceSouboru.nactiSklad();
            sklad = (nacteniSkladu == null) ? new ArrayList<>() : nacteniSkladu;

            List<Vyuctovani> nacteniVyuctovani = SpravceSouboru.nactiVyuctovani();
            seznamVyuctovani = (nacteniVyuctovani == null) ? new ArrayList<>() : nacteniVyuctovani;
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.nastavStavNacitani(false);
                    gui.zobrazChybyIntegrity();
                }
            });
        });
    }

    public List<Kafar> getKafari() { return kafari; }
    public List<PolozkaSkladu> getSklad() { return sklad; }
    public List<Vyuctovani> getSeznamVyuctovani() { return seznamVyuctovani; }
    public Admin getAdmin() { return admin; }
    public String getPrihlasenyUzivatel() { return prihlasenyUzivatel; }

    // Zkontroluje, zda je aktuálně přihlášený uživatel administrátorem
    public boolean isAdmin() {
        return prihlasenyUzivatel != null && prihlasenyUzivatel.equals(admin.getLogin());
    }

    // Ověří přihlašovací údaje (login a hash hesla) vůči databázi uživatelů a adminovi
    public boolean prihlasit(String login, String heslo) {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(login) && Uzivatel.overHeslo(heslo, k.getHesloHash())) {
                prihlasenyUzivatel = login;
                return true;
            }
        }
        if (admin.getLogin().equals(login) && Uzivatel.overHeslo(heslo, admin.getHesloHash())) {
            prihlasenyUzivatel = login;
            return true;
        }
        return false;
    }

    public void zpracujPrihlaseni(String login, String heslo) {
        if (prihlasit(login, heslo)) {
            if (isAdmin()) {
                gui.zobrazPanelKafaru();
            } else {
                gui.updateView();
            }
        } else {
            gui.zobrazChybu("Špatný login nebo heslo!");
        }
    }

    public void odhlasit() {
        prihlasenyUzivatel = null;
        if (gui != null) gui.updateView();
    }

    public void prepnoutDatabazi() {
        String novaSlozka = gui.vyberSlozku("Vyberte složku s databází", SpravceSouboru.getPracovniSlozka());
        if (novaSlozka != null) {
            SpravceSouboru.setPracovniSlozka(novaSlozka);
            SpravceSouboru.chybyIntegrity.clear();
            odhlasit(); 
            spustNacitaniDat();
        }
    }

    public boolean zmenitHeslo(String stareHeslo, String noveHeslo) {
        if (prihlasenyUzivatel == null) return false;
        
        if (isAdmin()) {
            if (Uzivatel.overHeslo(stareHeslo, admin.getHesloHash())) {
                admin.setHeslo(noveHeslo);
                SpravceSouboru.ulozAdmina(admin, prihlasenyUzivatel);
                return true;
            }
        } else {
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    if (Uzivatel.overHeslo(stareHeslo, k.getHesloHash())) {
                        k.setHeslo(noveHeslo);
                        SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    public void provedZapisNaPozadi(Runnable uloha) {
        pocetAktivnichZapisov.incrementAndGet();
        // Zobrazí varovný červený pruh upozorňující na probíhající síťovou komunikaci
        if (gui != null) javax.swing.SwingUtilities.invokeLater(() -> gui.nastavViditelnostZapisovani(true));
        
        ioExecutor.submit(() -> {
            try {
                uloha.run();
            } finally {
                if (pocetAktivnichZapisov.decrementAndGet() == 0) {
                    if (gui != null) javax.swing.SwingUtilities.invokeLater(() -> gui.nastavViditelnostZapisovani(false));
                    // Pokud uživatel kliknul na křížek během ukládání, aplikace se po dokončení zápisu sama vypne
                    if (cekaNaUkonceni) {
                        System.exit(0);
                    }
                }
            }
        });
    }

    public void ukonceniAplikace() {
        // Pokud probíhá zápis na pozadí, zablokujeme okamžité zabití procesu, čímž předejdeme poškození datových souborů na síti
        if (pocetAktivnichZapisov.get() > 0) {
            cekaNaUkonceni = true;
            if (gui != null) {
                gui.zobrazInformaci("Aplikace právě ukládá data na síťový disk.\nVyčkejte prosím, po dokončení operace se zavře sama.");
            }
        } else {
            ioExecutor.shutdown();
            System.exit(0);
        }
    }

    public void vypitKavu() {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(prihlasenyUzivatel)) {
                k.vypijKavu();
                provedZapisNaPozadi(() -> SpravceSouboru.ulozKafare(k, prihlasenyUzivatel));
                if (gui != null) gui.updateView();
                break;
            }
        }
    }

    public void zmenitPocetKav(String login, int novyPocet) {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(login)) {
                k.setPocetVypitychKav(novyPocet);
                // Odkopnutí pomalého síťového I/O zápisu na pozadí, tabulka se ihned uvolní
                provedZapisNaPozadi(() -> SpravceSouboru.ulozKafare(k, prihlasenyUzivatel));
                break;
            }
        }
    }

    public void zalozitUzivatele(String login, String heslo) {
        Kafar k = new Kafar(login, heslo);
        kafari.add(k);
        SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
        if (gui != null) gui.updateView();
    }

    public void naskladnit(PolozkaSkladu p) {
        sklad.add(p);
        SpravceSouboru.ulozPolozkuNaSklad(p, prihlasenyUzivatel);
    }

    public void upravitPolozkuSkladu(PolozkaSkladu p) {
        SpravceSouboru.ulozPolozkuNaSklad(p, prihlasenyUzivatel);
    }

    public void smazatPolozkuSkladu(PolozkaSkladu p) {
        sklad.remove(p);
        SpravceSouboru.prepisCelySklad(sklad, prihlasenyUzivatel);
    }

    public void reloadVyuctovani() {
        seznamVyuctovani = SpravceSouboru.nactiVyuctovani();
    }

    public List<Vyuctovani> getHistoriePrihlasenehoKafare() {
        List<Vyuctovani> historie = new ArrayList<>();
        for (Vyuctovani v : seznamVyuctovani) {
            if (v.getLogin().equals(prihlasenyUzivatel)) {
                historie.add(v);
            }
        }
        return historie;
    }

    public void zpracujExportHistorie() {
        List<Vyuctovani> historie = getHistoriePrihlasenehoKafare();
        if (historie.isEmpty()) {
            gui.zobrazInformaci("Nemáte žádnou historii k exportu.");
        } else {
            gui.otevriExportHistorieDialog(historie);
        }
    }

    public void zpracujPlatbu(Vyuctovani v) {
        v.setStavPlatby(true);
        v.setDatumPlatby(LocalDate.now());
        SpravceSouboru.ulozVyuctovani(v, admin.getLogin());
        gui.zobrazPanelUctenek();
    }

    public int[] getStatistikyPrihlasenehoKafare() {
        int nezuctovane = 0, nezaplacene = 0, zaplacene = 0;
        for (Kafar k : kafari) {
            if (k.getLogin().equals(prihlasenyUzivatel)) {
                nezuctovane = k.getPocetVypitychKav();
                break;
            }
        }
        for (Vyuctovani v : seznamVyuctovani) {
            if (v.getLogin().equals(prihlasenyUzivatel)) {
                if (v.getStavPlatby()) zaplacene += v.getPocetVypitychKav();
                else nezaplacene += v.getPocetVypitychKav();
            }
        }
        return new int[]{nezuctovane, nezaplacene, zaplacene};
    }

    public boolean maNecoKVyuctovani() {
        return kafari.stream().mapToInt(Kafar::getPocetVypitychKav).sum() > 0;
    }

    public Vyuctovani najdiVyuctovani(String loginTab, LocalDate datumTab, BigDecimal cenaTab) {
        for (Vyuctovani v : seznamVyuctovani) {
            // Pozor: U BigDecimal je nutné použít compareTo() == 0, protože metoda equals() by brala v potaz i počet desetinných míst (12.1 vs 12.10)
            if (v.getLogin().equals(loginTab) && v.getDatumVystaveni().equals(datumTab) && 
               (v.getCenaZaVypiteKavy() != null && cenaTab != null && v.getCenaZaVypiteKavy().compareTo(cenaTab) == 0)) {
                return v;
            }
        }
        return null;
    }

    // Vrací agregovanou položku (sečte dostupná množství ze všech dřívějších nákupů dané suroviny se stejnou jednotkou)
    public PolozkaSkladu getAgregovanaPolozka(String nazev) {
        PolozkaSkladu combinedPolozka = null;
        for (PolozkaSkladu p : sklad) {
            if (p.getNazev().equals(nazev) && p.getAktualniMnozstvi() > 0) {
                if (combinedPolozka == null) {
                    combinedPolozka = new PolozkaSkladu(p.getId(), p.getNazev(), p.getKoupeneMnozstvi(),
                        p.getAktualniMnozstvi(), p.getJednotka(), p.getCenaZaKus(), p.getMenaPenezni());
                    } else if (combinedPolozka.getJednotka().equals(p.getJednotka())) {
                    combinedPolozka.setAktualniMnozstvi(combinedPolozka.getAktualniMnozstvi() + p.getAktualniMnozstvi());
                }
            }
        }
        if (combinedPolozka == null) {
            for (PolozkaSkladu p : sklad) {
                if (p.getNazev().equals(nazev)) {
                    combinedPolozka = new PolozkaSkladu(p.getId(), p.getNazev(), p.getKoupeneMnozstvi(),
                        p.getAktualniMnozstvi(), p.getJednotka(), p.getCenaZaKus(), p.getMenaPenezni());
                    break;
                }
            }
        }
        if (combinedPolozka == null) {
            combinedPolozka = new PolozkaSkladu(-1, nazev, 0, 0, "ks", BigDecimal.ZERO, "CZK");
        }
        return combinedPolozka;
    }

    private void odeberSurovinuFyzicky(String nazev, int mnozstvi, List<PolozkaSkladu> skutecneSpotrebovane) {
        if (mnozstvi <= 0) return;
        int zbyva = mnozstvi;
        
        for (PolozkaSkladu s : sklad) {
            if (s.getNazev().equals(nazev) && s.getAktualniMnozstvi() > 0) {
                int odebrat = Math.min(s.getAktualniMnozstvi(), zbyva);
                s.setAktualniMnozstvi(s.getAktualniMnozstvi() - odebrat);
                SpravceSouboru.ulozPolozkuNaSklad(s, prihlasenyUzivatel);
                
                // Do účtenky se poznamená přesný střípek, aby byla známa přesná původní cena a ID položky
                skutecneSpotrebovane.add(new PolozkaSkladu(s.getId(), s.getNazev(), odebrat, odebrat, s.getJednotka(), s.getCenaZaKus(), s.getMenaPenezni()));
                
                zbyva -= odebrat;
                if (zbyva <= 0) break;
            }
        }
    }

    // Hlavní transakční metoda: 1. Odečte suroviny ze skladu, 2. Vytvoří hlavní účtenku, 3. Rozúčtuje útratu kafařům a vynuluje jim počítadla
    public void zpracujVyuctovani(int mnozstviKafe, int mnozstviMleka, int mnozstviCukr, int mnozstviCitr) {
        // Pre-validace: Zkontroluje se, zda je na skladě dostatek surovin, ještě předtím, než se do něj začne zapisovat
        if (mnozstviKafe > getAgregovanaPolozka("Kafe").getAktualniMnozstvi()) {
            throw new IllegalArgumentException("Nedostatek suroviny na skladě: Káva");
        }
        if (mnozstviMleka > getAgregovanaPolozka("Mleko").getAktualniMnozstvi()) {
            throw new IllegalArgumentException("Nedostatek suroviny na skladě: Mléko");
        }
        if (mnozstviCukr > getAgregovanaPolozka("Cukr").getAktualniMnozstvi()) {
            throw new IllegalArgumentException("Nedostatek suroviny na skladě: Cukr");
        }
        if (mnozstviCitr > getAgregovanaPolozka("Kys. Citr.").getAktualniMnozstvi()) {
            throw new IllegalArgumentException("Nedostatek suroviny na skladě: Kys. citronová");
        }

        List<PolozkaSkladu> skutecneSpotrebovane = new ArrayList<>();
        
        odeberSurovinuFyzicky("Kafe", mnozstviKafe, skutecneSpotrebovane);
        odeberSurovinuFyzicky("Mleko", mnozstviMleka, skutecneSpotrebovane);
        odeberSurovinuFyzicky("Cukr", mnozstviCukr, skutecneSpotrebovane);
        odeberSurovinuFyzicky("Kys. Citr.", mnozstviCitr, skutecneSpotrebovane);

        int pocetKavCelkem = kafari.stream().mapToInt(Kafar::getPocetVypitychKav).sum();
        Vyuctovani vyuctovani = new Vyuctovani(skutecneSpotrebovane, prihlasenyUzivatel, LocalDate.now(), pocetKavCelkem);
        SpravceSouboru.ulozVyuctovani(vyuctovani, prihlasenyUzivatel);

        for (Kafar k : kafari) {
            if (k.getPocetVypitychKav() > 0) {
                Vyuctovani vk = new Vyuctovani(vyuctovani, k.getLogin(), k.getPocetVypitychKav());
                SpravceSouboru.ulozVyuctovani(vk, prihlasenyUzivatel);
                k.setPocetVypitychKav(0);
                SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
            }
        }
    }
    
    public boolean stornovatVyuctovani(Vyuctovani adminVyuctovani) {
        // 1. Najdou se všechny účtenky ze stejné dávky (podle data a celkové ceny vyúčtování)
        List<Vyuctovani> kSmazani = new ArrayList<>();
        for (Vyuctovani v : seznamVyuctovani) {
            if (v.getDatumVystaveni().equals(adminVyuctovani.getDatumVystaveni()) && 
                v.getCelkovaCena().compareTo(adminVyuctovani.getCelkovaCena()) == 0 &&
                v.getPocetUctovanychKavCelkem() == adminVyuctovani.getPocetUctovanychKavCelkem()) {
                
                if (v.getStavPlatby()) {
                    return false; // Nelze stornovat, někdo už svou část zaplatil!
                }
                kSmazani.add(v);
            }
        }

        // 2. Káva se vrátí zpět na vrub kafařům
        for (Vyuctovani v : kSmazani) {
            if (!v.getLogin().equals(admin.getLogin())) { // Přeskočí se adminova hlavička
                for (Kafar k : kafari) {
                    if (k.getLogin().equals(v.getLogin())) {
                        k.setPocetVypitychKav(k.getPocetVypitychKav() + v.getPocetVypitychKav());
                        SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
                        break;
                    }
                }
            }
        }

        // 3. Spotřebované suroviny se vrátí zpět na sklad formou "refundace"
        for (PolozkaSkladu spotrebovana : adminVyuctovani.getSpotrebovanePolozky()) {
            if (spotrebovana.getAktualniMnozstvi() > 0) {
                for (PolozkaSkladu s : sklad) {
                    if (s.getId() == spotrebovana.getId()) {
                        s.setAktualniMnozstvi(s.getAktualniMnozstvi() + spotrebovana.getAktualniMnozstvi());
                        SpravceSouboru.ulozPolozkuNaSklad(s, prihlasenyUzivatel);
                        break;
                    }
                }
            }
        }

        // 4. Účtenky se smažou z paměti a natvrdo se přepíše soubor
        seznamVyuctovani.removeAll(kSmazani);
        SpravceSouboru.prepisVsechnaVyuctovani(seznamVyuctovani, prihlasenyUzivatel);
        
        return true;
    }

    public void importDatZeZalohy(File file, boolean isNuclear) throws Exception {
        // Nahrání celého obsahu s normalizací konců řádků (Windows vs Linux formát)
        String content = Files.readString(file.toPath()).replace("\r\n", "\n");
        int sigIndex = content.indexOf("===SIGNATURE===\n");
        if (sigIndex == -1) throw new Exception("Soubor neobsahuje platný podpis zálohy.");
        
        // Rozdělení obsahu na čistá data a samotný kontrolní hash pro účely ověření integrity
        String dataToHash = content.substring(0, sigIndex);
        String signatureInFile = content.substring(sigIndex + 16).trim();
        
        if (!Uzivatel.checkSum(dataToHash).equals(signatureInFile)) {
            throw new Exception("Podpis souboru nesouhlasí! Data byla poškozena nebo upravena.");
        }
        
        List<Kafar> impKafari = new ArrayList<>();
        List<PolozkaSkladu> impSklad = new ArrayList<>();
        List<Vyuctovani> impVyuctovani = new ArrayList<>();
        Admin impAdmin = null;
        
        int section = 0; // 1=KAFARI, 2=SKLAD, 3=VYUCTOVANI
        for (String line : dataToHash.split("\n")) {
            if (line.equals("===KAFARI===")) { section = 1; continue; }
            if (line.equals("===SKLAD===")) { section = 2; continue; }
            if (line.equals("===VYUCTOVANI===")) { section = 3; continue; }
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(";");
            if (section == 1 && parts.length >= 3) {
                try {
                    int pocet = Integer.parseInt(parts[2]);
                    Kafar k = new Kafar(parts[0], "");
                    k.setHesloHash(parts[1]);
                    k.setPocetVypitychKav(pocet);
                    impKafari.add(k);
                } catch (NumberFormatException e) {
                    if (parts.length >= 4) {
                        Admin a = new Admin(parts[0], "");
                        a.setHesloHash(parts[1]);
                        a.setCisloUctuIBAN(parts[2]);
                        a.setCisloUctuCZ(parts[3]);
                        impAdmin = a;
                    }
                }
            } else if (section == 2 && parts.length == 7) {
                impSklad.add(new PolozkaSkladu(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]), 
                    Integer.parseInt(parts[3]), parts[4], new BigDecimal(parts[5]), parts[6]));
            } else if (section == 3 && parts.length > 10) {
                Vyuctovani v = new Vyuctovani();
                v.fromCsv(parts);
                impVyuctovani.add(v);
            }
        }
        
        if (isNuclear) {
            SpravceSouboru.smazVsechnaData(prihlasenyUzivatel);
            kafari.clear(); kafari.addAll(impKafari);
            sklad.clear(); sklad.addAll(impSklad);
            seznamVyuctovani.clear(); seznamVyuctovani.addAll(impVyuctovani);
            if (impAdmin != null) {
                admin.setLogin(impAdmin.getLogin());
                admin.setHesloHash(impAdmin.getHesloHash());
                admin.setCisloUctuIBAN(impAdmin.getCisloUctuIBAN());
                admin.setCisloUctuCZ(impAdmin.getCisloUctuCZ());
            }
        } else {
            // Chytré sloučení (Merge)
            for (Kafar ik : impKafari) {
                boolean found = false;
                for (Kafar ck : kafari) {
                    if (ck.getLogin().equals(ik.getLogin())) {
                        ck.setPocetVypitychKav(Math.max(ck.getPocetVypitychKav(), ik.getPocetVypitychKav()));
                        found = true; break;
                    }
                }
                if (!found) kafari.add(ik);
            }
            for (PolozkaSkladu is : impSklad) {
                boolean found = false;
                for (PolozkaSkladu cs : sklad) {
                    if (cs.getNazev().equals(is.getNazev()) && cs.getJednotka().equals(is.getJednotka())) {
                        cs.setKoupeneMnozstvi(cs.getKoupeneMnozstvi() + is.getKoupeneMnozstvi());
                        cs.setAktualniMnozstvi(cs.getAktualniMnozstvi() + is.getAktualniMnozstvi());
                        found = true; break;
                    }
                }
                if (!found) sklad.add(is);
            }
            for (Vyuctovani iv : impVyuctovani) {
                boolean found = false;
                for (Vyuctovani cv : seznamVyuctovani) {
                    if (cv.getLogin().equals(iv.getLogin()) && cv.getDatumVystaveni().equals(iv.getDatumVystaveni()) && (cv.getCenaZaVypiteKavy() != null && iv.getCenaZaVypiteKavy() != null && cv.getCenaZaVypiteKavy().compareTo(iv.getCenaZaVypiteKavy()) == 0)) {
                        found = true; break;
                    }
                }
                if (!found) seznamVyuctovani.add(iv);
            }
            if (impAdmin != null) {
                admin.setLogin(impAdmin.getLogin());
                admin.setHesloHash(impAdmin.getHesloHash());
                admin.setCisloUctuIBAN(impAdmin.getCisloUctuIBAN());
                admin.setCisloUctuCZ(impAdmin.getCisloUctuCZ());
            }
        }
        
        // Hromadný zápis - provede přepis celých tabulek najednou. Drasticky urychluje import u pomalých síťových disků.
        SpravceSouboru.prepisVsechnyUzivatele(kafari, admin, prihlasenyUzivatel);
        SpravceSouboru.prepisCelySklad(sklad, prihlasenyUzivatel);
        SpravceSouboru.prepisVsechnaVyuctovani(seznamVyuctovani, prihlasenyUzivatel);
    }
}