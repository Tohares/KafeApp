package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Kafar;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;

/**
 * Dialog pro registraci nového běžného uživatele (kafaře).
 * Zajišťuje základní validaci vstupů - kontroluje, zda se zadaná hesla shodují 
 * a zda uživatel se stejným loginem v systému již neexistuje.
 */
public class NewUserDialog extends JDialog {
    private String login;
    private char[] heslo;
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
            // Validace shody hesel a unikátnosti uživatelského jména
            char[] h1 = passwordFieldHesloFirst.getPassword();
            char[] h2 = passwordFieldHesloSecond.getPassword();
            if (!Arrays.equals(h1, h2)) {
                JOptionPane.showMessageDialog(this, "Hesla se neshoduji!", "Chyba", JOptionPane.ERROR_MESSAGE);
                Arrays.fill(h1, '0'); Arrays.fill(h2, '0');
                return;
            }
            else {
                for (Kafar k : kafari) {
                    if (k.getLogin().equals(textFieldLogin.getText())) {
                        JOptionPane.showMessageDialog(this, "Uzivatel s timto loginem uz existuje!", "Chyba", JOptionPane.ERROR_MESSAGE);
                        Arrays.fill(h1, '0'); Arrays.fill(h2, '0');
                        return;
                    }
                }
            }
            succeeded = true;
            this.login = textFieldLogin.getText();
            this.heslo = h1;
            Arrays.fill(h2, '0'); // Zahodíme nepotřebnou kopii, getHeslo() převezme h1
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

    public char[] getHeslo() {
        return heslo;
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
