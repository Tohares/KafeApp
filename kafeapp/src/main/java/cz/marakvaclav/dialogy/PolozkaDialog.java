package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.PolozkaSkladu;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class PolozkaDialog extends JDialog {
    private PolozkaSkladu polozka;
    private boolean isEditMode;
    private boolean succeeded;
    private boolean deleted;

    public PolozkaDialog(Frame parent, PolozkaSkladu polozkaToEdit) {
        super(parent, polozkaToEdit == null ? "Naskladnit novou položku" : "Změnit položku skladu", true);
        this.polozka = polozkaToEdit;
        this.isEditMode = (polozkaToEdit != null);
        this.succeeded = false;
        this.deleted = false;

        int rows = isEditMode ? 6 : 5;
        JPanel panel = new JPanel(new GridLayout(rows, 2, 10, 10));

        panel.add(new JLabel("Název:"));
        String[] nazvy = {"Kafe", "Mleko", "Cukr", "Kys. Citr."};
        JComboBox<String> comboBoxNazev = new JComboBox<>(nazvy);
        panel.add(comboBoxNazev);

        panel.add(new JLabel(isEditMode ? "Koupené množství:" : "Počet balení:"));
        JTextField textFieldKoupeneMnozstvi = new JTextField(isEditMode ? String.valueOf(polozka.getKoupeneMnozstvi()) : "", 15);
        panel.add(textFieldKoupeneMnozstvi);

        JTextField textFieldAktualniMnozstvi = null;
        if (isEditMode) {
            panel.add(new JLabel("Aktuální množství:"));
            textFieldAktualniMnozstvi = new JTextField(String.valueOf(polozka.getAktualniMnozstvi()), 15);
            panel.add(textFieldAktualniMnozstvi);
        }

        panel.add(new JLabel(isEditMode ? "Jednotka:" : "Jednotka balení:"));
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

        // Vynutí se načtení jednotek a předvýběr výchozích/původních hodnot
        if (isEditMode) {
            comboBoxNazev.setSelectedIndex(-1);
            comboBoxNazev.setSelectedItem(polozka.getNazev());
            comboBoxJednotka.setSelectedItem(polozka.getJednotka());
        } else {
            comboBoxNazev.setSelectedIndex(0);
        }

        panel.add(new JLabel("Cena za kus:"));
        JTextField textFieldCena = new JTextField(isEditMode ? polozka.getCenaZaKus().toPlainString() : "", 15);
        panel.add(textFieldCena);

        panel.add(new JLabel("Měna:"));
        JTextField textFieldMena = new JTextField(isEditMode ? polozka.getMenaPenezni() : "CZK", 15);
        textFieldMena.setEditable(false);
        panel.add(textFieldMena);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");
        JButton btnSmazat = new JButton("Smazat položku");

        btnOk.addActionListener(e -> {
            try {
                int koupene = Integer.parseInt(textFieldKoupeneMnozstvi.getText());
                BigDecimal cena = new BigDecimal(textFieldCena.getText().replace(",", "."));
                
                if (koupene < 0 || cena.compareTo(BigDecimal.ZERO) < 0) {
                    throw new NumberFormatException("Záporné hodnoty nejsou povoleny.");
                }

                int aktualni = koupene;
                if (isEditMode) {
                    aktualni = Integer.parseInt(textFieldAktualniMnozstvi.getText());
                    if (aktualni < 0) throw new NumberFormatException("Záporné hodnoty nejsou povoleny.");
                    if (aktualni > koupene) {
                        JOptionPane.showMessageDialog(this, "Aktuální množství nemůže být vyšší než celkové nakoupené množství!", "Chyba", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                Object vybranyNazev = comboBoxNazev.getSelectedItem();
                Object vybranaJednotka = comboBoxJednotka.getSelectedItem();

                if (vybranyNazev == null || vybranyNazev.toString().trim().isEmpty() || 
                    vybranaJednotka == null || vybranaJednotka.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Název položky a jednotka nesmí být prázdné.", "Chyba", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isEditMode) {
                    polozka.setNazev(vybranyNazev.toString().trim());
                    polozka.setJednotka(vybranaJednotka.toString().trim());
                    polozka.setKoupeneMnozstvi(koupene);
                    polozka.setAktualniMnozstvi(aktualni);
                    polozka.setCenaZaKus(cena);
                } else {
                    polozka = new PolozkaSkladu(vybranyNazev.toString().trim(), koupene, vybranaJednotka.toString().trim(), cena, textFieldMena.getText());
                }
                
                succeeded = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neplatný formát vstupních dat.", "Chyba", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnStorno.addActionListener(e -> dispose());

        btnSmazat.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Opravdu chcete tuto položku nenávratně smazat ze skladu?", 
                "Smazat položku", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                deleted = true;
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnOk);
        buttonPanel.add(btnStorno);
        if (isEditMode) {
            buttonPanel.add(btnSmazat);
        }

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

    public boolean isDeleted() {
        return deleted;
    }
}