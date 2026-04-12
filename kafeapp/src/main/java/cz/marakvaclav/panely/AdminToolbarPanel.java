package cz.marakvaclav.panely;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AdminToolbarPanel extends JPanel {
    private JButton kafariButton;
    private JButton skladButton;
    private JButton naskladnitButton;
    private JButton upravitPolozkuButton;
    private JButton vyuctovatButton;
    private JButton stornovatVyuctovaniButton;
    private JButton uctenkyButton;
    private JButton resetHeslaButton;
    private JButton deaktivovatUzivateleButton;
    private JButton obnovitUzivateleButton;

    public AdminToolbarPanel() {
        setLayout(new GridLayout(2, 1));
        JPanel adminRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JPanel adminRow2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        kafariButton = new JButton("Kafaři");
        skladButton = new JButton("Sklad");
        uctenkyButton = new JButton("Účtenky");
        vyuctovatButton = new JButton("Vyúčtovat");
        stornovatVyuctovaniButton = new JButton("Stornovat vyúčtování");
        
        naskladnitButton = new JButton("Naskladnit");
        upravitPolozkuButton = new JButton("Změnit položku");
        resetHeslaButton = new JButton("Resetovat heslo");
        deaktivovatUzivateleButton = new JButton("Smazat uživatele");
        obnovitUzivateleButton = new JButton("Obnovit uživatele");

        upravitPolozkuButton.setEnabled(false);
        stornovatVyuctovaniButton.setEnabled(false);
        resetHeslaButton.setEnabled(false);
        
        deaktivovatUzivateleButton.setForeground(new Color(220, 53, 69));
        deaktivovatUzivateleButton.setEnabled(false);
        obnovitUzivateleButton.setEnabled(false);

        adminRow1.add(kafariButton);
        adminRow1.add(skladButton);
        adminRow1.add(uctenkyButton);
        adminRow1.add(vyuctovatButton);
        adminRow1.add(stornovatVyuctovaniButton);
        
        adminRow2.add(naskladnitButton);
        adminRow2.add(upravitPolozkuButton);
        adminRow2.add(resetHeslaButton);
        adminRow2.add(deaktivovatUzivateleButton);
        adminRow2.add(obnovitUzivateleButton);

        add(adminRow1);
        add(adminRow2);
    }

    public void setKafariAction(ActionListener l) { kafariButton.addActionListener(l); }
    public void setSkladAction(ActionListener l) { skladButton.addActionListener(l); }
    public void setNaskladnitAction(ActionListener l) { naskladnitButton.addActionListener(l); }
    public void setUpravitPolozkuAction(ActionListener l) { upravitPolozkuButton.addActionListener(l); }
    public void setVyuctovatAction(ActionListener l) { vyuctovatButton.addActionListener(l); }
    public void setStornovatVyuctovaniAction(ActionListener l) { stornovatVyuctovaniButton.addActionListener(l); }
    public void setUctenkyAction(ActionListener l) { uctenkyButton.addActionListener(l); }
    public void setResetHeslaAction(ActionListener l) { resetHeslaButton.addActionListener(l); }
    public void setDeaktivovatUzivateleAction(ActionListener l) { deaktivovatUzivateleButton.addActionListener(l); }
    public void setObnovitUzivateleAction(ActionListener l) { obnovitUzivateleButton.addActionListener(l); }

    public void setUpravitPolozkuEnabled(boolean enabled) { upravitPolozkuButton.setEnabled(enabled); }
    public void setStornovatVyuctovaniEnabled(boolean enabled) { stornovatVyuctovaniButton.setEnabled(enabled); }
    public void setResetHeslaEnabled(boolean enabled) { resetHeslaButton.setEnabled(enabled); }
    public void setDeaktivovatUzivateleEnabled(boolean enabled) { deaktivovatUzivateleButton.setEnabled(enabled); }
    public void setObnovitUzivateleEnabled(boolean enabled) { obnovitUzivateleButton.setEnabled(enabled); }
    public void setStornovatVyuctovaniTooltip(String text) { stornovatVyuctovaniButton.setToolTipText(text); }
}