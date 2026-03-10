package cz.marakvaclav;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField textFieldLogin;
    private JPasswordField passwordFieldHeslo;
    private boolean succeeded;

    public LoginDialog(Frame parent) {
        super(parent, "Prihlaseni", true); // true nastavuje modalitu
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Login:"));
        textFieldLogin = new JTextField(15);
        panel.add(textFieldLogin);

        panel.add(new JLabel("Heslo:"));
        passwordFieldHeslo = new JPasswordField(15);
        panel.add(passwordFieldHeslo);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        btnOk.addActionListener(e -> {
            succeeded = true;
            dispose(); // Zavre dialog
        });

        btnStorno.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        panel.add(btnOk);
        panel.add(btnStorno);

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(panel, BorderLayout.CENTER);

        pack(); 
        setLocationRelativeTo(parent);
    }

    public String getLogin() {
        return textFieldLogin.getText();
    }

    public String getHeslo() {
        return new String(passwordFieldHeslo.getPassword());
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
