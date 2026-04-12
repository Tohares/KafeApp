package cz.marakvaclav.dialogy;

import javax.swing.*;
import java.awt.*;

import java.util.Arrays;
/**
 * Dialog pro resetování hesla kafaře administrátorem.
 */
public class ResetHeslaDialog extends JDialog {
    private JPasswordField passwordFieldNove1;
    private JPasswordField passwordFieldNove2;
    private char[] noveHeslo;
    private boolean succeeded = false;

    public ResetHeslaDialog(Frame parent, String login) {
        super(parent, "Zadejte nové heslo pro uživatele " + login, true);
        
        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        passwordFieldNove1 = new JPasswordField(15);
        passwordFieldNove2 = new JPasswordField(15);
        p.add(new JLabel("Nové heslo:")); 
        p.add(passwordFieldNove1);
        p.add(new JLabel("Nové heslo znovu:")); 
        p.add(passwordFieldNove2);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");
        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> {
            char[] h1 = passwordFieldNove1.getPassword();
            char[] h2 = passwordFieldNove2.getPassword();
            if (h1.length == 0) {
                JOptionPane.showMessageDialog(this, "Heslo nesmí být prázdné!", "Chyba", JOptionPane.ERROR_MESSAGE);
                Arrays.fill(h1, '0'); Arrays.fill(h2, '0');
            } else if (!Arrays.equals(h1, h2)) {
                JOptionPane.showMessageDialog(this, "Zadaná hesla se neshodují!", "Chyba", JOptionPane.ERROR_MESSAGE);
                Arrays.fill(h1, '0'); Arrays.fill(h2, '0');
            } else {
                succeeded = true;
                this.noveHeslo = h1;
                Arrays.fill(h2, '0');
                dispose();
            }
        });

        btnStorno.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnOk);
        buttonPanel.add(btnStorno);

        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(p, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public char[] getNoveHeslo() { return noveHeslo; }
    public boolean isSucceeded() { return succeeded; }
}