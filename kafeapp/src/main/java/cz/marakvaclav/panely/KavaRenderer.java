package cz.marakvaclav.panely;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Vykreslovač buňky pro počet káv.
 * Zobrazuje aktuální číslo a vedle něj tlačítko "Upravit" pro jasnou vizuální nápovědu, že lze buňku editovat.
 */
public class KavaRenderer extends JPanel implements TableCellRenderer {
    private JLabel label;
    private JButton editBtn;

    public KavaRenderer() {
        setLayout(new BorderLayout(5, 0));
        setOpaque(true);
        
        label = new JLabel();
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        editBtn = new JButton("Upravit");
        editBtn.setMargin(new Insets(2, 5, 2, 5));
        editBtn.setFocusable(false); // Aby nerušilo focus v tabulce

        add(label, BorderLayout.CENTER);
        add(editBtn, BorderLayout.EAST);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        label.setFont(table.getFont());
        
        label.setText(value != null ? value.toString() : "0");
        return this;
    }
}