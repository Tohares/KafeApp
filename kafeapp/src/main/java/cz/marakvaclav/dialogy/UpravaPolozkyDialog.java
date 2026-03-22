package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.PolozkaSkladu;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class UpravaPolozkyDialog extends JDialog {
    private JTextField textFieldKoupeneMnozstvi;
    private JTextField textFieldAktualniMnozstvi;
    private JTextField textFieldCena;
    private boolean succeeded;

    public UpravaPolozkyDialog(Frame parent, PolozkaSkladu polozka) {
        super(parent, "Změnit položku skladu", true);
        
        succeeded = false;
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Název:"));
        String[] nazvy = {"Kafe", "Mleko", "Cukr", "Kys. Citr."};
        JComboBox<String> comboBoxNazev = new JComboBox<>(nazvy);
        panel.add(comboBoxNazev);

        panel.add(new JLabel("Koupené množství:"));
        textFieldKoupeneMnozstvi = new JTextField(String.valueOf(polozka.getKoupeneMnozstvi()));
        panel.add(textFieldKoupeneMnozstvi);

        panel.add(new JLabel("Aktuální množství:"));
        textFieldAktualniMnozstvi = new JTextField(String.valueOf(polozka.getAktualniMnozstvi()));
        panel.add(textFieldAktualniMnozstvi);

        panel.add(new JLabel("Jednotka:"));
        JComboBox<String> comboBoxJednotka = new JComboBox<>();
        comboBoxJednotka.setEditable(false);
        panel.add(comboBoxJednotka);

        // Dynamicky upravuje nabídku měrných jednotek
        comboBoxNazev.addActionListener(e -> {
            String nazev = (String) comboBoxNazev.getSelectedItem();
            comboBoxJednotka.removeAllItems();
            comboBoxJednotka.setEditable(false);
            if (nazev != null) {
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
            }
        });

        // Vynutí se načtení jednotek a předvýběr původních hodnot
        comboBoxNazev.setSelectedIndex(-1);
        comboBoxNazev.setSelectedItem(polozka.getNazev());
        comboBoxJednotka.setSelectedItem(polozka.getJednotka());

        panel.add(new JLabel("Cena za kus:"));
        textFieldCena = new JTextField(polozka.getCenaZaKus().toPlainString());
        panel.add(textFieldCena);

        panel.add(new JLabel("Měna:"));
        JTextField textFieldMena = new JTextField(polozka.getMenaPenezni());
        textFieldMena.setEditable(false);
        panel.add(textFieldMena);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        btnOk.addActionListener(e -> {
            try {
                int koupene = Integer.parseInt(textFieldKoupeneMnozstvi.getText());
                int aktualni = Integer.parseInt(textFieldAktualniMnozstvi.getText());
                BigDecimal cena = new BigDecimal(textFieldCena.getText().replace(",", "."));
                
                if (koupene < 0 || aktualni < 0 || cena.compareTo(BigDecimal.ZERO) < 0) {
                    throw new NumberFormatException("Záporné hodnoty nejsou povoleny.");
                }

                if (aktualni > koupene) {
                    JOptionPane.showMessageDialog(this, "Aktuální množství nemůže být vyšší než celkové nakoupené množství!", "Chyba", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Object vybranyNazev = comboBoxNazev.getSelectedItem();
                Object vybranaJednotka = comboBoxJednotka.getSelectedItem();

                if (vybranyNazev == null || vybranyNazev.toString().trim().isEmpty() || 
                    vybranaJednotka == null || vybranaJednotka.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Název položky a jednotka nesmí být prázdné.", "Chyba", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                polozka.setNazev(vybranyNazev.toString().trim());
                polozka.setJednotka(vybranaJednotka.toString().trim());
                polozka.setKoupeneMnozstvi(koupene);
                polozka.setAktualniMnozstvi(aktualni);
                polozka.setCenaZaKus(cena);
                
                succeeded = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neplatný formát vstupních dat.", "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnStorno.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnOk);
        buttonPanel.add(btnStorno);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}