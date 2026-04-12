package cz.marakvaclav.panely;

import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.sluzby.KafeUIController;
import cz.marakvaclav.tabulky.UctenkyTableModel;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Panel pro zobrazení historie vyúčtování (účtenek).
 * Administrátor zde vidí globální seznam všech účtenek, běžný uživatel naopak jen své osobní záznamy.
 */
public class UctenkyPanel extends JPanel {
    private JTable table;
    private UctenkyTableModel tableModel;

    public UctenkyPanel(KafeUIController controller, Consumer<Vyuctovani> onOtevriPlatbu) {
        setLayout(new BorderLayout());
        tableModel = new UctenkyTableModel();
        table = new JTable(tableModel);
        // Skrytí sloupce s objektem Vyuctovani (sloupec 6), aby nebyl vidět v GUI, ale zůstal v modelu
        table.removeColumn(table.getColumnModel().getColumn(6));
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true); // Tabulka zachytí kliknutí i v prázdném prostoru
        
        // Nastavení speciálního vykreslovače a editoru pro poslední sloupec s tlačítkem platby
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer(controller));
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), controller, onOtevriPlatbu));
        
        // Listener, který umožňuje bezpečně zrušit výběr při opětovném kliknutí na již vybraný řádek
        table.addMouseListener(new MouseAdapter() {
            private int lastSelectedRow = -1;
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row == -1) {
                    if (table.isEditing()) {
                        table.getCellEditor().cancelCellEditing();
                    }
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
        if (table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }
        table.clearSelection();
        tableModel.setRowCount(0);
        
        // Seskupení účtenek do bloků (hlavní vyúčtování + podúčtenky)
        List<List<Vyuctovani>> skupiny = new ArrayList<>();
        List<Vyuctovani> aktualniSkupina = new ArrayList<>();
        
        for (Vyuctovani v : seznamVyuctovani) {
            if (aktualniSkupina.isEmpty()) {
                aktualniSkupina.add(v);
            } else {
                Vyuctovani predchozi = aktualniSkupina.get(0);
                // Účtenky patří k sobě, pokud mají stejné datum, shodnou celkovou útratu a shodný počet celkových káv
                if (v.getDatumVystaveni().equals(predchozi.getDatumVystaveni()) && 
                    (v.getCelkovaCena() != null && predchozi.getCelkovaCena() != null && v.getCelkovaCena().compareTo(predchozi.getCelkovaCena()) == 0) &&
                    v.getPocetUctovanychKavCelkem() == predchozi.getPocetUctovanychKavCelkem()) {
                    aktualniSkupina.add(v);
                } else {
                    skupiny.add(aktualniSkupina);
                    aktualniSkupina = new ArrayList<>();
                    aktualniSkupina.add(v);
                }
            }
        }
        if (!aktualniSkupina.isEmpty()) {
            skupiny.add(aktualniSkupina);
        }
        
        // Vypsání bloků v opačném pořadí (nejnovější nahoře), ale uvnitř bloku se zachová původní pořadí (Admin je první)
        for (int i = skupiny.size() - 1; i >= 0; i--) {
            for (Vyuctovani v : skupiny.get(i)) {
                if (filtrLogin == null || v.getLogin().equals(filtrLogin)) {
                    tableModel.addRow(new Object[]{v.getLogin(), v.getPocetVypitychKav(), v.getDatumVystaveni(),
                            v.getCenaJedneKavy(), v.getCenaZaVypiteKavy(), v.getStavPlatby(), v});
                }
            }
        }
    }

    public void pridejSelectionListener(javax.swing.event.ListSelectionListener listener) {
        table.getSelectionModel().addListSelectionListener(listener);
    }

    public void zrusVyber() {
        table.clearSelection();
    }

    public Vyuctovani getVybraneVyuctovani() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && selectedRow < table.getRowCount()) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            return (Vyuctovani) tableModel.getValueAt(modelRow, 6);
        }
        return null;
    }

    public void nastavOdsazeniBunek(int padding, java.util.function.Supplier<String> adminLoginSupplier) {
        var standardMargin = BorderFactory.createEmptyBorder(0, padding, 0, padding);
        var indentedMargin = BorderFactory.createEmptyBorder(0, padding + 10, 0, padding);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                int modelRow = t.convertRowIndexToModel(r);
                String login = t.getModel().getValueAt(modelRow, 0).toString();
                String adminLogin = adminLoginSupplier.get();
                setBorder((c == 0 && adminLogin != null && !login.equals(adminLogin)) ? indentedMargin : standardMargin);
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