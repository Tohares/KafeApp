package cz.marakvaclav.sluzby;

import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.entity.Admin;
import java.util.List;

/**
 * Rozhraní definující operace, které může Controller vyžadovat po View (uživatelském rozhraní).
 * Díky tomuto rozhraní je Controller plně odstřižen od konkrétní implementace GUI (např. Swing nebo JavaFX).
 */
public interface KafeView {
    void nastavStavNacitani(boolean nacitam);
    void zobrazChybyIntegrity();
    void zobrazPanelKafaru();
    void zobrazPanelUctenek();
    void updateView();
    String vyberSlozku(String titulek, String vychoziSlozka);
    void nastavViditelnostZapisovani(boolean viditelne);
    void otevriExportHistorieDialog(List<Vyuctovani> historie);
    Admin vyzadejNovehoAdmina(KafeUIController.AdminFactory factory);
    char[] vyzadejVynucenouZmenuHesla();
}