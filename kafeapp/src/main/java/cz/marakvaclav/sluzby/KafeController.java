package cz.marakvaclav.sluzby;

import cz.marakvaclav.entity.*;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

// Hlavní kontroler aplikace. Propojuje uživatelské rozhraní (GUI) s datovou vrstvou (SpravceSouboru) a udržuje stav aplikace v paměti.
public class KafeController implements KafeUIController {
    private KafeView view;
    private final SpravceSouboru spravceSouboru;

    // Instanciace doménových služeb
    private final AuthService authService;
    private final SkladService skladService;
    private final VyuctovaniService vyuctovaniService;

    // Jednovláknový exekutor garantuje, že se síťové zápisy řadí za sebe, neblokují UI a neperou se o zámky (Optimistic UI)
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    // Počítadlo běžících I/O operací pro zajištění bezpečného ukončení (aby se aplikace nezavřela během zápisu)
    private final AtomicInteger pocetAktivnichZapisu = new AtomicInteger(0);
    private boolean cekaNaUkonceni = false;

    public KafeController(SpravceSouboru spravceSouboru, AuthService authService, SkladService skladService, VyuctovaniService vyuctovaniService) {
        this.spravceSouboru = spravceSouboru;
        this.authService = authService;
        this.skladService = skladService;
        this.vyuctovaniService = vyuctovaniService;
    }

    public boolean inicializujAplikaci() {
        return true;
    }

    public void setView(KafeView view) {
        this.view = view;
        spustNacitaniDat();
    }

    // Asynchronně načte veškerá data (uživatele, sklad, účtenky) přes SpravceSouboru, aby neblokoval GUI
    public void spustNacitaniDat() {
        if (view != null) view.nastavStavNacitani(true);
        
        ioExecutor.submit(() -> {
            try {
                Admin admin = null;
                Admin tempAdmin = spravceSouboru.nactiAdmina();
                if (tempAdmin == null) {
                    if (view != null) {
                        admin = view.vyzadejNovehoAdmina((login, h1, h2, iban, cz) -> {
                            if (login.trim().isEmpty()) throw new IllegalArgumentException("Login nesmí být prázdný!");
                            if (h1.length == 0) throw new IllegalArgumentException("Heslo nesmí být prázdné!");
                            if (!Arrays.equals(h1, h2)) throw new IllegalArgumentException("Hesla se neshodují!");
                            if (iban != null && !iban.isEmpty() && !Admin.isValidIBAN(iban)) throw new IllegalArgumentException("Neplatný formát IBAN!");
                            if (!Admin.isCzAccountConsistentWithIban(cz, iban)) throw new IllegalArgumentException("České číslo účtu neodpovídá zadanému IBANu!");
                            
                            String cleanIban = iban.replaceAll("\\s+", "").toUpperCase();
                            try {
                                return new Admin(login.trim(), h1, cleanIban, cz.trim());
                            } finally {
                                java.util.Arrays.fill(h1, '0');
                                java.util.Arrays.fill(h2, '0');
                            }
                        });
                        if (admin == null) {
                            System.exit(0);
                        }
                    }
                    
                    if (admin != null) {
                        spravceSouboru.ulozAdmina(admin, admin.getLogin());
                    }
                } else {
                    admin = tempAdmin;
                }
                authService.setAdmin(admin);

                List<Kafar> nacteniKafaru = spravceSouboru.nactiKafare();
                authService.setKafari((nacteniKafaru == null) ? new ArrayList<>() : nacteniKafaru);

                List<PolozkaSkladu> nacteniSkladu = spravceSouboru.nactiSklad();
                skladService.setSklad((nacteniSkladu == null) ? new ArrayList<>() : nacteniSkladu);

                List<Vyuctovani> nacteniVyuctovani = spravceSouboru.nactiVyuctovani();
                vyuctovaniService.setSeznamVyuctovani((nacteniVyuctovani == null) ? new ArrayList<>() : nacteniVyuctovani);
                
                if (view != null) {
                    view.nastavStavNacitani(false);
                    view.zobrazChybyIntegrity();
                }
            } catch (SpravceSouboru.DatabaseUnavailableException e) {
                spravceSouboru.logEvent("ERROR", "Výpadek databáze při načítání: " + e.getMessage());
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (view != null) view.nastavStavNacitani(false);
                    odhlasit();
                    throw new SpravceSouboru.DatabaseUnavailableException("Spojení s databází bylo přerušeno:\n" + e.getMessage());
                });
            }
        });
    }

    public List<Kafar> getKafari() { return authService.getKafari(); }
    public List<Kafar> getAktivniKafari() { 
        return authService.getKafari().stream().filter(Kafar::isAktivni).collect(java.util.stream.Collectors.toList()); 
    }
    public List<Kafar> getDeaktivovaniKafari() {
        return authService.getKafari().stream().filter(k -> !k.isAktivni()).collect(java.util.stream.Collectors.toList());
    }
    public List<PolozkaSkladu> getSklad() { return skladService.getSklad(); }
    public List<Vyuctovani> getSeznamVyuctovani() { return vyuctovaniService.getSeznamVyuctovani(); }
    public Admin getAdmin() { return authService.getAdmin(); }
    public String getPrihlasenyUzivatel() { return authService.getPrihlasenyUzivatel(); }
    public List<String> getChybyIntegrity() { return spravceSouboru.getChybyIntegrity(); }

    // Zkontroluje, zda je aktuálně přihlášený uživatel administrátorem
    public boolean isAdmin() {
        return authService.isAdmin();
    }

    public void zpracujPrihlaseni(String login, char[] heslo) {
        try {
            authService.overPrihlaseni(login, heslo);
            authService.prihlasit(login);
            spravceSouboru.logEvent("INFO", "Uživatel '" + login + "' se přihlásil.");
            if (!isAdmin()) {
                Kafar prihlasenyKafar = authService.getPrihlasenyKafar();
                if (prihlasenyKafar != null && prihlasenyKafar.isVyzadujeZmenuHesla()) {
                    char[] noveHeslo = view != null ? view.vyzadejVynucenouZmenuHesla() : null;
                    if (noveHeslo != null) {
                        try {
                            prihlasenyKafar.setHeslo(noveHeslo);
                            prihlasenyKafar.setVyzadujeZmenuHesla(false);
                            spravceSouboru.ulozKafare(prihlasenyKafar, login);
                            spravceSouboru.logEvent("INFO", "Uživatel '" + login + "' si úspěšně nastavil vynucené heslo.");
                        } finally {
                            java.util.Arrays.fill(noveHeslo, '0');
                        }
                    } else {
                        odhlasit();
                        return;
                    }
                }
            }

            if (isAdmin()) {
                if (view != null) view.zobrazPanelKafaru();
            } else {
                if (view != null) view.updateView();
            }
        } catch (IllegalArgumentException e) {
            spravceSouboru.logEvent("WARNING", "Neúspěšný pokus o přihlášení pro login: '" + login + "'");
            throw e;
        }
    }

    public void odhlasit() {
        authService.odhlasit();
        if (view != null) view.updateView();
    }

    public void prepnoutDatabazi() {
        String novaSlozka = view != null ? view.vyberSlozku("Vyberte složku s databází", spravceSouboru.getPracovniSlozka()) : null;
        if (novaSlozka != null) {
            spravceSouboru.logEvent("INFO", "Změna databázové složky z '" + spravceSouboru.getPracovniSlozka() + "' na '" + novaSlozka + "'");
            spravceSouboru.setPracovniSlozka(novaSlozka);
            spravceSouboru.vymazChybyIntegrity();
            odhlasit(); 
            spustNacitaniDat();
        }
    }

    public void aktualizujPlatebniUdajeAdmina(String iban, String cz) {
        if (iban != null && !iban.isEmpty() && !Admin.isValidIBAN(iban)) {
            throw new IllegalArgumentException("Neplatný formát IBAN! Změny nebyly uloženy.");
        }
        if (!Admin.isCzAccountConsistentWithIban(cz, iban)) {
            throw new IllegalArgumentException("České číslo účtu neodpovídá zadanému IBANu! Změny nebyly uloženy.");
        }
        String cleanIban = iban.replaceAll("\\s+", "").toUpperCase();
        if (isAdmin()) {
            authService.getAdmin().setCisloUctuIBAN(cleanIban);
            authService.getAdmin().setCisloUctuCZ(cz);
            spravceSouboru.logEvent("WARNING", "Administrátor změnil své platební údaje.");
            provedZapisNaPozadi(() -> spravceSouboru.ulozAdmina(authService.getAdmin(), authService.getPrihlasenyUzivatel()));
        }
    }

    public void zmenitHeslo(char[] stareHeslo, char[] noveHeslo) {
        authService.zmenitHeslo(stareHeslo, noveHeslo);
        spravceSouboru.logEvent("INFO", "Uživatel '" + authService.getPrihlasenyUzivatel() + "' si změnil heslo.");
        if (isAdmin()) {
            provedZapisNaPozadi(() -> spravceSouboru.ulozAdmina(authService.getAdmin(), authService.getPrihlasenyUzivatel()));
        } else {
            provedZapisNaPozadi(() -> spravceSouboru.ulozKafare(authService.getPrihlasenyKafar(), authService.getPrihlasenyUzivatel()));
        }
    }

    public void provedZapisNaPozadi(Runnable uloha) {
        pocetAktivnichZapisu.incrementAndGet();
        // Zobrazí varovný červený pruh upozorňující na probíhající síťovou komunikaci
        if (view != null) view.nastavViditelnostZapisovani(true);
        
        ioExecutor.submit(() -> {
            try {
                // Rychlé storno (Short-circuit): Pokud byl uživatel mezitím odhlášen (např. pád disku v předchozím vlákně),
                // nebo pokud probíhá vypínání aplikace, další naklikané úkoly z fronty už neprovádíme a zahodíme je.
                if (authService.getPrihlasenyUzivatel() == null || cekaNaUkonceni) {
                    return;
                }
                uloha.run();
            } catch (SpravceSouboru.DatabaseUnavailableException e) {
                if (cekaNaUkonceni) return; // Během vypínání aplikace už nezobrazujeme chybové hlášky
                spravceSouboru.logEvent("ERROR", "Zápis dat selhal (odpojený disk?): " + e.getMessage());
                
                // Okamžitě zneplatníme relaci i na pozadí. Tím se zablokují (viz výše) všechny případné další 
                // naplánované kliknutí, které si uživatel "naklikal" do fronty během toho, co disk neodpovídal.
                authService.odhlasit(); 
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    odhlasit(); // Bezpečnostně odhlásí uživatele a provede reset UI
                    spustNacitaniDat(); // Zkusí znovu navázat spojení a případně ukáže prázdnou uvítací obrazovku
                    throw new SpravceSouboru.DatabaseUnavailableException("Zápis selhal! Síťový disk byl pravděpodobně odpojen:\n" + e.getMessage());
                });
            } finally {
                if (pocetAktivnichZapisu.decrementAndGet() == 0) {
                    if (view != null) view.nastavViditelnostZapisovani(false);
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
        if (pocetAktivnichZapisu.get() > 0) {
            cekaNaUkonceni = true;
            throw new IllegalStateException("Aplikace právě ukládá data na síťový disk.\nVyčkejte prosím, po dokončení operace se zavře sama.");
        } else {
            ioExecutor.shutdown();
            System.exit(0);
        }
    }

    public void vypitKavu() {
        Kafar k = authService.vypitKavu();
        if (k != null) {
            spravceSouboru.logEvent("INFO", "Uživatel '" + authService.getPrihlasenyUzivatel() + "' si zaznamenal 1 kávu.");
            provedZapisNaPozadi(() -> spravceSouboru.ulozKafare(k, authService.getPrihlasenyUzivatel()));
            if (view != null) view.updateView();
        }
    }

    public void odebratKavu() {
        Kafar k = authService.odebratKavu();
        if (k != null) {
            spravceSouboru.logEvent("INFO", "Uživatel '" + authService.getPrihlasenyUzivatel() + "' stornoval 1 kávu.");
            provedZapisNaPozadi(() -> spravceSouboru.ulozKafare(k, authService.getPrihlasenyUzivatel()));
            if (view != null) view.updateView();
        }
    }

    public void zmenitPocetKav(String login, int novyPocet) {
        Kafar k = authService.zmenitPocetKav(login, novyPocet);
        if (k != null) {
            spravceSouboru.logEvent("WARNING", "Admin změnil počet káv uživateli '" + login + "' na " + novyPocet);
            provedZapisNaPozadi(() -> spravceSouboru.ulozKafare(k, authService.getPrihlasenyUzivatel()));
        }
    }

    public void resetovatHesloKafare(String login, char[] noveHeslo) {
        Kafar k = authService.resetovatHesloKafare(login, noveHeslo);
        if (k != null) {
            spravceSouboru.logEvent("WARNING", "Admin vynutil reset hesla uživateli '" + login + "'.");
            provedZapisNaPozadi(() -> spravceSouboru.ulozKafare(k, authService.getPrihlasenyUzivatel()));
        }
    }

    public void deaktivovatKafare(String login) {
        Kafar k = authService.getKafar(login);
        if (k == null) return;
        if (k.getPocetVypitychKav() > 0) {
            throw new IllegalStateException("Uživatele nelze smazat (deaktivovat), protože má nezúčtované kávy.");
        }
        for (Vyuctovani v : vyuctovaniService.getHistorieKafare(login)) {
            if (!v.getStavPlatby()) {
                throw new IllegalStateException("Uživatele nelze smazat (deaktivovat), protože má stále nezaplacené účtenky.");
            }
        }
        k.setAktivni(false);
        spravceSouboru.logEvent("WARNING", "Admin deaktivoval (soft-delete) uživatele '" + login + "'.");
        provedZapisNaPozadi(() -> spravceSouboru.ulozKafare(k, authService.getPrihlasenyUzivatel()));
    }

    public void obnovitKafare(String login) {
        Kafar k = authService.getKafar(login);
        if (k != null && !k.isAktivni()) {
            k.setAktivni(true);
            spravceSouboru.logEvent("INFO", "Admin obnovil (zrušil deaktivaci) uživatele '" + login + "'.");
            provedZapisNaPozadi(() -> spravceSouboru.ulozKafare(k, authService.getPrihlasenyUzivatel()));
        }
    }

    public void zalozitUzivatele(String login, char[] heslo) {
        Kafar k = authService.zalozitUzivatele(login, heslo);
        spravceSouboru.logEvent("INFO", "Vytvořen nový uživatel '" + login + "'.");
        spravceSouboru.ulozKafare(k, authService.getPrihlasenyUzivatel());
        if (view != null) view.updateView();
    }

    public void naskladnit(PolozkaSkladu p) {
        skladService.naskladnit(p);
        spravceSouboru.logEvent("INFO", "Naskladněno " + p.getKoupeneMnozstvi() + " " + p.getJednotka() + " " + p.getSurovina().getNazev() + " za celkem " + p.getCenaZaKus() + " " + p.getMenaPenezni());
        spravceSouboru.ulozPolozkuNaSklad(p, authService.getPrihlasenyUzivatel());
    }

    public void upravitPolozkuSkladu(PolozkaSkladu p) {
        spravceSouboru.logEvent("WARNING", "Upravena položka skladu ID " + p.getId() + " (" + p.getSurovina().getNazev() + ").");
        spravceSouboru.ulozPolozkuNaSklad(p, authService.getPrihlasenyUzivatel());
    }

    public void smazatPolozkuSkladu(PolozkaSkladu p) {
        skladService.smazatPolozku(p);
        spravceSouboru.logEvent("WARNING", "Trvale smazána položka skladu ID " + p.getId() + " (" + p.getSurovina().getNazev() + ").");
        spravceSouboru.prepisCelySklad(skladService.getSklad(), authService.getPrihlasenyUzivatel());
    }

    public void reloadVyuctovani() {
        vyuctovaniService.setSeznamVyuctovani(spravceSouboru.nactiVyuctovani());
    }

    public void zpracujExportHistorie() {
        List<Vyuctovani> historie = vyuctovaniService.getHistorieKafare(authService.getPrihlasenyUzivatel());
        if (historie.isEmpty()) {
            throw new IllegalStateException("Nemáte žádnou historii k exportu.");
        } else {
            if (view != null) view.otevriExportHistorieDialog(historie);
        }
    }

    public void zpracujPlatbu(Vyuctovani v) {
        v.setStavPlatby(true);
        v.setDatumPlatby(LocalDate.now());
        v.setPlatebniUcetIBAN(authService.getAdmin().getCisloUctuIBAN());
        v.setPlatebniUcetCZ(authService.getAdmin().getCisloUctuCZ());
        spravceSouboru.logEvent("INFO", "Účtenka uživatele '" + v.getLogin() + "' ze dne " + v.getDatumVystaveni() + " byla označena jako zaplacená.");
        
        Vyuctovani adminVyuc = null;
        boolean vsechnyZaplaceny = true;
        if (!v.getLogin().equals(authService.getAdmin().getLogin())) {
            adminVyuc = vyuctovaniService.najdiHlavniVyuctovaniKPoductence(v, authService.getAdmin().getLogin());
            vsechnyZaplaceny = vyuctovaniService.jsouVsechnyPoductenkyZaplacene(v, authService.getAdmin().getLogin());
            if (vsechnyZaplaceny && adminVyuc != null && !adminVyuc.getStavPlatby()) {
                adminVyuc.setStavPlatby(true);
                adminVyuc.setDatumPlatby(LocalDate.now());
                spravceSouboru.logEvent("INFO", "Hlavní vyúčtování ze dne " + adminVyuc.getDatumVystaveni() + " bylo automaticky označeno jako kompletně zaplacené.");
            }
        }
        
        final Vyuctovani finalAdminVyuc = adminVyuc;
        final boolean finalVsechnyZaplaceny = vsechnyZaplaceny;

        provedZapisNaPozadi(() -> {
            spravceSouboru.ulozVyuctovani(v, authService.getAdmin().getLogin());
            if (finalVsechnyZaplaceny && finalAdminVyuc != null) {
                spravceSouboru.ulozVyuctovani(finalAdminVyuc, authService.getAdmin().getLogin());
            }
        });
        
        if (view != null) view.zobrazPanelUctenek();
    }

    public void oznamitPlatbu(Vyuctovani v) {
        v.setOznamenoJakoZaplacene(true);
        spravceSouboru.logEvent("INFO", "Uživatel '" + v.getLogin() + "' oznámil odeslání platby za účtenku ze dne " + v.getDatumVystaveni() + ".");
        provedZapisNaPozadi(() -> spravceSouboru.ulozVyuctovani(v, authService.getPrihlasenyUzivatel()));
        if (view != null) view.zobrazPanelUctenek();
    }

    public void stornoPlatby(Vyuctovani v) {
        v.setStavPlatby(false);
        v.setDatumPlatby(null);
        v.setPlatebniUcetIBAN(null);
        v.setPlatebniUcetCZ(null);
        v.setOznamenoJakoZaplacene(false);
        spravceSouboru.logEvent("INFO", "Platba účtenky uživatele '" + v.getLogin() + "' ze dne " + v.getDatumVystaveni() + " byla stornována.");

        Vyuctovani adminVyuc = null;
        if (!v.getLogin().equals(authService.getAdmin().getLogin())) {
            adminVyuc = vyuctovaniService.najdiHlavniVyuctovaniKPoductence(v, authService.getAdmin().getLogin());
            if (adminVyuc != null && adminVyuc.getStavPlatby()) {
                adminVyuc.setStavPlatby(false);
                adminVyuc.setDatumPlatby(null);
                spravceSouboru.logEvent("INFO", "Hlavní vyúčtování ze dne " + adminVyuc.getDatumVystaveni() + " bylo automaticky označeno jako nekompletní (storno podúčtenky).");
            }
        }

        final Vyuctovani finalAdminVyuc = adminVyuc;
        provedZapisNaPozadi(() -> {
            spravceSouboru.ulozVyuctovani(v, authService.getAdmin().getLogin());
            if (finalAdminVyuc != null) {
                spravceSouboru.ulozVyuctovani(finalAdminVyuc, authService.getAdmin().getLogin());
            }
        });

        if (view != null) view.zobrazPanelUctenek();
    }

    public boolean jeVyuctovaniCastecneZaplaceno(Vyuctovani adminVyuctovani) {
        return vyuctovaniService.jeVyuctovaniCastecneZaplaceno(adminVyuctovani, authService.getAdmin().getLogin());
    }

    public int[] getStatistikyPrihlasenehoKafare() {
        int nezuctovane = 0, nezaplacene = 0, zaplacene = 0;
        Kafar prihlaseny = authService.getPrihlasenyKafar();
        if (prihlaseny != null) nezuctovane = prihlaseny.getPocetVypitychKav();
        for (Vyuctovani v : vyuctovaniService.getHistorieKafare(authService.getPrihlasenyUzivatel())) {
            if (v.getStavPlatby()) zaplacene += v.getPocetVypitychKav();
            else nezaplacene += v.getPocetVypitychKav();
        }
        return new int[]{nezuctovane, nezaplacene, zaplacene};
    }

    public boolean maNecoKVyuctovani() {
        return authService.getPocetKavCelkem() > 0;
    }

    // Vrací agregovanou položku (sečte dostupná množství ze všech dřívějších nákupů dané suroviny se stejnou jednotkou)
    public PolozkaSkladu getAgregovanaPolozka(Surovina surovina) {
        return skladService.getAgregovanaPolozka(surovina);
    }

    // Hlavní transakční metoda: 1. Odečte suroviny ze skladu, 2. Vytvoří hlavní účtenku, 3. Rozúčtuje útratu kafařům a vynuluje jim počítadla
    public void zpracujVyuctovani(int mnozstviKafe, int mnozstviMleka, int mnozstviCukr, int mnozstviCitr) {
        skladService.overDostatek(Surovina.KAFE, mnozstviKafe);
        skladService.overDostatek(Surovina.MLEKO, mnozstviMleka);
        skladService.overDostatek(Surovina.CUKR, mnozstviCukr);
        skladService.overDostatek(Surovina.KYS_CITRONOVA, mnozstviCitr);

        List<PolozkaSkladu> skutecneSpotrebovane = new ArrayList<>();
        skutecneSpotrebovane.addAll(skladService.odeberSurovinu(Surovina.KAFE, mnozstviKafe));
        skutecneSpotrebovane.addAll(skladService.odeberSurovinu(Surovina.MLEKO, mnozstviMleka));
        skutecneSpotrebovane.addAll(skladService.odeberSurovinu(Surovina.CUKR, mnozstviCukr));
        skutecneSpotrebovane.addAll(skladService.odeberSurovinu(Surovina.KYS_CITRONOVA, mnozstviCitr));

        int pocetKavCelkem = authService.getPocetKavCelkem();
        String prihlasenyUzivatel = authService.getPrihlasenyUzivatel();
        Vyuctovani vyuctovani = new Vyuctovani(skutecneSpotrebovane, prihlasenyUzivatel, LocalDate.now(), pocetKavCelkem);
        vyuctovaniService.pridejVyuctovani(vyuctovani);
        spravceSouboru.ulozVyuctovani(vyuctovani, prihlasenyUzivatel);
        spravceSouboru.logEvent("INFO", "Provedeno hromadné vyúčtování pro " + pocetKavCelkem + " káv.");

        for (Kafar k : authService.getKafari()) {
            boolean zmena = false;
            if (k.getPocetVypitychKav() > 0) {
                Vyuctovani vk = new Vyuctovani(vyuctovani, k.getLogin(), k.getPocetVypitychKav());
                vyuctovaniService.pridejVyuctovani(vk);
                spravceSouboru.ulozVyuctovani(vk, prihlasenyUzivatel);
                k.setPocetVypitychKav(0);
                zmena = true;
            }
            if (k.getZruseneKavy() > 0) {
                k.setZruseneKavy(0); // Odpouštění hříchů při vyúčtování
                zmena = true;
            }
            if (zmena) {
                spravceSouboru.ulozKafare(k, prihlasenyUzivatel);
            }
        }
        spravceSouboru.prepisCelySklad(skladService.getSklad(), prihlasenyUzivatel);
    }
    
    public void stornovatVyuctovani(Vyuctovani adminVyuctovani) {
        if (vyuctovaniService.jeVyuctovaniCastecneZaplaceno(adminVyuctovani, authService.getAdmin().getLogin())) {
            throw new IllegalStateException("Nelze stornovat! Některé účtenky z tohoto vyúčtování již byly zaplaceny.");
        }

        List<Vyuctovani> kSmazani = vyuctovaniService.najdiVsechnyPoductenky(adminVyuctovani);
        String prihlasenyUzivatel = authService.getPrihlasenyUzivatel();

        // 2. Káva se vrátí zpět na vrub kafařům
        for (Vyuctovani v : kSmazani) {
            if (!v.getLogin().equals(authService.getAdmin().getLogin())) { 
                Kafar k = authService.getKafar(v.getLogin());
                if (k != null) {
                    k.setPocetVypitychKav(k.getPocetVypitychKav() + v.getPocetVypitychKav());
                    spravceSouboru.ulozKafare(k, prihlasenyUzivatel);
                }
            }
        }

        // 3. Spotřebované suroviny se vrátí zpět na sklad formou "refundace"
        skladService.vratSurovinyNaSklad(adminVyuctovani.getSpotrebovanePolozky());

        // 4. Účtenky se smažou z paměti a natvrdo se přepíše soubor
        vyuctovaniService.smazVyuctovani(kSmazani);
        spravceSouboru.logEvent("WARNING", "Hromadné vyúčtování ze dne " + adminVyuctovani.getDatumVystaveni() + " bylo STORNOVÁNO. Suroviny vráceny na sklad.");
        spravceSouboru.prepisVsechnaVyuctovani(vyuctovaniService.getSeznamVyuctovani(), prihlasenyUzivatel);
        spravceSouboru.prepisCelySklad(skladService.getSklad(), prihlasenyUzivatel);
    }

    public void importDatZeZalohy(File file, boolean isNuclear) throws Exception {
        // Nahrání celého obsahu s normalizací konců řádků (Windows vs Linux formát)
        String content = Files.readString(file.toPath()).replace("\r\n", "\n");
        int sigIndex = content.indexOf(SpravceSouboru.HEADER_SIGNATURE + "\n");
        if (sigIndex == -1) throw new Exception("Soubor neobsahuje platný podpis zálohy.");
        
        // Rozdělení obsahu na čistá data a samotný kontrolní hash pro účely ověření integrity
        String dataToHash = content.substring(0, sigIndex);
        String signatureInFile = content.substring(sigIndex + SpravceSouboru.HEADER_SIGNATURE.length() + 1).trim();
        
        if (!Uzivatel.checkSum(dataToHash).equals(signatureInFile)) {
            throw new Exception("Podpis souboru nesouhlasí! Data byla poškozena nebo upravena.");
        }
        
        List<Kafar> impKafari = new ArrayList<>();
        List<PolozkaSkladu> impSklad = new ArrayList<>();
        List<Vyuctovani> impVyuctovani = new ArrayList<>();
        Admin impAdmin = null;
        
        int section = 0; // 1=KAFARI, 2=SKLAD, 3=VYUCTOVANI
        for (String line : dataToHash.split("\n")) {
            if (line.equals(SpravceSouboru.HEADER_KAFARI)) { section = 1; continue; }
            if (line.equals(SpravceSouboru.HEADER_SKLAD)) { section = 2; continue; }
            if (line.equals(SpravceSouboru.HEADER_VYUCTOVANI)) { section = 3; continue; }
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(";");
            if (section == 1 && parts.length >= 4) {
                try {
                    int pocet = Integer.parseInt(parts[2]);
                    if (parts.length >= 6) { // Bezpečnostní kontrola specifická pro Kafaře
                        Kafar k = new Kafar(parts[0], new char[0]);
                        k.setHesloHash(parts[1]);
                        k.setPocetVypitychKav(pocet);
                        k.setVyzadujeZmenuHesla(Boolean.parseBoolean(parts[3]));
                        k.setZruseneKavy(Integer.parseInt(parts[4]));
                        k.setAktivni(Boolean.parseBoolean(parts[5]));
                        impKafari.add(k);
                    }
                } catch (NumberFormatException e) {
                    Admin a = new Admin(parts[0], new char[0]);
                    a.setHesloHash(parts[1]);
                    a.setCisloUctuIBAN(parts[2]);
                    a.setCisloUctuCZ(parts[3]);
                    impAdmin = a;
                }
            } else if (section == 2 && parts.length == 7) {
                Surovina sur = Surovina.fromString(parts[1]);
                if (sur != null) {
                    impSklad.add(new PolozkaSkladu(Integer.parseInt(parts[0]), sur, Integer.parseInt(parts[2]), 
                        Integer.parseInt(parts[3]), parts[4], new BigDecimal(parts[5]), parts[6]));
                }
            } else if (section == 3 && parts.length >= 12) {
                Vyuctovani v = new Vyuctovani();
                v.fromCsv(parts);
                impVyuctovani.add(v);
            }
        }
        
        if (isNuclear) {
            spravceSouboru.logEvent("WARNING", "Uživatel zahájil NUKLEÁRNÍ IMPORT dat! Současná data budou nenávratně ztracena.");
            spravceSouboru.smazVsechnaData(authService.getPrihlasenyUzivatel());
            authService.getKafari().clear(); authService.getKafari().addAll(impKafari);
            skladService.getSklad().clear(); skladService.getSklad().addAll(impSklad);
            vyuctovaniService.getSeznamVyuctovani().clear(); vyuctovaniService.getSeznamVyuctovani().addAll(impVyuctovani);
            if (impAdmin != null) {
                authService.getAdmin().setLogin(impAdmin.getLogin());
                authService.getAdmin().setHesloHash(impAdmin.getHesloHash());
                authService.getAdmin().setCisloUctuIBAN(impAdmin.getCisloUctuIBAN());
                authService.getAdmin().setCisloUctuCZ(impAdmin.getCisloUctuCZ());
            }
        } else {
            spravceSouboru.logEvent("INFO", "Uživatel zahájil chytré sloučení dat ze zálohy.");
            // Chytré sloučení (Merge)
            for (Kafar ik : impKafari) {
                int index = authService.getKafari().indexOf(ik);
                if (index != -1) {
                    Kafar ck = authService.getKafari().get(index);
                    ck.setPocetVypitychKav(Math.max(ck.getPocetVypitychKav(), ik.getPocetVypitychKav()));
                    ck.setZruseneKavy(Math.max(ck.getZruseneKavy(), ik.getZruseneKavy()));
                } else {
                    authService.getKafari().add(ik);
                }
            }
            for (PolozkaSkladu is : impSklad) {
                int index = skladService.getSklad().indexOf(is);
                if (index != -1) {
                    PolozkaSkladu cs = skladService.getSklad().get(index);
                    // Jedná se o stejnou naskladněnou položku, ponecháme tu s více spotřebovaným množstvím
                    cs.setAktualniMnozstvi(Math.min(cs.getAktualniMnozstvi(), is.getAktualniMnozstvi()));
                } else {
                    skladService.getSklad().add(is);
                }
            }
            for (Vyuctovani iv : impVyuctovani) {
                int index = vyuctovaniService.getSeznamVyuctovani().indexOf(iv);
                if (index != -1) {
                    Vyuctovani cv = vyuctovaniService.getSeznamVyuctovani().get(index);
                    // Pokud účtenka ze zálohy je zaplacená a současná nikoliv, převezmeme platbu ze zálohy
                    if (iv.getStavPlatby() && !cv.getStavPlatby()) {
                        cv.setStavPlatby(true);
                        cv.setDatumPlatby(iv.getDatumPlatby());
                    }
                } else {
                    vyuctovaniService.getSeznamVyuctovani().add(iv);
                }
            }
            if (impAdmin != null) {
                authService.getAdmin().setLogin(impAdmin.getLogin());
                authService.getAdmin().setHesloHash(impAdmin.getHesloHash());
                authService.getAdmin().setCisloUctuIBAN(impAdmin.getCisloUctuIBAN());
                authService.getAdmin().setCisloUctuCZ(impAdmin.getCisloUctuCZ());
            }
        }
        
        // Hromadný zápis - provede přepis celých tabulek najednou. Drasticky urychluje import u pomalých síťových disků.
        spravceSouboru.prepisVsechnyUzivatele(authService.getKafari(), authService.getAdmin(), authService.getPrihlasenyUzivatel());
        spravceSouboru.prepisCelySklad(skladService.getSklad(), authService.getPrihlasenyUzivatel());
        spravceSouboru.prepisVsechnaVyuctovani(vyuctovaniService.getSeznamVyuctovani(), authService.getPrihlasenyUzivatel());
        
        if (isNuclear) {
            odhlasit();
        }
    }
}