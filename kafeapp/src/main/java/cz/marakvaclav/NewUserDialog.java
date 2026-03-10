package cz.marakvaclav;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;

public class NewUserDialog extends JDialog {
    private JTextField textFieldLogin;
    private JPasswordField passwordFieldHesloFirst;
    private JPasswordField passwordFieldHesloSecond;
    private boolean succeeded;
    private List<Kafar> kafari;

    public NewUserDialog(Frame parent, List<Kafar> kafari) {
        super(parent, "Vytvoreni noveho uzivatele", true); // true nastavuje modalitu
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        panel.add(new JLabel("Login:"));
        textFieldLogin = new JTextField(15);
        panel.add(textFieldLogin);

        panel.add(new JLabel("Heslo:"));
        passwordFieldHesloFirst = new JPasswordField(15);
        panel.add(passwordFieldHesloFirst);

        panel.add(new JLabel("Heslo znovu:"));
        passwordFieldHesloSecond = new JPasswordField(15);
        panel.add(passwordFieldHesloSecond);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        this.kafari = kafari;

        btnOk.addActionListener(e -> {
            if (!Arrays.equals(passwordFieldHesloFirst.getPassword(), passwordFieldHesloSecond.getPassword())) {
                JOptionPane.showMessageDialog(this, "Hesla se neshoduji!", "Chyba", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else {
                for (Kafar k : kafari) {
                    if (k.getLogin().equals(textFieldLogin.getText())) {
                        JOptionPane.showMessageDialog(this, "Uzivatel s timto loginem uz existuje!", "Chyba", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
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
        return new String(passwordFieldHesloSecond.getPassword());
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}

