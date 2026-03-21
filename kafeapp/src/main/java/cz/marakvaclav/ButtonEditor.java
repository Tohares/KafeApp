package cz.marakvaclav;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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