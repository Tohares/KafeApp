package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Admin;
import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.sluzby.QRGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.function.BiConsumer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Dialog zobrazující detailní platební údaje ke konkrétnímu vyúčtování.
 * Generuje a zobrazuje QR kód (ve formátu SPAYD) pro snadnou úhradu z mobilního bankovnictví.
 * Pokud okno otevře administrátor, obsahuje navíc tlačítko pro manuální potvrzení přijetí platby.
 */
public class PlatbaDialog extends JDialog {
    private boolean succeeded = false;
    private JTextArea txtUdaje;
    private JLabel qrLabel;

    public PlatbaDialog(Frame parent, Vyuctovani vyuctovani, Admin admin, String prihlasenyUzivatel, BiConsumer<String, String> onUlozitPlatebniUdaje, Runnable onStornoPlatby, Runnable onOznamitPlatbu) {
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
            
            JLabel warningLabelCz = new JLabel("<html>&nbsp;<br>&nbsp;</html>");
            warningLabelCz.setForeground(new Color(220, 53, 69));
            warningLabelCz.setFont(new Font("Arial", Font.PLAIN, 11));
            warningLabelCz.setHorizontalAlignment(SwingConstants.RIGHT); // Zarovnáme doprava

            p.add(new JLabel("IBAN:")); p.add(tfIban);
            p.add(new JLabel("CZ účet:")); p.add(tfCz);
            
            JPanel dialogContent = new JPanel(new BorderLayout(0, 5));
            dialogContent.add(p, BorderLayout.NORTH);
            dialogContent.add(warningLabelCz, BorderLayout.CENTER);
            
            DocumentListener ibanListener = new DocumentListener() {
                public void changedUpdate(DocumentEvent ev) { update(); }
                public void removeUpdate(DocumentEvent ev) { update(); }
                public void insertUpdate(DocumentEvent ev) { update(); }
                private void update() {
                    String iban = tfIban.getText().replaceAll("\\s+", "").toUpperCase();
                    if (iban.startsWith("CZ") && iban.length() == 24) {
                        String vygenerovanyUcet = Admin.getCzAccountFromIban(iban);
                        if (!vygenerovanyUcet.isEmpty()) {
                            tfCz.setText(vygenerovanyUcet);
                            warningLabelCz.setText("<html>&nbsp;<br>&nbsp;</html>");
                        }
                    } else if (!iban.isEmpty() && !iban.startsWith("CZ")) {
                        warningLabelCz.setText("<html>Za shodu účtu se zahraničním<br>IBANem ručí administrátor.</html>");
                    } else {
                        warningLabelCz.setText("<html>&nbsp;<br>&nbsp;</html>");
                    }
                }
            };
            tfIban.getDocument().addDocumentListener(ibanListener);
            
            // Inicializace varování pro již uložený zahraniční IBAN
            String initialIban = tfIban.getText().replaceAll("\\s+", "").toUpperCase();
            if (!initialIban.isEmpty() && !initialIban.startsWith("CZ")) {
                warningLabelCz.setText("<html>Za shodu CZ účtu se zahraničním<br>IBANem ručí administrátor.</html>");
            }
            
            int res = JOptionPane.showConfirmDialog(this, dialogContent, "Úprava platebních údajů", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                try {
                    onUlozitPlatebniUdaje.accept(tfIban.getText().trim(), tfCz.getText().trim());
                    obnovZobrazeniUdaju(vyuctovani, admin);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnZaplaceno.addActionListener(e -> {
            succeeded = true;
            dispose();
        });

        JButton btnStornoPlatby = new JButton("Stornovat platbu");
        btnStornoPlatby.setForeground(new Color(220, 53, 69));
        
        btnStornoPlatby.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Opravdu chcete stornovat tuto úhradu?", "Storno platby", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                onStornoPlatby.run();
                succeeded = false;
                dispose();
            }
        });

        JButton btnOznamitPlatbu = new JButton("Odeslal jsem platbu");
        btnOznamitPlatbu.setBackground(new Color(255, 250, 200));
        btnOznamitPlatbu.addActionListener(e -> {
            onOznamitPlatbu.run();
            succeeded = false;
            dispose();
        });

        btnZavrit.addActionListener(e -> dispose());

        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            if (!vyuctovani.getStavPlatby()) {
                buttonPanel.add(btnUpravit);
                buttonPanel.add(btnZaplaceno);
            } else {
                buttonPanel.add(btnStornoPlatby);
            }
        } else {
            if (!vyuctovani.getStavPlatby() && !vyuctovani.isOznamenoJakoZaplacene()) {
                buttonPanel.add(btnOznamitPlatbu);
            }
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
        
        String spaydRetezec = vyuctovani.generujSpaydRetezec(admin);
        BufferedImage qrImage = QRGenerator.generujQR(spaydRetezec);
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
