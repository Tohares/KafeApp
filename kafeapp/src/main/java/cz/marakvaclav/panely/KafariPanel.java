package cz.marakvaclav.panely;

import cz.marakvaclav.entity.Kafar;
import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.tabulky.KafariTableModel;

import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * Panel pro administrátora zobrazující ucelený přehled všech uživatelů (kafařů).
 * Ukazuje aktuální nezaúčtovaný dluh kafaře a agregované historické statistiky (účtované a zaplacené kávy).
 */
public class KafariPanel extends JPanel {
    private JTable table;
    private KafariTableModel tableModel;

    public KafariPanel() {
        setLayout(new BorderLayout());
        tableModel = new KafariTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Přepočítá statistiky pro každého kafaře na základě kompletní historie vyúčtování
    public void obnovData(List<Kafar> kafari, List<Vyuctovani> seznamVyuctovani) {
        tableModel.setRowCount(0);
        for (Kafar k : kafari) {
            int nezuctovaneKavy = k.getPocetVypitychKav();
            int zaplaceneKavy = 0;
            int uctovaneKavy = 0;
            for (Vyuctovani v : seznamVyuctovani) {
                if (v.getLogin().equals(k.getLogin())) {
                    if (v.getStavPlatby()) {
                        zaplaceneKavy += v.getPocetVypitychKav();
                    } else {                            
                        uctovaneKavy += v.getPocetVypitychKav();
                    }                        
                }
            }
            tableModel.addRow(new Object[]{k.getLogin(), nezuctovaneKavy, zaplaceneKavy, uctovaneKavy});
        }
    }

    public JTable getTable() { return table; }
    public KafariTableModel getTableModel() { return tableModel; }
}