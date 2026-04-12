package cz.marakvaclav.panely;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;
import java.awt.event.MouseEvent;

/**
 * Editor buňky pro počet káv.
 * Po kliknutí nahradí buňku textovým polem a tlačítky pro okamžité uložení nebo stornování úpravy.
 */
public class KavaEditor extends AbstractCellEditor implements TableCellEditor {
    private JPanel panel;
    private JTextField textField;
    private JButton btnUlozit;
    private JButton btnStorno;
    private boolean actionPotvrzena = false;

    public KavaEditor() {
        panel = new JPanel(new BorderLayout(5, 0));
        
        textField = new JTextField();
        
        btnUlozit = new JButton("Uložit");
        btnStorno = new JButton("Storno");

        btnUlozit.setMargin(new Insets(2, 5, 2, 5));
        btnStorno.setMargin(new Insets(2, 5, 2, 5));
        
        btnUlozit.setBackground(new Color(200, 255, 200));
        btnStorno.setBackground(new Color(255, 200, 200));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 2, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnUlozit);
        btnPanel.add(btnStorno);

        panel.add(textField, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.EAST);

        // Pokud se klikne na uložit, tabulka převezme hodnotu a uloží ji do modelu
        btnUlozit.addActionListener(e -> {
            actionPotvrzena = true;
            fireEditingStopped();
        });
        
        // Pokud se klikne na storno, úprava se zruší a hodnota se vrátí zpět
        btnStorno.addActionListener(e -> {
            actionPotvrzena = true;
            fireEditingCanceled();
        });
        
        // Pokud uživatel zmáčkne Enter přímo v textovém poli, bere se to jako uložení
        textField.addActionListener(e -> {
            actionPotvrzena = true;
            fireEditingStopped();
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        actionPotvrzena = false;
        textField.setText(value != null ? value.toString() : "0");
        textField.setFont(table.getFont());
        panel.setBackground(table.getSelectionBackground());
        
        // Hack: Zpoždění aktivace tlačítek o zlomek vteřiny zabrání tomu, aby se okamžitě 
        // spustila při uvolnění tlačítka myši z původního kliknutí na tabulku
        btnUlozit.setEnabled(false);
        btnStorno.setEnabled(false);
        Timer t = new Timer(100, e -> {
            btnUlozit.setEnabled(true);
            btnStorno.setEnabled(true);
        });
        t.setRepeats(false);
        t.start();

        // Automaticky přesune zaměření do pole, aby mohl uživatel rovnou psát
        SwingUtilities.invokeLater(() -> {
            textField.requestFocusInWindow();
            textField.selectAll();
        });
        
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
            return ((MouseEvent) e).getClickCount() >= 1; // Aktivuje se okamžitě na první kliknutí
        }
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        // Pokud je editace přerušena jinak než naším tlačítkem/Enterem (např. uživatel klikl do jiné buňky),
        // JTable se pokusí automaticky uložit data. My ho místo toho přinutíme k akci Storno.
        if (!actionPotvrzena) {
            cancelCellEditing();
            return true;
        }
        return super.stopCellEditing();
    }
}