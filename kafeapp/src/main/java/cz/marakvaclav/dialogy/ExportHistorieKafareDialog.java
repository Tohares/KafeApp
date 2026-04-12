package cz.marakvaclav.dialogy;

import cz.marakvaclav.entity.Vyuctovani;

import java.awt.*;
import java.util.List;
import java.io.File;
import java.io.PrintWriter;
import javax.swing.*;

/**
 * Dialog pro zobrazení a případný export osobní historie vyúčtování běžného uživatele (kafaře).
 * Data zobrazuje v přehledném textovém formátu a nabízí uložení do standardního CSV souboru
 * pro možnost dalšího zpracování (např. v Excelu).
 */
public class ExportHistorieKafareDialog extends JDialog{
    private boolean succeeded = false;

    public ExportHistorieKafareDialog(JFrame parent, List<Vyuctovani> historie) {
        super(parent, "Export historie", true);
        setLayout(new BorderLayout(10, 10));

        JTextArea textArea = new JTextArea(20, 80);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder sbVzhled = new StringBuilder();
        sbVzhled.append(String.format("%-12s | %-10s | %-10s | %-10s | %-6s | %-10s\n", 
                            "Datum", "Počet káv", "Cena/ks", "Celkem", "Měna", "Zaplaceno"));
        sbVzhled.append("----------------------------------------------------------------------\n");

        // Formátování dat historie do podoby zarovnané tabulky pro čistě vizuální zobrazení
        for (Vyuctovani v : historie) {
            sbVzhled.append(String.format("%-12s | %-10d | %-10s | %-10s | %-6s | %-10s\n",
                    v.getDatumVystaveni().toString(),
                    v.getPocetVypitychKav(),
                    v.getCenaJedneKavy().toString(),
                    v.getCenaZaVypiteKavy().toString(),
                    "CZK",
                    v.getStavPlatby() ? "ANO" : "NE"));
        }

        textArea.setText(sbVzhled.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton btnExport = new JButton("Exportovat do CSV");
        JButton btnZavrit = new JButton("Zavřít");

        btnExport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
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
                
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("Datum;PocetKav;Cena/ks;Celkem;Mena;Zaplaceno");
                    for (Vyuctovani v : historie) {
                        writer.println(
                            v.getDatumVystaveni().toString() + ";" +
                            v.getPocetVypitychKav() + ";" +
                            v.getCenaJedneKavy().toString() + ";" +
                            v.getCenaZaVypiteKavy().toString() + ";" +
                            "CZK" + ";" +
                            (v.getStavPlatby() ? "ANO" : "NE"));
                    }
                } catch (Exception ex) {
                    System.err.println("Chyba při exportu do CSV: " + ex.getMessage());
                }
                succeeded = true;
                dispose();
            }
        });

        btnZavrit.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnExport);
        buttonPanel.add(btnZavrit);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }
    
    public boolean isSucceeded() {
        return succeeded;
    }
}
