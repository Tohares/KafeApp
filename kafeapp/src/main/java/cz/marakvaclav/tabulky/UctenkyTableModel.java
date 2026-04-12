package cz.marakvaclav.tabulky;

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Datový model pro zobrazení seznamu vyúčtování (účtenek).
 * Definuje datové typy jednotlivých sloupců pro jejich správné grafické formátování 
 * a omezuje editaci pouze na interaktivní sloupec pro potvrzení platby.
 */
public class UctenkyTableModel extends DefaultTableModel {
    private static final String[] SLOUPCE = {"Kafař", "Počet káv", "Datum", "Cena jedné kávy", "Cena celkem", "Uhrazeno", "Objekt Vyuctovani"};

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
            case 2: return LocalDate.class;
            case 3: return BigDecimal.class;
            case 4: return BigDecimal.class;
            case 5: return Boolean.class;
            case 6: return cz.marakvaclav.entity.Vyuctovani.class;
            default: return String.class;
        }
    }
}