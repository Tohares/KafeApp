package cz.marakvaclav.panely;

import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.sluzby.KafeGui;

import javax.swing.*;
import java.awt.*;

/**
 * Editor buňky tabulky, který vkládá interaktivní tlačítko do sloupce se stavem platby.
 * Umožňuje uživateli kliknutím na toto tlačítko vyvolat dialog pro úhradu daného vyúčtování.
 */
public class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private Vyuctovani vyuctovani;
    private KafeGui gui;

    public ButtonEditor(JCheckBox checkBox, KafeGui gui) {
        super(checkBox);
        this.gui = gui;
        button = new JButton();
        this.setClickCountToStart(1);
        button.setOpaque(true);
        
        button.addActionListener(e -> {
            fireEditingStopped(); 
            // Pokud účtenka ještě nebyla zaplacena, kliknutím se otevře okno s platebními údaji a QR kódem
            if (vyuctovani != null && !vyuctovani.getStavPlatby()) {
                gui.otevriPlatbu(vyuctovani);
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        
        int modelRow = table.convertRowIndexToModel(row);
        this.vyuctovani = gui.najdiVyuctovaniZRadku(modelRow);
        
        boolean zaplaceno = (value instanceof Boolean) ? (Boolean) value : false;
        button.setText(zaplaceno ? "✓ Uhrazeno" : "Zaplatit");
        
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return vyuctovani != null ? vyuctovani.getStavPlatby() : false;
    }
}