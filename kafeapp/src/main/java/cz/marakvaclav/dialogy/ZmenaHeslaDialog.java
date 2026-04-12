package cz.marakvaclav.dialogy;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Dialog pro změnu hesla aktuálně přihlášeného uživatele.
 * Vyžaduje pro potvrzení bezpečně zadat původní heslo a zopakovat zadání nového, 
 * čímž se předchází překlepům při jeho změně.
 */
public class ZmenaHeslaDialog extends JDialog {
    private JPasswordField passwordFieldStare;
    private JPasswordField passwordFieldNove1;
    private JPasswordField passwordFieldNove2;
    private char[] stareHeslo;
    private char[] noveHeslo;
    private boolean succeeded;

    public ZmenaHeslaDialog(Frame parent) {
        super(parent, "Změna hesla", true);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Staré heslo:"));
        passwordFieldStare = new JPasswordField(15);
        panel.add(passwordFieldStare);

        panel.add(new JLabel("Nové heslo:"));
        passwordFieldNove1 = new JPasswordField(15);
        panel.add(passwordFieldNove1);

        panel.add(new JLabel("Nové heslo znovu:"));
        passwordFieldNove2 = new JPasswordField(15);
        panel.add(passwordFieldNove2);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> {
            char[] oldH = passwordFieldStare.getPassword();
            char[] h1 = passwordFieldNove1.getPassword();
            char[] h2 = passwordFieldNove2.getPassword();
            if (!Arrays.equals(h1, h2)) {
                JOptionPane.showMessageDialog(this, "Nová hesla se neshodují!", "Chyba", JOptionPane.ERROR_MESSAGE);
                Arrays.fill(oldH, '0'); Arrays.fill(h1, '0'); Arrays.fill(h2, '0');
                return;
            }
            succeeded = true;
            this.stareHeslo = oldH;
            this.noveHeslo = h1;
            Arrays.fill(h2, '0');
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
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public char[] getStareHeslo() { return stareHeslo; }
    public char[] getNoveHeslo() { return noveHeslo; }
    public boolean isSucceeded() { return succeeded; }
}