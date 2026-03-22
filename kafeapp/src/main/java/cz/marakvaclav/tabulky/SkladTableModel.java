package cz.marakvaclav.tabulky;

import javax.swing.table.DefaultTableModel;

public class SkladTableModel extends DefaultTableModel {
    private static final String[] SLOUPCE = {"ID", "Název", "<html>Koupené<br>množství</html>", "<html>Aktuální<br>množství</html>", "Jednotka", "Cena za kus", "Měna"};

    public SkladTableModel() {
        super(SLOUPCE, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Sklad se edituje přes dialogy, ne přímo v buňkách
    }
}