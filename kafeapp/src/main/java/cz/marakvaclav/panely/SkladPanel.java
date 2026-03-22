package cz.marakvaclav.panely;

import cz.marakvaclav.entity.PolozkaSkladu;
import cz.marakvaclav.tabulky.SkladTableModel;

import java.awt.*;
import java.util.List;
import javax.swing.*;

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

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void obnovData(List<PolozkaSkladu> sklad) {
        tableModel.setRowCount(0);
        for (PolozkaSkladu p : sklad) {
            tableModel.addRow(new Object[]{p.getId(), p.getNazev(), p.getKoupeneMnozstvi(), 
                p.getAktualniMnozstvi(), p.getJednotka(), p.getCenaZaKus(), p.getMenaPenezni()});
        }
    }

    public JTable getTable() { return table; }
}