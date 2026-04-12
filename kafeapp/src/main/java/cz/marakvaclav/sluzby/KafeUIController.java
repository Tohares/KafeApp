package cz.marakvaclav.sluzby;

import cz.marakvaclav.entity.*;
import java.io.File;
import java.util.List;

/**
 * Rozhraní definující operace, které může uživatelské rozhraní (View) vyžadovat po Controlleru.
 * Zajišťuje stoprocentní oddělení prezentační vrstvy od konkrétní implementace byznys logiky.
 */
public interface KafeUIController {
    interface AdminFactory {
        Admin create(String login, char[] h1, char[] h2, String iban, String cz);
    }

    void ukonceniAplikace();
    void zmenitPocetKav(String login, int novyPocet);
    Admin getAdmin();
    String getPrihlasenyUzivatel();
    boolean isAdmin();
    List<Kafar> getKafari();
    List<Kafar> getAktivniKafari();
    void deaktivovatKafare(String login);
    List<Kafar> getDeaktivovaniKafari();
    void obnovitKafare(String login);
    List<Vyuctovani> getSeznamVyuctovani();
    List<PolozkaSkladu> getSklad();
    void zpracujExportHistorie();
    void prepnoutDatabazi();
    void naskladnit(PolozkaSkladu p);
    void upravitPolozkuSkladu(PolozkaSkladu p);
    void smazatPolozkuSkladu(PolozkaSkladu p);
    boolean maNecoKVyuctovani();
    PolozkaSkladu getAgregovanaPolozka(Surovina surovina);
    void zpracujVyuctovani(int mnozstviKafe, int mnozstviMleka, int mnozstviCukr, int mnozstviCitr);
    void reloadVyuctovani();
    void stornovatVyuctovani(Vyuctovani adminVyuctovani);
    void stornoPlatby(Vyuctovani v);
    boolean jeVyuctovaniCastecneZaplaceno(Vyuctovani adminVyuctovani);
    void oznamitPlatbu(Vyuctovani v);
    void vypitKavu();
    void odebratKavu();
    void resetovatHesloKafare(String login, char[] noveHeslo);
    void zpracujPrihlaseni(String login, char[] heslo);
    void odhlasit();
    void zmenitHeslo(char[] stareHeslo, char[] noveHeslo);
    void zalozitUzivatele(String login, char[] heslo);
    int[] getStatistikyPrihlasenehoKafare();
    void aktualizujPlatebniUdajeAdmina(String iban, String cz);
    void zpracujPlatbu(Vyuctovani v);
    void importDatZeZalohy(File file, boolean isNuclear) throws Exception;
    List<String> getChybyIntegrity();
}