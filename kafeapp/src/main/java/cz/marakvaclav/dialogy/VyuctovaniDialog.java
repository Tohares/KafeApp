package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.PolozkaSkladu;
import cz.marakvaclav.entity.Surovina;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog pro vytvoření hromadného vyúčtování.
 * Umožňuje zadat spotřebované množství surovin a automaticky validuje, zda je dostatek zásob na skladě.
 */
public class VyuctovaniDialog extends JDialog {
    private boolean succeeded;
    
    public interface VyuctovaniHandler {
        void zpracuj(int kafe, int mleko, int cukr, int citr);
    }

    public VyuctovaniDialog(Frame parent, PolozkaSkladu skladKafe, PolozkaSkladu skladMleko, PolozkaSkladu skladCukr, PolozkaSkladu skladCitr, VyuctovaniHandler handler) {
        super(parent, "Vyúčtovat", true);
        succeeded = false;
    
        JPanel panel = new JPanel(new GridLayout(5, 4, 10, 10));

        panel.add(new JLabel("Surovina"));
        panel.add(new JLabel("Na skladě"));
        panel.add(new JLabel("Jednotka balení"));
        panel.add(new JLabel("Spotřeba"));

        panel.add(new JLabel(Surovina.KAFE.getNazev()));
        panel.add(new JLabel(String.valueOf(skladKafe.getAktualniMnozstvi())));
        panel.add(new JLabel(skladKafe.getJednotka()));
        JTextField textFieldKafe = new JTextField(15);
        panel.add(textFieldKafe);

        panel.add(new JLabel(Surovina.MLEKO.getNazev()));
        panel.add(new JLabel(String.valueOf(skladMleko.getAktualniMnozstvi())));
        panel.add(new JLabel(skladMleko.getJednotka()));
        JTextField textFieldMleko = new JTextField(15);
        panel.add(textFieldMleko);

        panel.add(new JLabel(Surovina.CUKR.getNazev()));
        panel.add(new JLabel(String.valueOf(skladCukr.getAktualniMnozstvi())));
        panel.add(new JLabel(skladCukr.getJednotka()));
        JTextField textFieldCukr = new JTextField(15);
        panel.add(textFieldCukr);

        panel.add(new JLabel(Surovina.KYS_CITRONOVA.getNazev()));
        panel.add(new JLabel(String.valueOf(skladCitr.getAktualniMnozstvi())));
        panel.add(new JLabel(skladCitr.getJednotka()));
        JTextField textFieldCitr = new JTextField(15);
        panel.add(textFieldCitr);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        btnOk.addActionListener(e -> {
            try{
                int mnozstviKafe = parseMnozstvi(textFieldKafe.getText());
                int mnozstviMleka = parseMnozstvi(textFieldMleko.getText());
                int mnozstviCukr = parseMnozstvi(textFieldCukr.getText());
                int mnozstviCitr = parseMnozstvi(textFieldCitr.getText());

                // Dialog už neprovádí byznys logiku, pouze ji deleguje přes handler. Controller sám v případě nedostatku surovin vyhodí výjimku.
                handler.zpracuj(mnozstviKafe, mnozstviMleka, mnozstviCukr, mnozstviCitr);
                succeeded = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neplatný formát vstupních dat.", "Chyba", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Nedostatek na skladě", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnStorno.addActionListener(e -> {
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnOk);
        buttonPanel.add(btnStorno);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    private int parseMnozstvi(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        int hodnota = Integer.parseInt(text.trim());
        if (hodnota < 0) {
            throw new NumberFormatException("Záporné hodnoty nejsou povoleny.");
        }
        return hodnota;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

}
