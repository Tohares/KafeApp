package cz.marakvaclav.panely;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard (Palubní deska) pro běžného kafaře.
 * Sdružuje informační panel uživatele, tlačítka pro záznam/storno kávy a tabulku historie účtenek.
 * Tímto se odděluje layoutová logika od hlavního okna aplikace.
 */
public class KafarDashboardPanel extends JPanel {

    private final JButton odebratButton;
    private final JPanel uctenkySekce;
    private final JPanel topUserPanel;

    public KafarDashboardPanel(UserPanel userPanel, UctenkyPanel uctenkyPanel, JButton vypitButton, JButton odebratButton) {
        this.odebratButton = odebratButton;

        setLayout(new BorderLayout(0, 10));

        uctenkySekce = new JPanel(new BorderLayout(0, 5));
        JLabel nadpisUctenky = new JLabel("Historie vyúčtování a platby");
        nadpisUctenky.setFont(new Font("Arial", Font.BOLD, 18));
        nadpisUctenky.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
        uctenkySekce.add(nadpisUctenky, BorderLayout.NORTH);
        uctenkySekce.add(uctenkyPanel, BorderLayout.CENTER);

        JPanel akcePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        akcePanel.add(vypitButton);
        akcePanel.add(odebratButton);

        topUserPanel = new JPanel(new BorderLayout());
        topUserPanel.add(userPanel, BorderLayout.CENTER);
        topUserPanel.add(akcePanel, BorderLayout.SOUTH);

        add(topUserPanel, BorderLayout.NORTH);
        add(uctenkySekce, BorderLayout.CENTER);
    }

    /**
     * Zajišťuje, že se panely správně vrátí zpět do dashboardu,
     * pokud si je mezitím "vypůjčilo" hlavní okno pro administrátorský režim.
     */
    public void pripojPanely(UserPanel userPanel, UctenkyPanel uctenkyPanel) {
        topUserPanel.add(userPanel, BorderLayout.CENTER);
        uctenkySekce.add(uctenkyPanel, BorderLayout.CENTER);
    }

    /**
     * Aktualizuje zobrazení tlačítek na základě aktuálního stavu konta kafaře.
     * 
     * @param nezuctovaneKavy Počet aktuálně nezúčtovaných káv uživatele.
     */
    public void aktualizuj(int nezuctovaneKavy) {
        odebratButton.setEnabled(nezuctovaneKavy > 0);
    }
}