package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.*;
import cz.marakvaclav.sluzby.KafeController;
import cz.marakvaclav.sluzby.SpravceSouboru;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ImportHistorieVsehoDialog extends JDialog {
    private boolean succeeded = false;

    public ImportHistorieVsehoDialog(Frame parent, KafeController controller) {
        super(parent, "Import dat ze zálohy", true);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblInfo = new JLabel("Vyberte strategii pro nahrání dat ze záložního souboru:");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(lblInfo, BorderLayout.NORTH);

        JPanel radioPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        radioPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JRadioButton rbtnNuklearni = new JRadioButton("Plná obnova (Nenávratně smaže a přepíše aktuální data)");
        JRadioButton rbtnSlouceni = new JRadioButton("Chytré sloučení (Doplní chybějící účtenky a sečte sklad)");
        rbtnSlouceni.setSelected(true); // Defaultní a bezpečnější volba

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbtnNuklearni);
        bg.add(rbtnSlouceni);
        radioPanel.add(rbtnNuklearni);
        radioPanel.add(rbtnSlouceni);
        topPanel.add(radioPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnImport = new JButton("Vybrat soubor a Importovat");
        JButton btnStorno = new JButton("Zrušit");

        btnImport.addActionListener(e -> {
            if (rbtnNuklearni.isSelected()) {
                int confirm = JOptionPane.showConfirmDialog(this, "POZOR: Opravdu chcete vymazat všechna současná data a nahradit je zálohou?", "Varování", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Vyberte soubor se zálohou...");
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                importData(file, controller, rbtnNuklearni.isSelected());
            }
        });

        btnStorno.addActionListener(e -> dispose());
        btnPanel.add(btnImport);
        btnPanel.add(btnStorno);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void importData(File file, KafeController controller, boolean isNuclear) {
        try {
            // Nahrání celého obsahu s normalizací konců řádků (Windows vs Linux formát)
            String content = Files.readString(file.toPath()).replace("\r\n", "\n");
            int sigIndex = content.indexOf("===SIGNATURE===\n");
            if (sigIndex == -1) throw new Exception("Soubor neobsahuje platný podpis zálohy.");
            
            // Rozdělení obsahu na čistá data a samotný kontrolní hash pro účely ověření integrity
            String dataToHash = content.substring(0, sigIndex);
            String signatureInFile = content.substring(sigIndex + 16).trim();
            
            if (!Uzivatel.checkSum(dataToHash).equals(signatureInFile)) {
                throw new Exception("Podpis souboru nesouhlasí! Data byla poškozena nebo upravena.");
            }
            
            List<Kafar> impKafari = new ArrayList<>();
            List<PolozkaSkladu> impSklad = new ArrayList<>();
            List<Vyuctovani> impVyuctovani = new ArrayList<>();
            Admin impAdmin = null;
            
            int section = 0; // 1=KAFARI, 2=SKLAD, 3=VYUCTOVANI
            for (String line : dataToHash.split("\n")) {
                if (line.equals("===KAFARI===")) { section = 1; continue; }
                if (line.equals("===SKLAD===")) { section = 2; continue; }
                if (line.equals("===VYUCTOVANI===")) { section = 3; continue; }
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(";");
                if (section == 1 && parts.length >= 3) {
                    try {
                        int pocet = Integer.parseInt(parts[2]);
                        Kafar k = new Kafar(parts[0], "");
                        k.setHesloHash(parts[1]);
                        k.setPocetVypitychKav(pocet);
                        impKafari.add(k);
                    } catch (NumberFormatException e) {
                        if (parts.length >= 4) {
                            Admin a = new Admin(parts[0], "");
                            a.setHesloHash(parts[1]);
                            a.setCisloUctuIBAN(parts[2]);
                            a.setCisloUctuCZ(parts[3]);
                            impAdmin = a;
                        }
                    }
                } else if (section == 2 && parts.length == 7) {
                    impSklad.add(new PolozkaSkladu(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]), 
                        Integer.parseInt(parts[3]), parts[4], new BigDecimal(parts[5]), parts[6]));
                } else if (section == 3 && parts.length > 10) {
                    Vyuctovani v = new Vyuctovani();
                    v.fromCsv(parts);
                    impVyuctovani.add(v);
                }
            }
            
            if (isNuclear) {
                SpravceSouboru.smazVsechnaData(controller.getPrihlasenyUzivatel());
                controller.getKafari().clear(); controller.getKafari().addAll(impKafari);
                controller.getSklad().clear(); controller.getSklad().addAll(impSklad);
                controller.getSeznamVyuctovani().clear(); controller.getSeznamVyuctovani().addAll(impVyuctovani);
                if (impAdmin != null) {
                    controller.getAdmin().setLogin(impAdmin.getLogin());
                    controller.getAdmin().setHesloHash(impAdmin.getHesloHash());
                    controller.getAdmin().setCisloUctuIBAN(impAdmin.getCisloUctuIBAN());
                    controller.getAdmin().setCisloUctuCZ(impAdmin.getCisloUctuCZ());
                }
            } else {
                // Chytré sloučení (Merge)
                for (Kafar ik : impKafari) {
                    boolean found = false;
                    for (Kafar ck : controller.getKafari()) {
                        if (ck.getLogin().equals(ik.getLogin())) {
                        // Pokud uživatel existuje v aktuálních datech i v záloze, ponechá se vyšší počet káv (pro případ, že po provedení zálohy stihl pít)
                            ck.setPocetVypitychKav(Math.max(ck.getPocetVypitychKav(), ik.getPocetVypitychKav()));
                            found = true; break;
                        }
                    }
                    if (!found) controller.getKafari().add(ik);
                }
                for (PolozkaSkladu is : impSklad) {
                    boolean found = false;
                    for (PolozkaSkladu cs : controller.getSklad()) {
                        if (cs.getNazev().equals(is.getNazev()) && cs.getJednotka().equals(is.getJednotka())) {
                            cs.setKoupeneMnozstvi(cs.getKoupeneMnozstvi() + is.getKoupeneMnozstvi());
                            cs.setAktualniMnozstvi(cs.getAktualniMnozstvi() + is.getAktualniMnozstvi());
                            found = true; break;
                        }
                    }
                    if (!found) controller.getSklad().add(is);
                }
                for (Vyuctovani iv : impVyuctovani) {
                    boolean found = false;
                    for (Vyuctovani cv : controller.getSeznamVyuctovani()) {
                    // Účtenka se porovnává podle loginu, data a ceny za kávu (protože to ji jednoznačně identifikuje)
                        if (cv.getLogin().equals(iv.getLogin()) && cv.getDatumVystaveni().equals(iv.getDatumVystaveni()) && (cv.getCenaZaVypiteKavy() != null && iv.getCenaZaVypiteKavy() != null && cv.getCenaZaVypiteKavy().compareTo(iv.getCenaZaVypiteKavy()) == 0)) {
                            found = true; break;
                        }
                    }
                    if (!found) controller.getSeznamVyuctovani().add(iv);
                }
                if (impAdmin != null) {
                    controller.getAdmin().setLogin(impAdmin.getLogin());
                    controller.getAdmin().setHesloHash(impAdmin.getHesloHash());
                    controller.getAdmin().setCisloUctuIBAN(impAdmin.getCisloUctuIBAN());
                    controller.getAdmin().setCisloUctuCZ(impAdmin.getCisloUctuCZ());
                }
            }
            
            // Nyní se všechny nové/aktualizované záznamy zapíšou do souborů (a tím se i podepíšou klasickými sigy)
            for (Kafar k : controller.getKafari()) SpravceSouboru.ulozKafare(k, controller.getPrihlasenyUzivatel());
            for (PolozkaSkladu p : controller.getSklad()) SpravceSouboru.ulozPolozkuNaSklad(p, controller.getPrihlasenyUzivatel());
            for (Vyuctovani v : controller.getSeznamVyuctovani()) SpravceSouboru.ulozVyuctovani(v, controller.getPrihlasenyUzivatel());
            SpravceSouboru.ulozAdmina(controller.getAdmin(), controller.getPrihlasenyUzivatel());
            
            succeeded = true;
            JOptionPane.showMessageDialog(this, "Import byl úspěšně dokončen.", "Úspěch", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Chyba při importu: " + ex.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
