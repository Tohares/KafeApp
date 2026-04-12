package cz.marakvaclav.panely;

import javax.swing.*;

public class KafeMenuBar extends JMenuBar {
    private JMenuItem menuItemPrihlasit;
    private JMenuItem menuItemOdhlasit;
    private JMenuItem menuItemZmenitHeslo;
    private JMenuItem menuItemExportZalohy;
    private JMenuItem menuItemImportZalohy;
    private JMenu menuZaloha;

    public KafeMenuBar(MenuActionHandler handler) {
        JMenu menuSoubor = new JMenu("Aplikace");
        menuItemPrihlasit = new JMenuItem("Přihlásit");
        menuItemOdhlasit = new JMenuItem("Odhlásit");
        menuItemZmenitHeslo = new JMenuItem("Změnit heslo");
        JMenuItem menuItemPrepnoutDatabazi = new JMenuItem("Přepnout databázi");
        JMenuItem menuItemKonec = new JMenuItem("Ukončit");

        menuItemPrihlasit.addActionListener(e -> handler.prihlasit());
        menuItemOdhlasit.addActionListener(e -> handler.odhlasit());
        menuItemZmenitHeslo.addActionListener(e -> handler.zmenitHeslo());
        menuItemPrepnoutDatabazi.addActionListener(e -> handler.prepnoutDatabazi());
        menuItemKonec.addActionListener(e -> handler.ukoncitAplikaci());

        menuSoubor.add(menuItemPrihlasit);
        menuSoubor.add(menuItemOdhlasit);
        menuSoubor.add(menuItemZmenitHeslo);
        menuSoubor.addSeparator();
        menuSoubor.add(menuItemPrepnoutDatabazi);
        menuSoubor.addSeparator();
        menuSoubor.add(menuItemKonec);

        menuZaloha = new JMenu("Zálohování");
        menuItemExportZalohy = new JMenuItem("Export kompletní zálohy");
        menuItemImportZalohy = new JMenuItem("Import dat ze zálohy");

        menuItemExportZalohy.addActionListener(e -> handler.exportZalohy());
        menuItemImportZalohy.addActionListener(e -> handler.importZalohy());

        menuZaloha.add(menuItemExportZalohy);
        menuZaloha.add(menuItemImportZalohy);

        JMenu menuNapoveda = new JMenu("Nápověda");
        JMenuItem menuItemOAplikaci = new JMenuItem("O aplikaci");
        menuItemOAplikaci.addActionListener(e -> handler.oAplikaci());
        menuNapoveda.add(menuItemOAplikaci);

        add(menuSoubor);
        add(menuZaloha);
        add(menuNapoveda);
    }

    public void aktualizujStav(boolean jePrihlasen, boolean jeAdmin, boolean nacitaSe) {
        if (nacitaSe) {
            menuItemPrihlasit.setVisible(false);
            menuItemOdhlasit.setVisible(false);
            menuItemZmenitHeslo.setVisible(false);
            menuItemExportZalohy.setVisible(false);
            menuItemImportZalohy.setVisible(false);
            menuZaloha.setEnabled(false);
            menuZaloha.setToolTipText("Načítám data...");
        } else {
            menuItemPrihlasit.setVisible(!jePrihlasen);
            menuItemOdhlasit.setVisible(jePrihlasen);
            menuItemZmenitHeslo.setVisible(jePrihlasen);
            
            menuItemExportZalohy.setVisible(jeAdmin);
            menuItemImportZalohy.setVisible(jeAdmin);
            menuZaloha.setEnabled(jeAdmin);
            menuZaloha.setToolTipText(jeAdmin ? null : "Pouze pro administrátora");
        }
    }

    public interface MenuActionHandler {
        void prihlasit();
        void odhlasit();
        void zmenitHeslo();
        void prepnoutDatabazi();
        void ukoncitAplikaci();
        void exportZalohy();
        void importZalohy();
        void oAplikaci();
    }
}