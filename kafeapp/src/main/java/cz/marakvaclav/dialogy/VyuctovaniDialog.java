package cz.marakvaclav.dialogy;

import cz.marakvaclav.sluzby.KafeController;

import javax.swing.*;
import java.awt.*;

public class VyuctovaniDialog extends JDialog {
    private boolean succeeded;

    public VyuctovaniDialog(Frame parent, KafeController controller) {
        super(parent, "Vyúčtovat", true);
        succeeded = false;
    
        JPanel panel = new JPanel(new GridLayout(5, 4, 10, 10));

        panel.add(new JLabel("Surovina"));
        panel.add(new JLabel("Na skladě"));
        panel.add(new JLabel("Jednotka balení"));
        panel.add(new JLabel("Spotřeba"));

        panel.add(new JLabel("Káva"));
        panel.add(new JLabel(String.valueOf(controller.getAgregovanaPolozka("Kafe").getAktualniMnozstvi())));
        panel.add(new JLabel(controller.getAgregovanaPolozka("Kafe").getJednotka()));
        JTextField textFieldKafe = new JTextField(15);
        panel.add(textFieldKafe);

        panel.add(new JLabel("Mléko"));
        panel.add(new JLabel(String.valueOf(controller.getAgregovanaPolozka("Mleko").getAktualniMnozstvi())));
        panel.add(new JLabel(controller.getAgregovanaPolozka("Mleko").getJednotka()));
        JTextField textFieldMleko = new JTextField(15);
        panel.add(textFieldMleko);

        panel.add(new JLabel("Cukr"));
        panel.add(new JLabel(String.valueOf(controller.getAgregovanaPolozka("Cukr").getAktualniMnozstvi())));
        panel.add(new JLabel(controller.getAgregovanaPolozka("Cukr").getJednotka()));
        JTextField textFieldCukr = new JTextField(15);
        panel.add(textFieldCukr);

        panel.add(new JLabel("Kys. citronová"));
        panel.add(new JLabel(String.valueOf(controller.getAgregovanaPolozka("Kys. Citr.").getAktualniMnozstvi())));
        panel.add(new JLabel(controller.getAgregovanaPolozka("Kys. Citr.").getJednotka()));
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

                controller.zpracujVyuctovani(mnozstviKafe, mnozstviMleka, mnozstviCukr, mnozstviCitr);
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
