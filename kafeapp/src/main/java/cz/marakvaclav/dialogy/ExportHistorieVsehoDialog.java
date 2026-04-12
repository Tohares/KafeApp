package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.*;
import cz.marakvaclav.sluzby.KafeUIController;
import cz.marakvaclav.sluzby.SpravceSouboru;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;

/**
 * Dialog pro export kompletní zálohy databáze do jednoho souboru.
 * Zahrnuje uživatele, stav skladu i historii vyúčtování.
 */
public class ExportHistorieVsehoDialog extends JDialog {
    
    public ExportHistorieVsehoDialog(Frame parent, KafeUIController controller) {
        super(parent, "Export kompletní zálohy", true);
        setLayout(new BorderLayout(10, 10));

        JLabel lblInfo = new JLabel("<html><div style='text-align: center;'>Tato akce vyexportuje kompletní stav aplikace<br>(Kafaře, Sklad i Účtenky) do jednoho podepsaného souboru.</div></html>");
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(lblInfo, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnExport = new JButton("Vybrat umístění a Exportovat");
        JButton btnStorno = new JButton("Zrušit");

        btnExport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Uložit kompletní zálohu jako...");
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                if (file.exists()) {
                    int response = JOptionPane.showConfirmDialog(this, 
                            "Soubor již existuje. Opravdu jej chcete přepsat?", 
                            "Potvrzení přepsání", 
                            JOptionPane.YES_NO_OPTION, 
                            JOptionPane.WARNING_MESSAGE);
                    if (response != JOptionPane.YES_OPTION) return;
                }
                
                exportData(file, controller);
                dispose();
            }
        });

        btnStorno.addActionListener(e -> dispose());

        btnPanel.add(btnExport);
        btnPanel.add(btnStorno);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void exportData(File file, KafeUIController controller) {
        try {
            StringBuilder sb = new StringBuilder();
            
            sb.append(SpravceSouboru.HEADER_KAFARI).append("\n");
            sb.append(controller.getAdmin().toCsv()).append("\n"); // Včetně dat administrátora!
            for (Kafar k : controller.getKafari()) sb.append(k.toCsv()).append("\n");
            
            sb.append(SpravceSouboru.HEADER_SKLAD).append("\n");
            for (PolozkaSkladu p : controller.getSklad()) sb.append(p.toCsv()).append("\n");
            
            sb.append(SpravceSouboru.HEADER_VYUCTOVANI).append("\n");
            for (Vyuctovani v : controller.getSeznamVyuctovani()) sb.append(v.toCsv()).append("\n");

            // Vygeneruje se podpis pro celý tento obsah
            String signature = Uzivatel.checkSum(sb.toString());
            sb.append(SpravceSouboru.HEADER_SIGNATURE).append("\n").append(signature).append("\n");

            try (PrintWriter writer = new PrintWriter(file)) {
                writer.write(sb.toString());
            }
            JOptionPane.showMessageDialog(this, "Záloha byla úspěšně vytvořena a podepsána.", "Úspěch", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Chyba při exportu: " + ex.getMessage(), "Chyba", JOptionPane.ERROR_MESSAGE);
        }
    }
}
