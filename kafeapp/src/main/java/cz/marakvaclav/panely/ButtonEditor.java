package cz.marakvaclav.panely;

import cz.marakvaclav.entity.Vyuctovani;
import cz.marakvaclav.sluzby.KafeUIController;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Editor buňky tabulky, který vkládá interaktivní tlačítko do sloupce se stavem platby.
 * Umožňuje uživateli kliknutím na toto tlačítko vyvolat dialog pro úhradu daného vyúčtování.
 */
public class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private Vyuctovani vyuctovani;
    private KafeUIController controller;

    public ButtonEditor(JCheckBox checkBox, KafeUIController controller, Consumer<Vyuctovani> onOtevriPlatbu) {
        super(checkBox);
        this.controller = controller;
        button = new JButton();
        this.setClickCountToStart(1);
        button.setOpaque(true);
        
        button.addActionListener(e -> {
            fireEditingStopped(); 
            if (vyuctovani != null) {
                boolean isAdminLine = controller.getAdmin() != null && vyuctovani.getLogin().equals(controller.getAdmin().getLogin());
                if (!isAdminLine) {
                    onOtevriPlatbu.accept(vyuctovani);
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        
        if (row < 0 || row >= table.getRowCount()) return button;
        int modelRow = table.convertRowIndexToModel(row);
        
        this.vyuctovani = (Vyuctovani) table.getModel().getValueAt(modelRow, 6);
        
        boolean zaplaceno = (value instanceof Boolean) ? (Boolean) value : false;
        boolean oznameno = (vyuctovani != null && vyuctovani.isOznamenoJakoZaplacene());
        
        boolean isCurrentUserAdmin = controller.getAdmin() != null && controller.getPrihlasenyUzivatel().equals(controller.getAdmin().getLogin());
        boolean isAdminLine = controller.getAdmin() != null && vyuctovani != null && vyuctovani.getLogin().equals(controller.getAdmin().getLogin());
        
        if (isCurrentUserAdmin) {
            if (isAdminLine) {
                if (zaplaceno) {
                    button.setText("✓ Vše uhrazeno");
                    button.setBackground(new Color(200, 255, 200));
                } else {
                    button.setText("Čeká na úhrady");
                    button.setBackground(new Color(230, 230, 230)); 
                }
            } else {
                if (zaplaceno) {
                    button.setText("✓ Detail / Storno");
                    button.setBackground(new Color(200, 255, 200));
                } else if (oznameno) {
                    button.setText("❗ Potvrdit platbu");
                    button.setBackground(new Color(255, 200, 100)); // Oranžová
                } else {
                    button.setText("Nezaplaceno");
                    button.setBackground(new Color(255, 200, 200));
                }
            }
        } else {
            if (zaplaceno) {
                button.setText("✓ Detail platby");
                button.setBackground(new Color(200, 255, 200));
            } else if (oznameno) {
                button.setText("⏳ Čeká na potvrzení");
                button.setBackground(new Color(255, 250, 200)); 
            } else {
                button.setText("Zaplatit");
                button.setBackground(new Color(255, 200, 200));
            }
        }

        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return vyuctovani != null ? vyuctovani.getStavPlatby() : false;
    }
}