package cz.marakvaclav;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class NovaPolozkaDialog extends JDialog{
    private JTextField textFieldNazev;
    private JTextField textFieldKoupeneMnozstvi;
    private JTextField textFieldJednotka;
    private JTextField textFieldCena;
    private JTextField textFieldMena;
    private PolozkaSkladu polozka;
    private boolean succeeded;

    public NovaPolozkaDialog(Frame parent) {
        super(parent, "Naskladnit novou položku", true);
        
        succeeded = false;
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Název:"));
        textFieldNazev = new JTextField(15);
        panel.add(textFieldNazev);

        panel.add(new JLabel("Koupené množství:"));
        textFieldKoupeneMnozstvi = new JTextField(15);
        panel.add(textFieldKoupeneMnozstvi);

        panel.add(new JLabel("Jednotka:"));
        textFieldJednotka = new JTextField(15);
        panel.add(textFieldJednotka);

        panel.add(new JLabel("Cena za kus:"));
        textFieldCena = new JTextField(15);
        panel.add(textFieldCena);

        panel.add(new JLabel("Měna:"));
        textFieldMena = new JTextField("CZK", 15);
        panel.add(textFieldMena);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        btnOk.addActionListener(e -> {
            try {
                String nazev = textFieldNazev.getText();
                float mnozstvi = Float.parseFloat(textFieldKoupeneMnozstvi.getText());
                String jednotka = textFieldJednotka.getText();
                BigDecimal cena = new BigDecimal(textFieldCena.getText());
                String mena = textFieldMena.getText();

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
