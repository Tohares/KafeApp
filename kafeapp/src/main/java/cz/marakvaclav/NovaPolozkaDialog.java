package cz.marakvaclav;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class NovaPolozkaDialog extends JDialog{
    private JTextField textFieldKoupeneMnozstvi;
    private JTextField textFieldCena;
    private JTextField textFieldMena;
    private PolozkaSkladu polozka;
    private boolean succeeded;

    public NovaPolozkaDialog(Frame parent) {
        super(parent, "Naskladnit novou položku", true);
        
        succeeded = false;
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Název:"));
        String[] nazvy = {"Kafe", "Mleko", "Cukr", "Kys. Citr."};
        JComboBox<String> comboBoxNazev = new JComboBox<>(nazvy);
        panel.add(comboBoxNazev);

        panel.add(new JLabel("Počet balení:"));
        textFieldKoupeneMnozstvi = new JTextField(15);
        panel.add(textFieldKoupeneMnozstvi);

        panel.add(new JLabel("Jednotka balení:"));
        JComboBox<String> comboBoxJednotka = new JComboBox<>();
        comboBoxJednotka.setEditable(false);
        panel.add(comboBoxJednotka);

        comboBoxNazev.addActionListener(e -> {
            String nazev = (String) comboBoxNazev.getSelectedItem();
            comboBoxJednotka.removeAllItems();
            comboBoxJednotka.setEditable(false);
            switch (nazev) {
                case "Kafe":
                    comboBoxJednotka.addItem("kg");
                    comboBoxJednotka.addItem("0.5kg");
                    break;
                case "Mleko":
                    comboBoxJednotka.addItem("l");
                    break;
                case "Cukr":
                    comboBoxJednotka.addItem("kg");
                    break;
                case "Kys. Citr.":
                    comboBoxJednotka.setEditable(true);
                    break;
            }
        });
                
        comboBoxNazev.setSelectedIndex(0);

        panel.add(new JLabel("Cena za kus:"));
        textFieldCena = new JTextField(15);
        panel.add(textFieldCena);

        panel.add(new JLabel("Měna:"));
        textFieldMena = new JTextField("CZK", 15);
        textFieldMena.setEditable(false);
        panel.add(textFieldMena);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        btnOk.addActionListener(e -> {
            try {
                String nazev = comboBoxNazev.getSelectedItem().toString();
                int mnozstvi = Integer.parseInt(textFieldKoupeneMnozstvi.getText());
                String jednotka = comboBoxJednotka.getSelectedItem().toString();
                BigDecimal cena = new BigDecimal(textFieldCena.getText());
                String mena = textFieldMena.getText();
                
                if (mnozstvi < 0 || cena.compareTo(BigDecimal.ZERO) < 0) {
                    throw new NumberFormatException("Záporné hodnoty nejsou povoleny.");
                }

                polozka = new PolozkaSkladu(nazev, mnozstvi, jednotka, cena, mena);
                succeeded = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neplatný formát vstupních dat.", "Chyba", JOptionPane.ERROR_MESSAGE);
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

    public PolozkaSkladu getPolozka() {
        return polozka;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

}
