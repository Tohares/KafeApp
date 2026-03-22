package cz.marakvaclav.panely;

import cz.marakvaclav.tabulky.UserTableModel;

import java.awt.*;
import javax.swing.*;

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

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void obnovData(String login, int nezuctovane, int nezaplacene, int zaplacene) {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{login, nezuctovane, nezaplacene, zaplacene});
    }

    public void nastavVypiteKavy(int pocet) {
        tableModel.setValueAt(pocet, 0, 1);
    }

    public JTable getTable() { return table; }
}