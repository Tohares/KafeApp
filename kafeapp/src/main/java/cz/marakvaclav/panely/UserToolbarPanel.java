package cz.marakvaclav.panely;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class UserToolbarPanel extends JPanel {
    private JButton prihlasitButton;
    private JButton odhlasitButton;
    private JButton zmenitHesloButton;
    private JButton zalozitUzivateleButton;
    private JButton exportHistorieKafareButton;

    public UserToolbarPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        prihlasitButton = new JButton("Přihlásit");
        odhlasitButton = new JButton("Odhlásit");
        zmenitHesloButton = new JButton("Změnit heslo");
        zalozitUzivateleButton = new JButton("Vytvořit nového uživatele");
        exportHistorieKafareButton = new JButton("Export historie");

        add(prihlasitButton);
        add(odhlasitButton);
        add(zmenitHesloButton);
        add(zalozitUzivateleButton);
        add(exportHistorieKafareButton);
    }

    public void setPrihlasitAction(ActionListener l) { prihlasitButton.addActionListener(l); }
    public void setOdhlasitAction(ActionListener l) { odhlasitButton.addActionListener(l); }
    public void setZmenitHesloAction(ActionListener l) { zmenitHesloButton.addActionListener(l); }
    public void setZalozitUzivateleAction(ActionListener l) { zalozitUzivateleButton.addActionListener(l); }
    public void setExportHistorieAction(ActionListener l) { exportHistorieKafareButton.addActionListener(l); }

    public void setPrihlasitVisible(boolean v) { prihlasitButton.setVisible(v); }
    public void setOdhlasitVisible(boolean v) { odhlasitButton.setVisible(v); }
    public void setZmenitHesloVisible(boolean v) { zmenitHesloButton.setVisible(v); }
    public void setZalozitUzivateleVisible(boolean v) { zalozitUzivateleButton.setVisible(v); }
    public void setExportHistorieVisible(boolean v) { exportHistorieKafareButton.setVisible(v); }

    public void skrytVse() {
        prihlasitButton.setVisible(false);
        odhlasitButton.setVisible(false);
        zmenitHesloButton.setVisible(false);
        zalozitUzivateleButton.setVisible(false);
        exportHistorieKafareButton.setVisible(false);
    }
}