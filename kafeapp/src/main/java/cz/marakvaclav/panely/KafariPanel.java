package cz.marakvaclav.panely;

import cz.marakvaclav.entity.Kafar;
import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.tabulky.KafariTableModel;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        table.setFillsViewportHeight(true); // Tabulka zachytí kliknutí i v prázdném prostoru pod řádky
        
        // Nastavení vlastního vykreslovače a editoru pro sloupec "Nezúčtované kávy"
        table.getColumnModel().getColumn(1).setCellRenderer(new KavaRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new KavaEditor());

        // Vlastní vykreslovač pro sloupec "Zrušené kávy", který upozorní na podvodné chování (více jak 10 % stornovaných káv)
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                
                int modelRow = t.convertRowIndexToModel(r);
                int nezuctovane = Integer.parseInt(t.getModel().getValueAt(modelRow, 1).toString());
                int zrusene = Integer.parseInt(v.toString());
                
                if (!s) { // Neměníme barvu, pokud má uživatel zrovna označený celý řádek myší
                    if (zrusene > (nezuctovane * 0.1)) {
                        comp.setBackground(new Color(255, 200, 200)); // Červené varování
                    } else {
                        comp.setBackground(t.getBackground());
                    }
                }
                
                int celkemKliku = nezuctovane + zrusene;
                if (celkemKliku > 0 && zrusene > 0) {
                    long procenta = Math.round((zrusene * 100.0) / celkemKliku);
                    setText(zrusene + " (" + procenta + " %)");
                }
                
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Zachování vizuálního stylu aplikace
                return comp;
            }
        });

        // Listener pro zrušení editace a výběru při kliknutí do prázdného místa pod tabulkou
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

    // Přepočítá statistiky pro každého kafaře na základě kompletní historie vyúčtování
    public void obnovData(List<Kafar> kafari, List<Vyuctovani> seznamVyuctovani) {
        if (table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }
        table.clearSelection();
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
            tableModel.addRow(new Object[]{k.getLogin(), nezuctovaneKavy, k.getZruseneKavy(), zaplaceneKavy, uctovaneKavy});
        }
    }

    public void pridejSelectionListener(javax.swing.event.ListSelectionListener listener) {
        table.getSelectionModel().addListSelectionListener(listener);
    }

    public void zrusVyber() {
        table.clearSelection();
    }

    public boolean maVybranehoKafare() {
        return table.getSelectedRow() != -1;
    }

    public String getVybranyLogin() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            return (String) tableModel.getValueAt(modelRow, 0);
        }
        return null;
    }

    public void setOnPocetKavZmenen(java.util.function.BiConsumer<String, Integer> onZmena) {
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == 1 && row >= 0) {
                try {
                    Object newValue = tableModel.getValueAt(row, column);
                    int novyPocet = Integer.parseInt(newValue.toString());
                    if (novyPocet < 0) throw new NumberFormatException();
                    String login = (String) tableModel.getValueAt(row, 0);
                    onZmena.accept(login, novyPocet);
                } catch (NumberFormatException ex) {
                    onZmena.accept(null, -1); // Signalizace chyby formátu ven
                }
            }
        });
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