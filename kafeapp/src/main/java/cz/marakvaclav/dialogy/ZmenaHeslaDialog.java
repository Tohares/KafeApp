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
            if (!Arrays.equals(passwordFieldNove1.getPassword(), passwordFieldNove2.getPassword())) {
                JOptionPane.showMessageDialog(this, "Nová hesla se neshodují!", "Chyba", JOptionPane.ERROR_MESSAGE);
                return;
            }
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
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public String getStareHeslo() { return new String(passwordFieldStare.getPassword()); }
    public String getNoveHeslo() { return new String(passwordFieldNove1.getPassword()); }
    public boolean isSucceeded() { return succeeded; }
}