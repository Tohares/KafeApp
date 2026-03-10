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
                return false;
            }
        };
        
        kafariPanel = new JPanel(new BorderLayout());
        kafariTable = new JTable(kafariTableModel);
        kafariTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        JScrollPane userScrollPane = new JScrollPane(userTable); 
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        String[] sloupce3 = {"Nazev", "Koupene mnozstvi", "Aktualni mnozstvi", "Jednotka", "Cena za kus", "Mena"};
        skladTableModel = new DefaultTableModel(sloupce3, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        skladPanel = new JPanel(new BorderLayout());
        skladTable = new JTable(skladTableModel);
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

        JPanel panelPrepinacuAdmina = new JPanel();
        kafariButton = new JButton("Kafari");
        skladButton = new JButton("Sklad");
        
        kafariButton.addActionListener(e -> akcePrepnoutNaKafare());
        skladButton.addActionListener(e -> akcePrepnoutNaSklad());

        panelPrepinacuAdmina.add(kafariButton);
        panelPrepinacuAdmina.add(skladButton);

        add(panelPrepinacuAdmina, BorderLayout.NORTH);

        updateView();
        setVisible(true);
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
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            int vybranyRadek = kafariTable.getSelectedRow();
            if (vybranyRadek >= 0) {
                Kafar k = kafari.get(vybranyRadek);
                k.zaplatit();
                kafariTableModel.setValueAt(k.getPocetVypitychKav(), vybranyRadek, 1);
                SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
            }
        }
        else {
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    k.zaplatit();
                    userTableModel.setValueAt(k.getPocetVypitychKav(), 0, 1);
                    SpravceSouboru.ulozKafare(k, prihlasenyUzivatel);
                }
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
                kafariTableModel.setRowCount(0);
                for (Kafar k : kafari) {
                    kafariTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
                }
                updateView();
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
                skladTableModel.addRow(new Object[]{p.getNazev(), p.getKoupeneMnozstvi(), p.getAktualniMnozstvi(), p.getJednotka(), 
                    p.getCenaZaKus(), p.getMenaPenezni()});
            }
            updateView();
            emptyPanel.add(skladPanel);
            return;
        }
    }

    private void setButtonsVisible(boolean zaplatit, boolean vypit, boolean odhlasit, boolean prihlasit, boolean zalozit, boolean kafari, boolean sklad) {
        zaplatitButton.setVisible(zaplatit);
        vypitButton.setVisible(vypit);
        odhlasitButton.setVisible(odhlasit);
        prihlasitButton.setVisible(prihlasit);
        zalozitUzivateleButton.setVisible(zalozit);
        kafariButton.setVisible(kafari);
        skladButton.setVisible(sklad);
    }

    private void updateView() {
        emptyPanel.removeAll();

        if (prihlasenyUzivatel == null) {
            emptyPanel.add(welcomePanel);
            setButtonsVisible(false, false, false, true, true, false, false);
        } else if (prihlasenyUzivatel.equals(admin.getLogin())) {
            setButtonsVisible(true, false, true, false, true, true, true);
        }
        else {
            userTableModel.setRowCount(0);
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    userTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
                    emptyPanel.add(userPanel);
                    setButtonsVisible(true, true, true, false, false, false, false);
                }
            }
        }
        emptyPanel.revalidate();
        emptyPanel.repaint();
    }
}