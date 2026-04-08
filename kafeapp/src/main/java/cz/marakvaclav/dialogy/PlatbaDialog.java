package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Admin;
import cz.marakvaclav.entity.Vyuctovani;

import java.awt.*;
import javax.swing.*;

/**
 * Dialog zobrazující detailní platební údaje ke konkrétnímu vyúčtování.
 * Generuje a zobrazuje QR kód (ve formátu SPAYD) pro snadnou úhradu z mobilního bankovnictví.
 * Pokud okno otevře administrátor, obsahuje navíc tlačítko pro manuální potvrzení přijetí platby.
 */
public class PlatbaDialog extends JDialog {
    private boolean succeeded = false;

    public PlatbaDialog(Frame parent, Vyuctovani vyuctovani, Admin admin, String prihlasenyUzivatel) {
        super(parent, "Platební údaje", true);

        setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea txtUdaje = new JTextArea(vyuctovani.vypisPlatebniUdaje(admin));
        txtUdaje.setEditable(false);
        txtUdaje.setBackground(getBackground());
        txtUdaje.setFont(new Font("Monospaced", Font.PLAIN, 14));
        infoPanel.add(txtUdaje);

        JLabel qrLabel = new JLabel();
        qrLabel.setHorizontalAlignment(JLabel.CENTER);
        // Vygenerování SPAYD QR kódu pro okamžité zaplacení skrze bankovní aplikaci
        java.awt.image.BufferedImage qrImage = vyuctovani.vytvorQRKodProPlatbu(admin);
        if (qrImage != null) {
            qrLabel.setIcon(new ImageIcon(qrImage));
        } else {
            qrLabel.setText("QR kód se nepodařilo vygenerovat.");
        }
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(qrLabel);

        JPanel buttonPanel = new JPanel();
        JButton btnZaplaceno = new JButton("Označit jako zaplacené");
        JButton btnZavrit = new JButton("Zavřít");

        btnZaplaceno.addActionListener(e -> {
            succeeded = true;
            dispose();
        });

        btnZavrit.addActionListener(e -> dispose());

        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            buttonPanel.add(btnZaplaceno);
        }
        buttonPanel.add(btnZavrit);

        add(infoPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
