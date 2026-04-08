package cz.marakvaclav.tabulky;

import javax.swing.table.DefaultTableModel;

/**
 * Datový model pro zobrazení seznamu vyúčtování (účtenek).
 * Definuje datové typy jednotlivých sloupců pro jejich správné grafické formátování 
 * a omezuje editaci pouze na interaktivní sloupec pro potvrzení platby.
 */
public class UctenkyTableModel extends DefaultTableModel {
    private static final String[] SLOUPCE = {"Kafař", "Počet káv", "Datum", "Cena jedné kávy", "Cena celkem", "Uhrazeno"};

    public UctenkyTableModel() {
        super(SLOUPCE, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 5; // Je povoleno upravovat pouze sloupec s tlačítkem pro platbu
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 1: return Integer.class;
            case 2: return java.time.LocalDate.class;
            case 3: return java.math.BigDecimal.class;
            case 4: return java.math.BigDecimal.class;
            case 5: return Boolean.class;
            default: return String.class;
        }
    }
}