package cz.marakvaclav.panely;

import cz.marakvaclav.entity.PolozkaSkladu;
import cz.marakvaclav.tabulky.SkladTableModel;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Panel pro administrátora zobrazující aktuální stav skladu surovin.
 * Slouží k rychlému přehledu o tom, kolik kusů jakého balení z původního nákupu zbývá.
 */
public class SkladPanel extends JPanel {
    private JTable table;
    private SkladTableModel tableModel;

    public SkladPanel() {
        setLayout(new BorderLayout());
        tableModel = new SkladTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);

        // Listener pro zrušení editace a výběru při kliknutí do prázdného místa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row == -1) {
                    if (table.isEditing()) {
                        table.getCellEditor().cancelCellEditing();
                    }
                    table.clearSelection();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void obnovData(List<PolozkaSkladu> sklad) {
        if (table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }
        table.clearSelection();
        tableModel.setRowCount(0);
        // Zobrazení v opačném pořadí (nejnovější položky nahoře)
        for (int i = sklad.size() - 1; i >= 0; i--) {
            PolozkaSkladu p = sklad.get(i);
            tableModel.addRow(new Object[]{p.getId(), p.getSurovina(), p.getKoupeneMnozstvi(), 
                p.getAktualniMnozstvi(), p.getJednotka(), p.getCenaZaKus(), p.getMenaPenezni()});
        }
    }

    public void pridejSelectionListener(javax.swing.event.ListSelectionListener listener) {
        table.getSelectionModel().addListSelectionListener(listener);
    }

    public void zrusVyber() {
        table.clearSelection();
    }

    public boolean maVybranouPolozku() {
        return table.getSelectedRow() != -1;
    }

    public int getVybraneIdPolozky() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            return (int) tableModel.getValueAt(modelRow, 0);
        }
        return -1;
    }

    public void nastavOdsazeniBunek(int padding) {
        var standardMargin = BorderFactory.createEmptyBorder(0, padding, 0, padding);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBorder(standardMargin);
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnModel().getColumn(i).getCellRenderer() == null) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }
    }
}