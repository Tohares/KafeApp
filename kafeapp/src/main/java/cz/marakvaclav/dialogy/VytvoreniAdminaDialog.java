package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Admin;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Úvodní inicializační dialog, který se zobrazí pouze při historicky prvním spuštění aplikace.
 * Slouží k vytvoření nepostradatelného administrátorského účtu a zadání bankovních údajů,
 * na které budou ostatní uživatelé zasílat své platby za vypitou kávu.
 */
public class VytvoreniAdminaDialog extends JDialog {
    private JTextField textFieldLogin;
    private JPasswordField passwordFieldHesloFirst;
    private JPasswordField passwordFieldHesloSecond;
    private JTextField textFieldIban;
    private JTextField textFieldCz;
    private boolean succeeded;
    private Admin admin;
    
    public interface AdminCreator {
        Admin create(String login, char[] h1, char[] h2, String iban, String cz);
    }

    public VytvoreniAdminaDialog(Frame parent, AdminCreator creator) {
        super(parent, "První spuštění - Vytvoření administrátora", true);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        panel.add(new JLabel("Login administrátora:"));
        textFieldLogin = new JTextField(15);
        panel.add(textFieldLogin);

        panel.add(new JLabel("Heslo:"));
        passwordFieldHesloFirst = new JPasswordField(15);
        panel.add(passwordFieldHesloFirst);

        panel.add(new JLabel("Heslo znovu:"));
        passwordFieldHesloSecond = new JPasswordField(15);
        panel.add(passwordFieldHesloSecond);

        panel.add(new JLabel("Číslo účtu (IBAN):"));
        textFieldIban = new JTextField(15);
        panel.add(textFieldIban);

        panel.add(new JLabel("Číslo účtu (CZ):"));
        textFieldCz = new JTextField(15);
        panel.add(textFieldCz);

        JLabel warningLabelCz = new JLabel("<html>&nbsp;<br>&nbsp;</html>");
        warningLabelCz.setForeground(new Color(220, 53, 69)); // Varovná červená
        warningLabelCz.setFont(new Font("Arial", Font.PLAIN, 11));
        warningLabelCz.setHorizontalAlignment(SwingConstants.RIGHT); // Zarovnáme doprava pod textové pole

        textFieldIban.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            private void update() {
                String iban = textFieldIban.getText().replaceAll("\\s+", "").toUpperCase();
                if (iban.startsWith("CZ") && iban.length() == 24) {
                    String vygenerovanyUcet = Admin.getCzAccountFromIban(iban);
                    if (!vygenerovanyUcet.isEmpty()) {
                        textFieldCz.setText(vygenerovanyUcet);
                        warningLabelCz.setText("<html>&nbsp;<br>&nbsp;</html>");
                    }
                } else if (!iban.isEmpty() && !iban.startsWith("CZ")) {
                    warningLabelCz.setText("<html>Za shodu CZ účtu se zahraničním<br>IBANem ručí administrátor.</html>");
                } else {
                    warningLabelCz.setText("<html>&nbsp;<br>&nbsp;</html>");
                }
            }
        });

        JButton btnOk = new JButton("Vytvořit administrátora");
        JButton btnStorno = new JButton("Ukončit aplikaci");

        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> {
            try {
                admin = creator.create(textFieldLogin.getText(), passwordFieldHesloFirst.getPassword(), passwordFieldHesloSecond.getPassword(), textFieldIban.getText(), textFieldCz.getText());
                succeeded = true;
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnStorno.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnOk);
        buttonPanel.add(btnStorno);

        // Formulář i štítek obalíme do hlavního čistého panelu
        JPanel mainContent = new JPanel(new BorderLayout(0, 5));
        mainContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainContent.add(panel, BorderLayout.NORTH);
        mainContent.add(warningLabelCz, BorderLayout.CENTER);

        getContentPane().add(mainContent, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Nelze zavřít křížkem - dokud není admin vytvořen, aplikace nemá smysl
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Zabrání křížku
        pack(); 
        setLocationRelativeTo(parent);
    }

    public Admin getAdmin() { return admin; }
    public boolean isSucceeded() { return succeeded; }
}