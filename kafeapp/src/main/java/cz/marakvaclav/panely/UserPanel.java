package cz.marakvaclav.panely;

import cz.marakvaclav.tabulky.UserTableModel;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Informační panel pro běžného uživatele (kafaře) zobrazený v horní části jeho okna.
 * Poskytuje přehlednou tabulku o tom, kolik káv momentálně dluží a jaké má dlouhodobé statistiky.
 */
public class UserPanel extends JPanel {
    private JTable table;
    private UserTableModel tableModel;

    public UserPanel() {
        setLayout(new BorderLayout());
        tableModel = new UserTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, 70)); // Omezí výšku tabulky jen na hlavičku a 1 řádek
        add(scrollPane, BorderLayout.CENTER);
    }

    public void obnovData(String login, int nezuctovane, int nezaplacene, int zaplacene) {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{login, nezuctovane, nezaplacene, zaplacene});
    }

    public void nastavVypiteKavy(int pocet) {
        tableModel.setValueAt(pocet, 0, 1);
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