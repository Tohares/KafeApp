package cz.marakvaclav.panely;

import cz.marakvaclav.sluzby.KafeUIController;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import cz.marakvaclav.entity.Vyuctovani;

/**
 * Vykreslovač buňky tabulky, který vizuálně formátuje stav platby.
 * Zaplacené účtenky zobrazuje zeleně jako nezaktivní "Uhrazeno", nezaplacené naopak červeně jako "Zaplatit".
 */
public class ButtonRenderer extends JButton implements TableCellRenderer {
    private KafeUIController controller;

    public ButtonRenderer(KafeUIController controller) {
        this.controller = controller;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (row < 0 || row >= table.getRowCount()) return this;
        int modelRow = table.convertRowIndexToModel(row);
        String login = (String) table.getModel().getValueAt(modelRow, 0);

        Vyuctovani v = (Vyuctovani) table.getModel().getValueAt(modelRow, 6);
        boolean oznameno = (v != null && v.isOznamenoJakoZaplacene());
        boolean zaplaceno = (value instanceof Boolean) ? (Boolean) value : false;
        
        boolean isCurrentUserAdmin = controller.getAdmin() != null && controller.getPrihlasenyUzivatel().equals(controller.getAdmin().getLogin());
        boolean isAdminLine = controller.getAdmin() != null && login.equals(controller.getAdmin().getLogin());
        
        if (isCurrentUserAdmin) {
            if (isAdminLine) {
                if (zaplaceno) {
                    setText("✓ Vše uhrazeno");
                    setBackground(new Color(200, 255, 200));
                } else {
                    setText("Čeká na úhrady");
                    setBackground(new Color(230, 230, 230)); // Světle šedá (neutrální)
                }
                setEnabled(false);
            } else {
                if (zaplaceno) {
                    setText("✓ Detail / Storno");
                    setBackground(new Color(200, 255, 200));
                } else if (oznameno) {
                    setText("❗ Potvrdit platbu");
                    setBackground(new Color(255, 200, 100)); // Oranžová
                } else {
                    setText("Nezaplaceno");
                    setBackground(new Color(255, 200, 200));
                }
                setEnabled(true);
            }
        } else {
            if (zaplaceno) {
                setText("✓ Detail platby");
                setBackground(new Color(200, 255, 200));
            } else if (oznameno) {
                setText("⏳ Čeká na potvrzení");
                setBackground(new Color(255, 250, 200)); // Žlutá
            } else {
                setText("Zaplatit");
                setBackground(new Color(255, 200, 200));
            }
            setEnabled(true);
        }
        
        return this;
    }
}