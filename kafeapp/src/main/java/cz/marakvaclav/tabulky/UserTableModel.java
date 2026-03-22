package cz.marakvaclav.tabulky;

import javax.swing.table.DefaultTableModel;

public class UserTableModel extends DefaultTableModel {
    private static final String[] SLOUPCE = {"Uživatel (login)", "Nezúčtované kávy", "Nezaplacené kávy", "Zaplacené kávy"};

    public UserTableModel() {
        super(SLOUPCE, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Tabulka uživatele je pouze pro čtení
    }
}