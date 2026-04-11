package cz.marakvaclav.panely;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Vykreslovač buňky tabulky, který vizuálně formátuje stav platby.
 * Zaplacené účtenky zobrazuje zeleně jako nezaktivní "Uhrazeno", nezaplacené naopak červeně jako "Zaplatit".
 */
public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        boolean zaplaceno = (value instanceof Boolean) ? (Boolean) value : false;
        
        if (zaplaceno) {
            setText("✓ Detail platby");
            setBackground(new Color(200, 255, 200));
            setEnabled(true);
        } else {
            setText("Zaplatit");
            setBackground(new Color(255, 200, 200));
            setEnabled(true);
        }
        
        return this;
    }
}