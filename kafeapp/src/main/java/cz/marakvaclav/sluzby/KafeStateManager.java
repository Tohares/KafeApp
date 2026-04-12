package cz.marakvaclav.sluzby;

import cz.marakvaclav.panely.*;

import javax.swing.*;
import java.awt.*;

/**
 * Třída zodpovědná za sestavení správného uživatelského rozhraní na základě stavu aplikace.
 * Zapouzdřuje v sobě veškerou "if-else" logiku pro zobrazení tlačítek a panelů
 * a odlehčuje tak hlavní třídě KafeGui.
 */
public class KafeStateManager {
    private final KafeUIController controller;
    private final JPanel emptyPanel;
    private final JPanel welcomePanel;
    private final KafarDashboardPanel kafarDashboardPanel;
    private final UserPanel userPanel;
    private final UctenkyPanel uctenkyPanel;
    private final UserToolbarPanel userToolbarPanel;
    private final AdminToolbarPanel adminToolbarPanel;
    private final KafeMenuBar menuBar;
    private final JPanel loadingPanel;

    public KafeStateManager(KafeUIController controller, JPanel emptyPanel, JPanel welcomePanel, 
                            KafarDashboardPanel kafarDashboardPanel, UserPanel userPanel, 
                            UctenkyPanel uctenkyPanel, UserToolbarPanel userToolbarPanel, 
                            AdminToolbarPanel adminToolbarPanel, KafeMenuBar menuBar) {
        this.controller = controller;
        this.emptyPanel = emptyPanel;
        this.welcomePanel = welcomePanel;
        this.kafarDashboardPanel = kafarDashboardPanel;
        this.userPanel = userPanel;
        this.uctenkyPanel = uctenkyPanel;
        this.userToolbarPanel = userToolbarPanel;
        this.adminToolbarPanel = adminToolbarPanel;
        this.menuBar = menuBar;

        this.loadingPanel = vytvorLoadingPanel();
    }

    private JPanel vytvorLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel loadingLabel = new JLabel("Načítám data ze sítě, čekejte prosím...");
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(loadingLabel, gbc);
        
        gbc.gridy = 1;
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        panel.add(progressBar, gbc);
        
        return panel;
    }

    /**
     * Vyhodnotí aktuální stav aplikace (přihlášený uživatel, administrátor, načítání)
     * a poskládá na hlavní panel správné komponenty.
     */
    public void renderState(boolean nacitaSe) {
        emptyPanel.removeAll();
        userToolbarPanel.skrytVse();
        adminToolbarPanel.setVisible(false);

        if (nacitaSe) {
            emptyPanel.add(loadingPanel);
            menuBar.aktualizujStav(false, false, true);
        } else {
            String prihlasenyUzivatel = controller.getPrihlasenyUzivatel();
            boolean isAdmin = controller.isAdmin();

            if (prihlasenyUzivatel == null) {
                emptyPanel.add(welcomePanel);
                userToolbarPanel.setPrihlasitVisible(true);
                userToolbarPanel.setZalozitUzivateleVisible(true);
            } else if (isAdmin) {
                userToolbarPanel.setOdhlasitVisible(true);
                userToolbarPanel.setZmenitHesloVisible(true);
                userToolbarPanel.setZalozitUzivateleVisible(true);
                adminToolbarPanel.setVisible(true);
            } else {
                int[] stat = controller.getStatistikyPrihlasenehoKafare();
                kafarDashboardPanel.pripojPanely(userPanel, uctenkyPanel);
                uctenkyPanel.obnovData(controller.getSeznamVyuctovani(), prihlasenyUzivatel);
                userPanel.obnovData(prihlasenyUzivatel, stat[0], stat[1], stat[2]);
                kafarDashboardPanel.aktualizuj(stat[0]);
                
                emptyPanel.add(kafarDashboardPanel);

                userToolbarPanel.setOdhlasitVisible(true);
                userToolbarPanel.setZmenitHesloVisible(true);
                userToolbarPanel.setExportHistorieVisible(true);
            }
            
            // Aktualizace viditelnosti položek v horním menu
            menuBar.aktualizujStav(prihlasenyUzivatel != null, isAdmin, false);
        }
    }
}