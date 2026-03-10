package cz.marakvaclav;

import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class KafeGui extends JFrame {
    private JTable adminTable;
    private JTable userTable;
    private DefaultTableModel adminTableModel;
    private DefaultTableModel userTableModel;
    private List<Kafar> kafari;
    private String prihlasenyUzivatel = null;
    private JPanel emptyPanel;
    private JPanel adminPanel;
    private JPanel welcomePanel;
    private JPanel userPanel;
    private JButton zaplatitButton;
    private JButton vypitButton;
    private JButton prihlasitButton;
    private JButton odhlasitButton;
    private Admin admin;


    public KafeGui(List<Kafar> kafari, Admin admin) {
        this.kafari = kafari;
        this.admin = admin;
        
        setTitle("KafeApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600,400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        adminPanel = new JPanel(new BorderLayout());
        
        String[] sloupce = {"Uzivatel (login)", "Nezaplacene kavy", "Zaplacene kavy", "Suroviny na sklade"};
        adminTableModel = new DefaultTableModel(sloupce, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Kafar k : kafari) {
            adminTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
        }

        String[] sloupce2 = {"Uzivatel (login)", "Nezaplacene kavy", "Zaplacene kavy"};
        userTableModel = new DefaultTableModel(sloupce2, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        adminTable = new JTable(adminTableModel);
        adminTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(adminTable);
        adminPanel.add(scrollPane, BorderLayout.CENTER);

        userPanel = new JPanel(new BorderLayout());
        userTable = new JTable(userTableModel);
        JScrollPane userScrollPane = new JScrollPane(userTable); 
        userPanel.add(userScrollPane, BorderLayout.CENTER);

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

        zaplatitButton.addActionListener(e -> akceZaplatit());
        vypitButton.addActionListener(e -> akceVypitKavu());
        prihlasitButton.addActionListener(e -> akcePrihlasit());
        odhlasitButton.addActionListener(e -> akceOdhlasit());

        panelTlacitek.add(zaplatitButton);
        panelTlacitek.add(vypitButton);
        panelTlacitek.add(prihlasitButton);
        panelTlacitek.add(odhlasitButton);

        add(panelTlacitek, BorderLayout.SOUTH);

        updateView();
        setVisible(true);
    }

    private void akceVypitKavu() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            int vybranyRadek = adminTable.getSelectedRow();
            if (vybranyRadek >= 0) {
                Kafar k = kafari.get(vybranyRadek);
                k.vypijKavu();
                adminTableModel.setValueAt(k.getPocetVypitychKav(), vybranyRadek, 1);
            }
        }
        else {
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    k.vypijKavu();
                    userTableModel.setValueAt(k.getPocetVypitychKav(), 0, 1);
                }
            }
        }        
    }

    private void akceZaplatit() {
        if (prihlasenyUzivatel.equals(admin.getLogin())) {
            int vybranyRadek = adminTable.getSelectedRow();
            if (vybranyRadek >= 0) {
                Kafar k = kafari.get(vybranyRadek);
                k.zaplatit();
                adminTableModel.setValueAt(k.getPocetVypitychKav(), vybranyRadek, 1);
            }
        }
        else {
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    k.zaplatit();
                    userTableModel.setValueAt(k.getPocetVypitychKav(), 0, 1);
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
            System.out.println("Pokus o prihlaseni: " + login + " s heslem: " + heslo); //odstranit v ostre verzi
            for (Kafar k : kafari) {
                if (k.getLogin().equals(login) && k.getHesloHash().equals(hesloHash)) {
                    prihlasenyUzivatel = login;
                    updateView();
                    return;
                }
            }
            if (admin.getLogin().equals(login) && admin.getHesloHash().equals(hesloHash)) {
                prihlasenyUzivatel = login;
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

    private void updateView() {
        emptyPanel.removeAll();

        if (prihlasenyUzivatel == null) {
            emptyPanel.add(welcomePanel);
            zaplatitButton.setVisible(false);
            vypitButton.setVisible(false);
            odhlasitButton.setVisible(false);
            prihlasitButton.setVisible(true);
        } else if (prihlasenyUzivatel.equals(admin.getLogin())) {
            emptyPanel.add(adminPanel);
            zaplatitButton.setVisible(true);
            vypitButton.setVisible(true);
            odhlasitButton.setVisible(true);
            prihlasitButton.setVisible(false);
        }
        else {
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    userTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
                    emptyPanel.add(userPanel);
                    zaplatitButton.setVisible(true);
                    vypitButton.setVisible(true);
                    odhlasitButton.setVisible(true);
                    prihlasitButton.setVisible(false);
                }
            }
        }
        emptyPanel.revalidate();
        emptyPanel.repaint();
    }
}