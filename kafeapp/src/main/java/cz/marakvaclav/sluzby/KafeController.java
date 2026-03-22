package cz.marakvaclav.sluzby;

import cz.marakvaclav.dialogy.VytvoreniAdminaDialog;
import cz.marakvaclav.entity.*;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.math.BigDecimal;

public class KafeController {
    private List<Kafar> kafari;
    private List<PolozkaSkladu> sklad;
    private List<Vyuctovani> seznamVyuctovani;
    private Admin admin;
    private String prihlasenyUzivatel = null;
    private KafeGui gui;

    public KafeController() {
    }

    public boolean inicializujAplikaci() {
        admin = SpravceSouboru.nactiAdmina();
        if (admin == null) {
            VytvoreniAdminaDialog dialog = new VytvoreniAdminaDialog(null);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) {
                admin = dialog.getAdmin();
                SpravceSouboru.ulozAdmina(admin, admin.getLogin());
            } else {
                return false;
            }
        }

        List<Kafar> nacteniKafaru = SpravceSouboru.nactiKafare();
        kafari = (nacteniKafaru == null) ? new ArrayList<>() : nacteniKafaru;

        List<PolozkaSkladu> nacteniSkladu = SpravceSouboru.nactiSklad();
        sklad = (nacteniSkladu == null) ? new ArrayList<>() : nacteniSkladu;

        List<Vyuctovani> nacteniVyuctovani = SpravceSouboru.nactiVyuctovani();
        seznamVyuctovani = (nacteniVyuctovani == null) ? new ArrayList<>() : nacteniVyuctovani;
        
        return true;
    }

    public void setGui(KafeGui gui) {
        this.gui = gui;
    }

    public List<Kafar> getKafari() { return kafari; }
    public List<PolozkaSkladu> getSklad() { return sklad; }
    public List<Vyuctovani> getSeznamVyuctovani() { return seznamVyuctovani; }
    public Admin getAdmin() { return admin; }
    public String getPrihlasenyUzivatel() { return prihlasenyUzivatel; }

    public boolean isAdmin() {
        return prihlasenyUzivatel != null && prihlasenyUzivatel.equals(admin.getLogin());
    }

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
            if (!inicializujAplikaci()) {
                System.exit(0);
            }
            gui.zobrazChybyIntegrity();
            gui.updateView();
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

    public void vypitKavu() {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(prihlasenyUzivatel)) {
                k.vypijKavu();
                SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
                if (gui != null) gui.updateView();
                break;
            }
        }
    }

    public void zmenitPocetKav(String login, int novyPocet) {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(login)) {
                k.setPocetVypitychKav(novyPocet);
                SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
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
        }        return null;
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
}