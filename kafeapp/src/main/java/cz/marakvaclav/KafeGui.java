package cz.marakvaclav;

import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class KafeGui extends JFrame {
    private JTable kafariTable;
    private JTable userTable;
    private JTable skladTable;
    private DefaultTableModel kafariTableModel;
    private DefaultTableModel userTableModel;
    private DefaultTableModel skladTableModel;
    private List<Kafar> kafari;
    private List<PolozkaSkladu> sklad;
    private String prihlasenyUzivatel = null;
    private JPanel emptyPanel;
    private JPanel kafariPanel;
    private JPanel skladPanel;
    private JPanel welcomePanel;
    private JPanel userPanel;
    private JButton zaplatitButton;
    private JButton vypitButton;
    private JButton prihlasitButton;
    private JButton odhlasitButton;
    private JButton zalozitUzivateleButton;
    private JButton kafariButton;
    private JButton skladButton;
    private JButton naskladnitButton;
    private JButton vyuctovatButton;
    private Admin admin;

    public KafeGui(List<Kafar> kafari, List<PolozkaSkladu> sklad, Admin admin) {
        this.kafari = kafari;
        this.admin = admin;
        this.sklad = sklad;
        
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
       
        String[] sloupce = {"Uzivatel (login)", "Nezaplacene kavy", "Zaplacene kavy", "Uctovane kavy"};
        kafariTableModel = new DefaultTableModel(sloupce, 0) {
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

        String[] sloupce2 = {"Uzivatel (login)", "Nezaplacene kavy", "Zaplacene kavy"};
        userTableModel = new DefaultTableModel(sloupce2, 0) {
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

        String[] sloupce3 = {"ID", "Nazev", "<html>Koupene<br>mnozstvi</html>", "<html>Aktualni<br>mnozstvi</html>", "Jednotka", "Cena za kus", "Mena"};
        skladTableModel = new DefaultTableModel(sloupce3, 0) {
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
        JScrollPane skladScrollPane = new JScrollPane(skladTable);
        skladPanel.add(skladScrollPane, BorderLayout.CENTER);

        welcomePanel = new JPanel(new GridBagLayout());
        JLabel vzkaz = new JLabel("Pro přístup se prosím přihlaste.");
        vzkaz.setFont(new Font("Arial", Font.BOLD, 16));
        welcomePanel.add(vzkaz);

        emptyPanel = new JPanel(new BorderLayout());
        add(emptyPanel, BorderLayout.CENTER);

        JPanel panelTlacitek = new JPanel();
        zaplatitButton = new JButton("Zaplatit");
        vypitButton = new JButton("Vypit kavu");
        prihlasitButton = new JButton("Prihlasit");
        odhlasitButton = new JButton("Odhlasit");
        zalozitUzivateleButton = new JButton("Vytvorit noveho uzivatele");

        zaplatitButton.addActionListener(e -> akceZaplatit());
        vypitButton.addActionListener(e -> akceVypitKavu());
        prihlasitButton.addActionListener(e -> akcePrihlasit());
        odhlasitButton.addActionListener(e -> akceOdhlasit());
        zalozitUzivateleButton.addActionListener(e -> akceZalozitUzivatele());

        panelTlacitek.add(zaplatitButton);
        panelTlacitek.add(vypitButton);
        panelTlacitek.add(prihlasitButton);
        panelTlacitek.add(odhlasitButton);
        panelTlacitek.add(zalozitUzivateleButton);

        add(panelTlacitek, BorderLayout.SOUTH);

        JPanel panelTlacitekAdmina = new JPanel();
        kafariButton = new JButton("Kafari");
        skladButton = new JButton("Sklad");
        naskladnitButton = new JButton("Naskladnit");
        vyuctovatButton = new JButton("Vyuctovat");
        
        kafariButton.addActionListener(e -> akcePrepnoutNaKafare());
        skladButton.addActionListener(e -> akcePrepnoutNaSklad());
        naskladnitButton.addActionListener(e -> akceNaskladnit());
        vyuctovatButton.addActionListener(e -> akceVyuctovat());

        panelTlacitekAdmina.add(kafariButton);
        panelTlacitekAdmina.add(skladButton);
        panelTlacitekAdmina.add(naskladnitButton);
        panelTlacitekAdmina.add(vyuctovatButton);

        add(panelTlacitekAdmina, BorderLayout.NORTH);

        updateView();
        setVisible(true);
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
        System.out.println("Dodelat akceVyuctovat()");
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            VyuctovaniDialog vyuctovaniDialog = new VyuctovaniDialog(this, sklad, kafari, prihlasenyUzivatel);
            vyuctovaniDialog.setVisible(true);

            if (vyuctovaniDialog.isSucceeded()) {
                System.out.println("dopsat zobrazeni vyuctovani na panel");
            }
        }
        updateView();
        akcePrepnoutNaKafare();        
    }

    private void akceVypitKavu() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            int vybranyRadek = kafariTable.getSelectedRow();
            if (vybranyRadek >= 0) {
                Kafar k = kafari.get(vybranyRadek);
                k.vypijKavu();
                kafariTableModel.setValueAt(k.getPocetVypitychKav(), vybranyRadek, 1);
                SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
            }
        }
        else {
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    k.vypijKavu();
                    userTableModel.setValueAt(k.getPocetVypitychKav(), 0, 1);
                    SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
                }
            }
        }        
    }

    private void akceZaplatit() {
        for (Kafar k : kafari) {
            if (k.getLogin().equals(prihlasenyUzivatel)) {
                k.zaplatit();
                userTableModel.setValueAt(k.getPocetVypitychKav(), 0, 1);
                SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
            }
        }
    }

    private void akcePrihlasit() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);

        if (loginDialog.isSucceeded()) {
            String login = loginDialog.getLogin();
            String heslo = loginDialog.getHeslo();
            String hesloHash = Uzivatel.hashHeslo(heslo);
            System.out.println("Pokus o prihlaseni: " + login + " s heslem: " + heslo + " hash: " + hesloHash); //odstranit v ostre verzi
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
                kafariTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
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

    private void setButtonsVisible(boolean zaplatit, boolean vypit, boolean odhlasit, boolean prihlasit, boolean zalozit, boolean kafari,
            boolean sklad, boolean naskladnit, boolean vyuctovat) {
        zaplatitButton.setVisible(zaplatit);
        vypitButton.setVisible(vypit);
        odhlasitButton.setVisible(odhlasit);
        prihlasitButton.setVisible(prihlasit);
        zalozitUzivateleButton.setVisible(zalozit);
        kafariButton.setVisible(kafari);
        skladButton.setVisible(sklad);
        naskladnitButton.setVisible(naskladnit);
        vyuctovatButton.setVisible(vyuctovat);
    }

    private void updateView() {
        emptyPanel.removeAll();

        if (prihlasenyUzivatel == null) {
            emptyPanel.add(welcomePanel);
            setButtonsVisible(false, false, false, true, true, false, false, false, false);
        } else if (prihlasenyUzivatel.equals(admin.getLogin())) {
            setButtonsVisible(false, false, true, false, true, true, true, true, true);
        }
        else {
            userTableModel.setRowCount(0);
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    userTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
                    emptyPanel.add(userPanel);
                    setButtonsVisible(true, true, true, false, false, false, false, false, false);
                }
            }
        }
        emptyPanel.revalidate();
        emptyPanel.repaint();
    }
}