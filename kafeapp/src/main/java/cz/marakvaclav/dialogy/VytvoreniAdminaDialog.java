package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Admin;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class VytvoreniAdminaDialog extends JDialog {
    private JTextField textFieldLogin;
    private JPasswordField passwordFieldHesloFirst;
    private JPasswordField passwordFieldHesloSecond;
    private JTextField textFieldIban;
    private JTextField textFieldCz;
    private boolean succeeded;
    private Admin admin;

    public VytvoreniAdminaDialog(Frame parent) {
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

        JButton btnOk = new JButton("Vytvořit administrátora");
        JButton btnStorno = new JButton("Ukončit aplikaci");

        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> {
            if (textFieldLogin.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Login nesmí být prázdný!", "Chyba", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Arrays.equals(passwordFieldHesloFirst.getPassword(), passwordFieldHesloSecond.getPassword())) {
                JOptionPane.showMessageDialog(this, "Hesla se neshodují!", "Chyba", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (passwordFieldHesloFirst.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Heslo nesmí být prázdné!", "Chyba", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            admin = new Admin(textFieldLogin.getText().trim(), new String(passwordFieldHesloFirst.getPassword()), textFieldIban.getText().trim(), textFieldCz.getText().trim());
            succeeded = true;
            dispose();
        });

        btnStorno.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnOk);
        buttonPanel.add(btnStorno);

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Zabrání křížku
        pack(); 
        setLocationRelativeTo(parent);
    }

    public Admin getAdmin() { return admin; }
    public boolean isSucceeded() { return succeeded; }
}