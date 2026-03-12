package cz.marakvaclav;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VyuctovaniDialog extends JDialog {
    private List<PolozkaSkladu> sklad;
    private List<PolozkaSkladu> spotrebovanePolozky;
    private boolean succeeded;

    public VyuctovaniDialog(Frame parent, List<PolozkaSkladu> sklad, List<Kafar> kafari, String login) {
        super(parent, "Vyuctovat", true);
        this.sklad = sklad;
        succeeded = false;
    
        JPanel panel = new JPanel(new GridLayout(5, 4, 10, 10));

        panel.add(new JLabel("Surovina"));
        panel.add(new JLabel("Na sklade"));
        panel.add(new JLabel("Jednotka baleni"));
        panel.add(new JLabel("Spotreba"));

        panel.add(new JLabel("Kafe"));
        panel.add(new JLabel(String.valueOf(najdiPolozku("Kafe").getAktualniMnozstvi())));
        panel.add(new JLabel(najdiPolozku("Kafe").getJednotka()));
        JTextField textFieldKafe = new JTextField(15);
        panel.add(textFieldKafe);

        panel.add(new JLabel("Mleko"));
        panel.add(new JLabel(String.valueOf(najdiPolozku("Mleko").getAktualniMnozstvi())));
        panel.add(new JLabel(najdiPolozku("Mleko").getJednotka()));
        JTextField textFieldMleko = new JTextField(15);
        panel.add(textFieldMleko);

        panel.add(new JLabel("Cukr"));
        panel.add(new JLabel(String.valueOf(najdiPolozku("Cukr").getAktualniMnozstvi())));
        panel.add(new JLabel(najdiPolozku("Cukr").getJednotka()));
        JTextField textFieldCukr = new JTextField(15);
        panel.add(textFieldCukr);

        panel.add(new JLabel("Kys. Citr."));
        panel.add(new JLabel(String.valueOf(najdiPolozku("Kys. Citr.").getAktualniMnozstvi())));
        panel.add(new JLabel(najdiPolozku("Kys. Citr.").getJednotka()));
        JTextField textFieldCitr = new JTextField(15);
        panel.add(textFieldCitr);

        JButton btnOk = new JButton("OK");
        JButton btnStorno = new JButton("Storno");

        btnOk.addActionListener(e -> {
            try{
                spotrebovanePolozky = new ArrayList<>();
                PolozkaSkladu spotrKafe = new PolozkaSkladu(-1,"Kafe",Integer.parseInt(textFieldKafe.getText()),Integer.parseInt(textFieldKafe.getText()),
                    najdiPolozku("Kafe").getJednotka(),najdiPolozku("Kafe").getCenaZaKus(),najdiPolozku("Kafe").getMenaPenezni());
                spotrebovanePolozky.add(spotrKafe);

                int mnozstviMleka = Integer.parseInt(textFieldMleko.getText());
                BigDecimal cenaMleka = cenaMleka(mnozstviMleka);
                PolozkaSkladu spotrMleko = new PolozkaSkladu(-1,"Mleko",mnozstviMleka,mnozstviMleka,
                    najdiPolozku("Mleko").getJednotka(),cenaMleka,najdiPolozku("Mleko").getMenaPenezni());
                spotrebovanePolozky.add(spotrMleko);

                PolozkaSkladu spotrCukr = new PolozkaSkladu(-1,"Cukr",Integer.parseInt(textFieldCukr.getText()),Integer.parseInt(textFieldCukr.getText()),
                    najdiPolozku("Cukr").getJednotka(),najdiPolozku("Cukr").getCenaZaKus(),najdiPolozku("Cukr").getMenaPenezni());
                spotrebovanePolozky.add(spotrCukr);

                PolozkaSkladu spotrCitr = new PolozkaSkladu(-1,"Kys. Citr.",Integer.parseInt(textFieldCitr.getText()),Integer.parseInt(textFieldCitr.getText()),
                    najdiPolozku("Kys. Citr.").getJednotka(),najdiPolozku("Kys. Citr.").getCenaZaKus(),najdiPolozku("Kys. Citr.").getMenaPenezni());
                spotrebovanePolozky.add(spotrCitr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Neplatný formát vstupních dat.", "Chyba", JOptionPane.ERROR_MESSAGE);
            }

            for (PolozkaSkladu spotr : spotrebovanePolozky) {
                int spotrMnozstvi = spotr.getAktualniMnozstvi();
                for (PolozkaSkladu skladova : sklad) {
                    if (spotrMnozstvi == 0) {
                        break;
                    }
                    if (skladova.getNazev().equals(spotr.getNazev())) {
                        if (skladova.getAktualniMnozstvi() >= spotrMnozstvi) {
                            skladova.setAktualniMnozstvi(skladova.getAktualniMnozstvi()-spotrMnozstvi);
                            SpravceSouboru.ulozPolozkuNaSklad(skladova, login);
                            break;
                        } else {
                            spotrMnozstvi -= skladova.getAktualniMnozstvi();
                            skladova.setAktualniMnozstvi(0);
                            SpravceSouboru.ulozPolozkuNaSklad(skladova, login);
                        }
                    }
                }    
            }
            
            Vyuctovani vyuctovani = new Vyuctovani(spotrebovanePolozky, login, LocalDate.now(), spocitejVypiteKavy(kafari));
            SpravceSouboru.ulozVyuctovani(vyuctovani, login);
            for (Kafar k : kafari) {
                Vyuctovani vyuctovaniKafare = new Vyuctovani(vyuctovani, k.getLogin(), k.getPocetVypitychKav());
                SpravceSouboru.ulozVyuctovani(vyuctovaniKafare, login);
                k.setPocetVypitychKav(0);
                SpravceSouboru.ulozKafare(k, login);
            }

            succeeded = true;
            dispose();
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

    private PolozkaSkladu najdiPolozku(String nazev) {
        PolozkaSkladu combinedPolozka = null;
        for (PolozkaSkladu p : sklad) {
            if (p.getNazev().equals(nazev) && p.getAktualniMnozstvi() > 0) {
                if (combinedPolozka == null) {
                    combinedPolozka = new PolozkaSkladu(p.getId(),p.getNazev(),p.getKoupeneMnozstvi(),
                        p.getAktualniMnozstvi(),p.getJednotka(),p.getCenaZaKus(),p.getMenaPenezni());
                }
                else if (combinedPolozka.getJednotka().equals(p.getJednotka())) {
                    combinedPolozka.setAktualniMnozstvi(combinedPolozka.getAktualniMnozstvi()+p.getAktualniMnozstvi());
                }
            }
        }
        if (combinedPolozka == null) {
            for (PolozkaSkladu p : sklad) {
                if (p.getNazev().equals(nazev)) {
                    combinedPolozka = new PolozkaSkladu(p.getId(),p.getNazev(),p.getKoupeneMnozstvi(),
                        p.getAktualniMnozstvi(),p.getJednotka(),p.getCenaZaKus(),p.getMenaPenezni());
                    break;
                }
            }
        }
        return combinedPolozka;
    }

    private BigDecimal cenaMleka(int mnozstvi) {
        int vstupniMnozstvi = mnozstvi;
        BigDecimal cena = BigDecimal.ZERO;
        for (PolozkaSkladu p : sklad) {
            if (p.getNazev().equals("Mleko")) {
                if (p.getAktualniMnozstvi() >= mnozstvi) {
                    cena = cena.add(p.getCenaZaKus().multiply(BigDecimal.valueOf(mnozstvi)));
                    break;
                }
                else {
                    mnozstvi -= p.getAktualniMnozstvi();
                    cena = cena.add(p.getCenaZaKus().multiply(BigDecimal.valueOf(p.getAktualniMnozstvi())));
                }
            }
        }
        cena = cena.divide(BigDecimal.valueOf(vstupniMnozstvi), 2, RoundingMode.HALF_UP);
        return cena;
    }

    private int spocitejVypiteKavy(List<Kafar> kafari) {
        int pocetVypitychKav = 0;
        for (Kafar k : kafari) {
            pocetVypitychKav += k.getPocetVypitychKav();
        }
        return pocetVypitychKav;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

}
