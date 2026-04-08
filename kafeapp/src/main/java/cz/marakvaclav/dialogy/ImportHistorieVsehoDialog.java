package cz.marakvaclav.dialogy;

import cz.marakvaclav.sluzby.KafeController;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Dialog pro import kompletní zálohy databáze ze souboru.
 * Nabízí uživateli (administrátorovi) dvě strategie obnovy: 
 * bezpečné chytré sloučení (přidá jen to, co chybí) nebo plné přepsání (nukleární volba).
 */
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
            controller.importDatZeZalohy(file, isNuclear);

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
