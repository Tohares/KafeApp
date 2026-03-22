package cz.marakvaclav.panely;

import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.sluzby.KafeGui;
import cz.marakvaclav.tabulky.UctenkyTableModel;

import java.awt.*;
import java.util.List;
import javax.swing.*;

public class UctenkyPanel extends JPanel {
    private JTable table;
    private UctenkyTableModel tableModel;

    public UctenkyPanel(KafeGui gui) {
        setLayout(new BorderLayout());
        tableModel = new UctenkyTableModel();
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), gui));
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            private int lastSelectedRow = -1;
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row == -1) {
                    table.clearSelection();
                    lastSelectedRow = -1;
                } 
                else if (row == lastSelectedRow) {
                    table.clearSelection();
                    lastSelectedRow = -1;
                    table.getSelectionModel().setValueIsAdjusting(true);
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setValueIsAdjusting(false);
                } else {
                    lastSelectedRow = row;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void obnovData(List<Vyuctovani> seznamVyuctovani, String filtrLogin) {
        tableModel.setRowCount(0);
        for (Vyuctovani v : seznamVyuctovani) {
            if (filtrLogin == null || v.getLogin().equals(filtrLogin)) {
                tableModel.addRow(new Object[]{v.getLogin(), v.getPocetVypitychKav(), v.getDatumVystaveni(),
                        v.getCenaJedneKavy(), v.getCenaZaVypiteKavy(), v.getStavPlatby()});
            }
        }
    }

    public JTable getTable() { return table; }
    public UctenkyTableModel getTableModel() { return tableModel; }
}