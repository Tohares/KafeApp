package cz.marakvaclav;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;


public class KafeGui extends JFrame {
    private JTable kafariTable;
    private JTable userTable;
    private JTable skladTable;
    private JTable uctenkyTable;
    private DefaultTableModel kafariTableModel;
    private DefaultTableModel userTableModel;
    private DefaultTableModel skladTableModel;
    private DefaultTableModel uctenkyTableModel;    
    private List<Kafar> kafari;
    private List<PolozkaSkladu> sklad;
    private List<Vyuctovani> seznamVyuctovani;
    private String prihlasenyUzivatel = null;
    private JPanel emptyPanel;
    private JPanel kafariPanel;
    private JPanel skladPanel;
    private JPanel uctenkyPanel;
    private JPanel welcomePanel;
    private JPanel userPanel;
    private JButton uctenkyButton;
    private JButton vypitButton;
    private JButton prihlasitButton;
    private JButton odhlasitButton;
    private JButton exportHistorieKafareButton;
    private JButton zalozitUzivateleButton;
    private JButton kafariButton;
    private JButton skladButton;
    private JButton naskladnitButton;
    private JButton vyuctovatButton;
    private Admin admin;

    public KafeGui(List<Kafar> kafari, List<PolozkaSkladu> sklad, List<Vyuctovani> seznamVyuctovani, Admin admin) {
        this.kafari = kafari;
        this.admin = admin;
        this.sklad = sklad;
        this.seznamVyuctovani = seznamVyuctovani;
        
        setTitle("KafeApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024,768);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        if (!SpravceSouboru.chybyIntegrity.isEmpty()) {
            String message = String.join("\n", SpravceSouboru.chybyIntegrity);
            JOptionPane.showMessageDialog(null, 
                "Při načítání dat došlo k chybám integrity:\n" + message, 
                "Kritická chyba dat", 
                JOptionPane.WARNING_MESSAGE);
        }
       
        String[] sloupceKafari = {"Uzivatel (login)", "Nezuctovane kavy", "Zaplacene kavy", "Uctovane kavy"};
        kafariTableModel = new DefaultTableModel(sloupceKafari, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };
        
        kafariPanel = new JPanel(new BorderLayout());
        kafariTable = new JTable(kafariTableModel);
        kafariTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        kafariTable.setFont(new Font("Arial", Font.PLAIN, 16));
        kafariTable.setRowHeight(30);
        kafariTable.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (column == 1 && row >= 0) {
                try {
                    Object newValue = kafariTableModel.getValueAt(row, column);
                    int novyPocet = Integer.parseInt(newValue.toString());

                    String login = (String) kafariTableModel.getValueAt(row, 0);
                    for (Kafar k : kafari) {
                        if (k.getLogin().equals(login)) {
                            k.setPocetVypitychKav(novyPocet);
                            SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
                            break;
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Zadejte prosím platné číslo!", "Chyba formátu", JOptionPane.ERROR_MESSAGE);
                    akcePrepnoutNaKafare(); 
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(kafariTable);
        kafariPanel.add(scrollPane, BorderLayout.CENTER);

        String[] sloupceUser = {"Uzivatel (login)", "Nezuctovane kavy", "Nezaplacene kavy", "Zaplacene kavy"};
        userTableModel = new DefaultTableModel(sloupceUser, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userPanel = new JPanel(new BorderLayout());
        userTable = new JTable(userTableModel);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        userTable.setFont(new Font("Arial", Font.PLAIN, 16));
        userTable.setRowHeight(30);
        JScrollPane userScrollPane = new JScrollPane(userTable); 
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        String[] sloupceSklad = {"ID", "Nazev", "<html>Koupene<br>mnozstvi</html>", "<html>Aktualni<br>mnozstvi</html>", "Jednotka", "Cena za kus", "Mena"};
        skladTableModel = new DefaultTableModel(sloupceSklad, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        skladPanel = new JPanel(new BorderLayout());
        skladTable = new JTable(skladTableModel);
        skladTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        skladTable.getTableHeader().setPreferredSize(new Dimension(0,50));
        skladTable.setFont(new Font("Arial", Font.PLAIN, 16));
        skladTable.setRowHeight(30);
        nastavOdsazeni(skladTable, 10);
        JScrollPane skladScrollPane = new JScrollPane(skladTable);
        skladPanel.add(skladScrollPane, BorderLayout.CENTER);

        String[] sloupceUctenky = {"Kafar", "Pocet kav", "Datum", "Cena jedne kavy", "Cena celkem", "Uhrazeno"};
        uctenkyTableModel = new DefaultTableModel(sloupceUctenky, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1: return Integer.class;
                    case 2: return LocalDate.class;
                    case 3: return BigDecimal.class;
                    case 4: return BigDecimal.class;
                    case 5: return Boolean.class;
                    default: return String.class;
                }
            }
        };

        uctenkyPanel = new JPanel(new BorderLayout());
        uctenkyTable = new JTable(uctenkyTableModel);
        uctenkyTable.setAutoCreateRowSorter(true);
        uctenkyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        uctenkyTable.getTableHeader().setPreferredSize(new Dimension(0,50));
        uctenkyTable.setFont(new Font("Arial", Font.PLAIN, 16));
        uctenkyTable.setRowHeight(30);
        uctenkyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = uctenkyTable.getSelectedRow();
                if (viewRow != -1) {
                    int selectedRow = uctenkyTable.convertRowIndexToModel(viewRow);
                    for (Vyuctovani v : seznamVyuctovani) {
                        if (v.getLogin().equals(uctenkyTableModel.getValueAt(selectedRow, 0))) {
                            if (!v.getStavPlatby() && 
                                v.getDatumVystaveni().equals(uctenkyTableModel.getValueAt(selectedRow, 2)) &&
                                v.getCenaZaVypiteKavy().equals(uctenkyTableModel.getValueAt(selectedRow, 4))) {
                                PlatbaDialog platbaDialog = new PlatbaDialog(this, v, admin, prihlasenyUzivatel);
                                platbaDialog.setVisible(true);
                                if (platbaDialog.isSucceeded()) {
                                    SpravceSouboru.ulozVyuctovani(v, prihlasenyUzivatel);
                                    akceUctenky();
                                }
                                break;
                            }
                        }
                    }
                }                        
            }
        });
        nastavOdsazeni(uctenkyTable, 10);
        JScrollPane uctenkyScrollPane = new JScrollPane(uctenkyTable);
        uctenkyPanel.add(uctenkyScrollPane, BorderLayout.CENTER);
        

        welcomePanel = new JPanel(new GridBagLayout());
        JLabel vzkaz = new JLabel("Pro přístup se prosím přihlaste.");
        vzkaz.setFont(new Font("Arial", Font.BOLD, 16));
        welcomePanel.add(vzkaz);

        emptyPanel = new JPanel(new BorderLayout());
        add(emptyPanel, BorderLayout.CENTER);

        JPanel panelTlacitek = new JPanel();
        
        vypitButton = new JButton("Vypit kavu");
        prihlasitButton = new JButton("Prihlasit");
        odhlasitButton = new JButton("Odhlasit");
        zalozitUzivateleButton = new JButton("Vytvorit noveho uzivatele");
        exportHistorieKafareButton = new JButton("Exportuj historii");

        vypitButton.addActionListener(e -> akceVypitKavu());
        prihlasitButton.addActionListener(e -> akcePrihlasit());
        odhlasitButton.addActionListener(e -> akceOdhlasit());
        zalozitUzivateleButton.addActionListener(e -> akceZalozitUzivatele());
        exportHistorieKafareButton.addActionListener(e -> akceExportHistorieKafare());

        panelTlacitek.add(vypitButton);
        panelTlacitek.add(prihlasitButton);
        panelTlacitek.add(odhlasitButton);
        panelTlacitek.add(zalozitUzivateleButton);
        panelTlacitek.add(exportHistorieKafareButton);

        add(panelTlacitek, BorderLayout.SOUTH);

        JPanel panelTlacitekAdmina = new JPanel();
        kafariButton = new JButton("Kafari");
        skladButton = new JButton("Sklad");
        naskladnitButton = new JButton("Naskladnit");
        vyuctovatButton = new JButton("Vyuctovat");
        uctenkyButton = new JButton("Uctenky");
        
        kafariButton.addActionListener(e -> akcePrepnoutNaKafare());
        skladButton.addActionListener(e -> akcePrepnoutNaSklad());
        naskladnitButton.addActionListener(e -> akceNaskladnit());
        vyuctovatButton.addActionListener(e -> akceVyuctovat());
        uctenkyButton.addActionListener(e -> akceUctenky());

        panelTlacitekAdmina.add(kafariButton);
        panelTlacitekAdmina.add(skladButton);
        panelTlacitekAdmina.add(naskladnitButton);
        panelTlacitekAdmina.add(vyuctovatButton);
        panelTlacitekAdmina.add(uctenkyButton);

        add(panelTlacitekAdmina, BorderLayout.NORTH);

        updateView();
        setVisible(true);
    }

    private void akceExportHistorieKafare() {
        List<Vyuctovani> historieVyuctovani = new ArrayList<>();
        for (Vyuctovani v : seznamVyuctovani) {
            if(v.getLogin().equals(prihlasenyUzivatel)) {
                historieVyuctovani.add(v);
            }
        }
        if (historieVyuctovani.isEmpty()) {
            return;
        }
        ExportHistorieKafareDialog exportHistorieKafareDialog = new ExportHistorieKafareDialog(this, historieVyuctovani);
        exportHistorieKafareDialog.setVisible(true);
        if (exportHistorieKafareDialog.isSucceeded()) {
            System.out.println("Export historie kafare: " + prihlasenyUzivatel + " se povedl.");
        }
    }

    private void akceNaskladnit() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            NovaPolozkaDialog novaPolozkaDialog = new NovaPolozkaDialog(this);
            novaPolozkaDialog.setVisible(true);

            if (novaPolozkaDialog.isSucceeded()) {
                PolozkaSkladu p = novaPolozkaDialog.getPolozka();
                sklad.add(p);
                SpravceSouboru.ulozPolozkuNaSklad(p, prihlasenyUzivatel);
            }
        }
        updateView();
        akcePrepnoutNaSklad();
    }

    private void akceVyuctovat() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            VyuctovaniDialog vyuctovaniDialog = new VyuctovaniDialog(this, sklad, kafari, prihlasenyUzivatel);
            vyuctovaniDialog.setVisible(true);

            if (vyuctovaniDialog.isSucceeded()) {
                System.out.println("Vyuctovani ulozeno.");
            }
        }
        seznamVyuctovani = SpravceSouboru.nactiVyuctovani();
        updateView();
        akceUctenky();        
    }

    private void akceVypitKavu() {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(prihlasenyUzivatel)) {
                k.vypijKavu();
                userTableModel.setValueAt(k.getPocetVypitychKav(), 0, 1);
                SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
            }
        }
    }        
    
    private void akceUctenky() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            uctenkyTableModel.setRowCount(0);
            for (Vyuctovani v : seznamVyuctovani) {
                uctenkyTableModel.addRow(new Object[]{v.getLogin(), v.getPocetVypitychKav(), v.getDatumVystaveni(),
                                v.getCenaJedneKavy(), v.getCenaZaVypiteKavy(), v.getStavPlatby()});
            }
            updateView();
            emptyPanel.add(uctenkyPanel);
            return;
        }
    }

    private void akcePrihlasit() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);

        if (loginDialog.isSucceeded()) {
            String login = loginDialog.getLogin();
            String heslo = loginDialog.getHeslo();
            String hesloHash = Uzivatel.hashHeslo(heslo);
            for (Kafar k : kafari) {
                if (k.getLogin().equals(login) && k.getHesloHash().equals(hesloHash)) {
                    prihlasenyUzivatel = login;
                    updateView();
                    return;
                }
            }
            if (admin.getLogin().equals(login) && admin.getHesloHash().equals(hesloHash)) {
                prihlasenyUzivatel = login;
                akcePrepnoutNaKafare();
                return;
            }
            JOptionPane.showMessageDialog(this, "Spatny login nebo heslo!", "Chyba", JOptionPane.ERROR_MESSAGE);            
        }
    }

    private void akceOdhlasit() {
        prihlasenyUzivatel = null;
        updateView();
    }

    private void akceZalozitUzivatele() {
        NewUserDialog newUserDialog = new NewUserDialog(this, kafari);
        newUserDialog.setVisible(true);

        if (newUserDialog.isSucceeded()) {
            String login = newUserDialog.getLogin();
            String heslo = newUserDialog.getHeslo();
            Kafar k = new Kafar(login, heslo);
            kafari.add(k);
            SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
            updateView();
        }
    }

    private void akcePrepnoutNaKafare() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            kafariTableModel.setRowCount(0);
            for (Kafar k : kafari) {
                int nezuctovaneKavy = k.getPocetVypitychKav();
                int zaplaceneKavy = 0;
                int uctovaneKavy = 0;
                for (Vyuctovani v : seznamVyuctovani) {
                    if (v.getLogin().equals(k.getLogin())) {
                        if (v.getStavPlatby()) {
                            zaplaceneKavy += v.getPocetVypitychKav();
                        }
                        else {                            
                            uctovaneKavy += v.getPocetVypitychKav();
                        }                        
                    }
                }
                kafariTableModel.addRow(new Object[]{k.getLogin(), nezuctovaneKavy, zaplaceneKavy, uctovaneKavy});
            }
            updateView();
            emptyPanel.add(kafariPanel);
            return;
        }
    }

    private void akcePrepnoutNaSklad() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            skladTableModel.setRowCount(0);
            for (PolozkaSkladu p : sklad) {
                skladTableModel.addRow(new Object[]{p.getId(),p.getNazev(), p.getKoupeneMnozstvi(), p.getAktualniMnozstvi(),
                    p.getJednotka(), p.getCenaZaKus(), p.getMenaPenezni()});
            }
            updateView();
            emptyPanel.add(skladPanel);
            return;
        }
    }

    private void setButtonsVisible(boolean uctenky, boolean vypit, boolean odhlasit, boolean prihlasit, boolean zalozit, boolean kafari,
            boolean sklad, boolean naskladnit, boolean vyuctovat, boolean expHistKafare) {
        uctenkyButton.setVisible(uctenky);
        vypitButton.setVisible(vypit);
        odhlasitButton.setVisible(odhlasit);
        prihlasitButton.setVisible(prihlasit);
        zalozitUzivateleButton.setVisible(zalozit);
        kafariButton.setVisible(kafari);
        skladButton.setVisible(sklad);
        naskladnitButton.setVisible(naskladnit);
        vyuctovatButton.setVisible(vyuctovat);
        exportHistorieKafareButton.setVisible(expHistKafare);
    }

    private void updateView() {
        emptyPanel.removeAll();

        if (prihlasenyUzivatel == null) {
            emptyPanel.add(welcomePanel);
            setButtonsVisible(false, false, false, true, true, false, false, false, false, false);
        } else if (prihlasenyUzivatel.equals(admin.getLogin())) {
            setButtonsVisible(true, false, true, false, true, true, true, true, true, false);
        }
        else {
            userTableModel.setRowCount(0);
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    int nezuctovaneKavy = k.getPocetVypitychKav();
                    int nezaplaceneKavy = 0;
                    int zaplaceneKavy = 0;
                                                            
                    uctenkyTableModel.setRowCount(0);
                    for (Vyuctovani v : seznamVyuctovani) {
                        if (v.getLogin().equals(prihlasenyUzivatel)) {
                            if (v.getStavPlatby()) {
                                zaplaceneKavy += v.getPocetVypitychKav();
                            }
                            else {
                                nezaplaceneKavy += v.getPocetVypitychKav();
                            }
                            uctenkyTableModel.addRow(new Object[]{v.getLogin(), v.getPocetVypitychKav(), v.getDatumVystaveni(),
                                v.getCenaJedneKavy(), v.getCenaZaVypiteKavy(), v.getStavPlatby()});                            
                        }
                    }

                    userTableModel.addRow(new Object[]{k.getLogin(), nezuctovaneKavy, nezaplaceneKavy, zaplaceneKavy});
                    
                    JPanel souhrnyPanel = new JPanel(new GridLayout(2, 1, 0, 10));
                    souhrnyPanel.add(userPanel);
                    souhrnyPanel.add(uctenkyPanel);
                    emptyPanel.add(souhrnyPanel);

                    setButtonsVisible(false, true, true, false, false, false, false, false, false, true);
                }
            }
        }
        emptyPanel.revalidate();
        emptyPanel.repaint();
    }

    private void nastavOdsazeni(JTable table, int padding) {
        var margin = javax.swing.BorderFactory.createEmptyBorder(0, padding, 0, padding);
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBorder(margin);
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnClass(i) != Boolean.class) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }
    }
}