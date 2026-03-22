package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Kafar;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;

public class NewUserDialog extends JDialog {
    private String login;
    private String heslo;
    private boolean succeeded;

    public NewUserDialog(Frame parent, List<Kafar> kafari) {
        super(parent, "Vytvoreni noveho uzivatele", true); // true nastavuje modalitu
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        panel.add(new JLabel("Login:"));
        JTextField textFieldLogin = new JTextField(15);
        panel.add(textFieldLogin);

        panel.add(new JLabel("Heslo:"));
        JPasswordField passwordFieldHesloFirst = new JPasswordField(15);
        panel.add(passwordFieldHesloFirst);

        panel.add(new JLabel("Heslo znovu:"));
        JPasswordField passwordFieldHesloSecond = new JPasswordField(15);
        panel.add(passwordFieldHesloSecond);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

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
            this.login = textFieldLogin.getText();
            this.heslo = new String(passwordFieldHesloSecond.getPassword());
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
        return login;
    }

    public String getHeslo() {
        return heslo;
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
