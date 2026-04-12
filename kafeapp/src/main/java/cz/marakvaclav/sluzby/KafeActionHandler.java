package cz.marakvaclav.sluzby;

import cz.marakvaclav.dialogy.*;
import cz.marakvaclav.entity.Kafar;
import cz.marakvaclav.entity.PolozkaSkladu;
import cz.marakvaclav.entity.Surovina;
import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.panely.*;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class KafeActionHandler {
    private final KafeUIController controller;
    private final KafeGui gui;
    private final KafeDialogManager dialogManager;

    public KafeActionHandler(KafeUIController controller, KafeGui gui, KafeDialogManager dialogManager) {
        this.controller = controller;
        this.gui = gui;
        this.dialogManager = dialogManager;
    }
    
    public void bindEvents(KafariPanel kafariPanel, SkladPanel skladPanel, UctenkyPanel uctenkyPanel,
                           UserToolbarPanel userToolbarPanel, AdminToolbarPanel adminToolbarPanel,
                           JButton vypitButton, JButton odebratButton) {

        kafariPanel.pridejSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = kafariPanel.maVybranehoKafare();
                adminToolbarPanel.setResetHeslaEnabled(rowSelected);
                adminToolbarPanel.setDeaktivovatUzivateleEnabled(rowSelected);
            }
        });

        kafariPanel.setOnPocetKavZmenen((login, novyPocet) -> {
            if (login == null) {
                dialogManager.zobrazChybu("Zadejte prosím platné číslo!");
                akcePrepnoutNaKafare(); 
            } else {
                controller.zmenitPocetKav(login, novyPocet);
            }
        });

        skladPanel.pridejSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                adminToolbarPanel.setUpravitPolozkuEnabled(skladPanel.maVybranouPolozku());
            }
        });

        uctenkyPanel.pridejSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Vyuctovani v = uctenkyPanel.getVybraneVyuctovani();
                if (v != null) {
                    if (controller.getAdmin() != null && v.getLogin().equals(controller.getAdmin().getLogin())) {
                        if (controller.jeVyuctovaniCastecneZaplaceno(v)) {
                            adminToolbarPanel.setStornovatVyuctovaniEnabled(false);
                            adminToolbarPanel.setStornovatVyuctovaniTooltip("Nelze stornovat, protože 1 nebo více kafařů už zaplatilo.");
                        } else {
                            adminToolbarPanel.setStornovatVyuctovaniEnabled(true);
                            adminToolbarPanel.setStornovatVyuctovaniTooltip(null);
                        }
                    } else {
                        adminToolbarPanel.setStornovatVyuctovaniEnabled(false);
                        adminToolbarPanel.setStornovatVyuctovaniTooltip("Funguje pouze pro admin celkový součet.");
                    }
                } else {
                    adminToolbarPanel.setStornovatVyuctovaniEnabled(false);
                    adminToolbarPanel.setStornovatVyuctovaniTooltip(null);
                }
            }
        });

        userToolbarPanel.setPrihlasitAction(e -> akcePrihlasit());
        userToolbarPanel.setOdhlasitAction(e -> akceOdhlasit());
        userToolbarPanel.setZmenitHesloAction(e -> akceZmenitHeslo());
        userToolbarPanel.setZalozitUzivateleAction(e -> akceZalozitUzivatele());
        userToolbarPanel.setExportHistorieAction(e -> akceExportHistorieKafare());

        adminToolbarPanel.setKafariAction(e -> akcePrepnoutNaKafare());
        adminToolbarPanel.setSkladAction(e -> akcePrepnoutNaSklad());
        adminToolbarPanel.setNaskladnitAction(e -> akceNaskladnit());
        adminToolbarPanel.setUpravitPolozkuAction(e -> akceUpravitPolozku());
        adminToolbarPanel.setVyuctovatAction(e -> akceVyuctovat());
        adminToolbarPanel.setStornovatVyuctovaniAction(e -> akceStornovatVyuctovani());
        adminToolbarPanel.setUctenkyAction(e -> akceUctenky());
        adminToolbarPanel.setResetHeslaAction(e -> akceResetHesla());
        adminToolbarPanel.setDeaktivovatUzivateleAction(e -> akceDeaktivovatUzivatele());
        adminToolbarPanel.setObnovitUzivateleAction(e -> akceObnovitUzivatele());

        vypitButton.addActionListener(e -> akceVypitKavu());
        odebratButton.addActionListener(e -> controller.odebratKavu());
    }

    public KafeMenuBar.MenuActionHandler getMenuActionHandler() {
        return new KafeMenuBar.MenuActionHandler() {
            @Override public void prihlasit() { akcePrihlasit(); }
            @Override public void odhlasit() { akceOdhlasit(); }
            @Override public void zmenitHeslo() { akceZmenitHeslo(); }
            @Override public void prepnoutDatabazi() { controller.prepnoutDatabazi(); }
            @Override public void ukoncitAplikaci() { controller.ukonceniAplikace(); }
            @Override public void exportZalohy() { akceExportZalohy(); }
            @Override public void importZalohy() { akceImportZalohy(); }
            @Override public void oAplikaci() { akceOAplikaci(); }
        };
    }

    public void akceExportHistorieKafare() {
        controller.zpracujExportHistorie();
    }

    public void akceExportZalohy() {
        ExportHistorieVsehoDialog dialog = new ExportHistorieVsehoDialog(gui, controller);
        dialog.setVisible(true);
    }

    public void akceImportZalohy() {
        ImportHistorieVsehoDialog dialog = new ImportHistorieVsehoDialog(gui, controller);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            gui.updateView();
        }
    }

    public void akceOAplikaci() {
        JOptionPane.showMessageDialog(gui, "KafeApp v1.0\nAplikace pro evidenci spotřeby kávy, mléka, cukru, kyseliny citronové a vyúčtování.\n\nAutor: Václav Mařák", "O aplikaci", JOptionPane.INFORMATION_MESSAGE);
    }

    public void akceNaskladnit() {
        if (controller.isAdmin()) {
            PolozkaDialog dialog = new PolozkaDialog(gui, null);
            dialog.setVisible(true);

            if (dialog.isSucceeded()) {
                controller.naskladnit(dialog.getPolozka());
            }
        }
        gui.zobrazPanelSkladu();
    }

    public void akceUpravitPolozku() {
        if (gui.maVybranouPolozku()) {
            int id = gui.getVybraneIdPolozky();
            PolozkaSkladu vybrana = controller.getSklad().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
            if (vybrana == null) return;
            PolozkaDialog dialog = new PolozkaDialog(gui, vybrana);
            dialog.setVisible(true);
            if (dialog.isDeleted()) {
                controller.smazatPolozkuSkladu(vybrana);
                gui.zobrazPanelSkladu();
            } else if (dialog.isSucceeded()) {
                controller.upravitPolozkuSkladu(vybrana);
                gui.zobrazPanelSkladu();
            }
        }
    }

    public void akceVyuctovat() {
        if (!controller.maNecoKVyuctovani()) {
            dialogManager.zobrazInformaci("Není co vyúčtovat. Celkový počet vypitých káv je 0.");
            return;
        }
        if (controller.isAdmin()) {
            VyuctovaniDialog vyuctovaniDialog = new VyuctovaniDialog(gui, 
                controller.getAgregovanaPolozka(Surovina.KAFE),
                controller.getAgregovanaPolozka(Surovina.MLEKO),
                controller.getAgregovanaPolozka(Surovina.CUKR),
                controller.getAgregovanaPolozka(Surovina.KYS_CITRONOVA),
                (kafe, mleko, cukr, citr) -> {
                    controller.zpracujVyuctovani(kafe, mleko, cukr, citr);
                });
            vyuctovaniDialog.setVisible(true);
        }
        controller.reloadVyuctovani();
        gui.zobrazPanelUctenek();        
    }

    public void akceStornovatVyuctovani() {
        Vyuctovani v = gui.getVybraneVyuctovani();
        if (v != null) {
            if (v.getLogin().equals(controller.getAdmin().getLogin())) {
                if (controller.jeVyuctovaniCastecneZaplaceno(v)) {
                    dialogManager.zobrazChybu("Nelze stornovat! Některé účtenky z tohoto vyúčtování již byly zaplaceny.");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(gui, 
                    "Opravdu chcete stornovat toto hromadné vyúčtování?\nKafařům se vrátí káva k úhradě a spotřebované suroviny se vloží zpět na sklad.", 
                    "Potvrdit storno", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.stornovatVyuctovani(v);
                    dialogManager.zobrazInformaci("Vyúčtování bylo úspěšně stornováno.");
                    gui.zobrazPanelUctenek(); // refresh tabulky (Pokud se storno nepodaří, kód sem nikdy nedojde!)
                }
            }
        }
    }

    public void akceResetHesla() {
        String login = gui.getVybranyLogin();
        if (login != null) {
            ResetHeslaDialog dialog = new ResetHeslaDialog(gui, login);
            dialog.setVisible(true);
            
            if (dialog.isSucceeded()) {
                char[] noveH = dialog.getNoveHeslo();
                try {
                    controller.resetovatHesloKafare(login, noveH);
                    dialogManager.zobrazInformaci("Heslo uživatele " + login + " bylo úspěšně změněno.\nPři dalším přihlášení bude vyzván k nastavení vlastního hesla.");
                } finally {
                    if (noveH != null) Arrays.fill(noveH, '0');
                }
            }
        }
    }

    public void akceDeaktivovatUzivatele() {
        String login = gui.getVybranyLogin();
        if (login != null) {
            int confirm = JOptionPane.showConfirmDialog(gui, 
                "Opravdu chcete nenávratně deaktivovat uživatele " + login + "?\nJeho historie účtenek zůstane zachována, ale z přehledu zmizí a ztratí přístup do aplikace.", 
                "Potvrdit smazání", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.deaktivovatKafare(login);
                dialogManager.zobrazInformaci("Uživatel " + login + " byl deaktivován.");
                gui.zobrazPanelKafaru();
            }
        }
    }

    public void akceObnovitUzivatele() {
        List<Kafar> smazani = controller.getDeaktivovaniKafari();
        if (smazani.isEmpty()) {
            dialogManager.zobrazInformaci("Žádní smazaní uživatelé k obnovení.");
            return;
        }
        
        String[] moznosti = smazani.stream().map(Kafar::getLogin).toArray(String[]::new);
        String vybrany = (String) JOptionPane.showInputDialog(
            gui, "Vyberte uživatele k obnovení:", "Obnovit uživatele",
            JOptionPane.QUESTION_MESSAGE, null, moznosti, moznosti[0]
        );
        
        if (vybrany != null) {
            controller.obnovitKafare(vybrany);
            dialogManager.zobrazInformaci("Uživatel " + vybrany + " byl úspěšně obnoven.");
            gui.zobrazPanelKafaru();
        }
    }

    public void akceVypitKavu() {
        controller.vypitKavu();
    }        
    
    public void akceUctenky() {
        gui.zobrazPanelUctenek();
    }

    public void akcePrihlasit() {
        LoginDialog loginDialog = new LoginDialog(gui);
        loginDialog.setVisible(true);

        if (loginDialog.isSucceeded()) {
            char[] heslo = loginDialog.getHeslo();
            try {
                controller.zpracujPrihlaseni(loginDialog.getLogin(), heslo);
            } finally {
                if (heslo != null) Arrays.fill(heslo, '0');
            }
        }
    }

    public void akceOdhlasit() {
        controller.odhlasit();
    }

    public void akceZmenitHeslo() {
        ZmenaHeslaDialog dialog = new ZmenaHeslaDialog(gui);
        dialog.setVisible(true);
        
        if (dialog.isSucceeded()) {
            char[] stareH = dialog.getStareHeslo();
            char[] noveH = dialog.getNoveHeslo();
            try {
                controller.zmenitHeslo(stareH, noveH);
                dialogManager.zobrazInformaci("Heslo bylo úspěšně změněno.");
            } finally {
                if (stareH != null) Arrays.fill(stareH, '0');
                if (noveH != null) Arrays.fill(noveH, '0');
            }
        }
    }

    public void akceZalozitUzivatele() {
        NewUserDialog newUserDialog = new NewUserDialog(gui, controller.getKafari());
        newUserDialog.setVisible(true);

        if (newUserDialog.isSucceeded()) {
            char[] heslo = newUserDialog.getHeslo();
            try {
                controller.zalozitUzivatele(newUserDialog.getLogin(), heslo);
            } finally {
                if (heslo != null) Arrays.fill(heslo, '0');
            }
        }
    }

    public void akcePrepnoutNaKafare() {
        gui.zobrazPanelKafaru();
    }

    public void akcePrepnoutNaSklad() {
        gui.zobrazPanelSkladu();
    }
}