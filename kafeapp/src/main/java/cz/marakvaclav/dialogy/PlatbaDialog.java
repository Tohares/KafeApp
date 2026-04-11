package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Admin;
import cz.marakvaclav.entity.Vyuctovani;

import java.awt.*;
import javax.swing.*;
import java.util.function.BiConsumer;

/**
 * Dialog zobrazující detailní platební údaje ke konkrétnímu vyúčtování.
 * Generuje a zobrazuje QR kód (ve formátu SPAYD) pro snadnou úhradu z mobilního bankovnictví.
 * Pokud okno otevře administrátor, obsahuje navíc tlačítko pro manuální potvrzení přijetí platby.
 */
public class PlatbaDialog extends JDialog {
    private boolean succeeded = false;
    private JTextArea txtUdaje;
    private JLabel qrLabel;

    public PlatbaDialog(Frame parent, Vyuctovani vyuctovani, Admin admin, String prihlasenyUzivatel, BiConsumer<String, String> onUlozitPlatebniUdaje) {
        super(parent, vyuctovani.getStavPlatby() ? "Detail platby" : "Platební údaje", true);

        setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtUdaje = new JTextArea();
        txtUdaje.setEditable(false);
        txtUdaje.setBackground(getBackground());
        txtUdaje.setFont(new Font("Monospaced", Font.PLAIN, 14));
        infoPanel.add(txtUdaje);

        qrLabel = new JLabel();
        qrLabel.setHorizontalAlignment(JLabel.CENTER);
        
        obnovZobrazeniUdaju(vyuctovani, admin);

        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(qrLabel);

        JPanel buttonPanel = new JPanel();
        JButton btnZaplaceno = new JButton("Označit jako zaplacené");
        JButton btnUpravit = new JButton("Upravit platební údaje");
        JButton btnZavrit = new JButton("Zavřít");

        btnUpravit.addActionListener(e -> {
            JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField tfIban = new JTextField(admin.getCisloUctuIBAN());
            JTextField tfCz = new JTextField(admin.getCisloUctuCZ());
            p.add(new JLabel("IBAN:")); p.add(tfIban);
            p.add(new JLabel("CZ účet:")); p.add(tfCz);
            
            int res = JOptionPane.showConfirmDialog(this, p, "Úprava platebních údajů", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String novyIban = tfIban.getText().trim();
                if (!novyIban.isEmpty() && !Admin.isValidIBAN(novyIban)) {
                    JOptionPane.showMessageDialog(this, "Neplatný formát IBAN! Změny nebyly uloženy.", "Chyba", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String novyCz = tfCz.getText().trim();
                if (!Admin.isCzAccountConsistentWithIban(novyCz, novyIban)) {
                    JOptionPane.showMessageDialog(this, "České číslo účtu neodpovídá zadanému IBANu! Změny nebyly uloženy.", "Chyba", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String cleanIban = novyIban.replaceAll("\\s+", "").toUpperCase();
                onUlozitPlatebniUdaje.accept(cleanIban, novyCz);
                obnovZobrazeniUdaju(vyuctovani, admin);
            }
        });

        btnZaplaceno.addActionListener(e -> {
            succeeded = true;
            dispose();
        });

        btnZavrit.addActionListener(e -> dispose());

        if (prihlasenyUzivatel.equals(admin.getLogin()) && !vyuctovani.getStavPlatby()) {
            buttonPanel.add(btnUpravit);
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

    private void obnovZobrazeniUdaju(Vyuctovani vyuctovani, Admin admin) {
        String textUdaju = vyuctovani.vypisPlatebniUdaje(admin);
        if (vyuctovani.getStavPlatby()) {
            textUdaju += "\n\n--------------------------------\nSTAV: UZAVŘENO A ZAPLACENO DNE " + vyuctovani.getDatumPlatby();
        }
        txtUdaje.setText(textUdaju);
        
        java.awt.image.BufferedImage qrImage = vyuctovani.vytvorQRKodProPlatbu(admin);
        if (qrImage != null) {
            qrLabel.setIcon(new ImageIcon(qrImage));
            qrLabel.setText("");
        } else {
            qrLabel.setIcon(null);
            qrLabel.setText("QR kód se nepodařilo vygenerovat.");
        }
        pack();
    }
}
