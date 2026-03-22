package cz.marakvaclav.tabulky;

import javax.swing.table.DefaultTableModel;

public class KafariTableModel extends DefaultTableModel {
    private static final String[] SLOUPCE = {"Uživatel (login)", "Nezúčtované kávy", "Zaplacené kávy", "Účtované kávy"};

    public KafariTableModel() {
        super(SLOUPCE, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 1; // Povolí editaci pouze pro sloupec "Nezuctovane kavy"
    }
}