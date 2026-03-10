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
    private JButton zalozitUzivateleButton;
    private Admin admin;

    public KafeGui(List<Kafar> kafari, Admin admin) {
        this.kafari = kafari;
        this.admin = admin;
        
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

        adminPanel = new JPanel(new BorderLayout());
        
        String[] sloupce = {"Uzivatel (login)", "Nezaplacene kavy", "Zaplacene kavy", "Suroviny na sklade"};
        adminTableModel = new DefaultTableModel(sloupce, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

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
            int vybranyRadek = adminTable.getSelectedRow();
            if (vybranyRadek >= 0) {
                Kafar k = kafari.get(vybranyRadek);
                k.zaplatit();
                adminTableModel.setValueAt(k.getPocetVypitychKav(), vybranyRadek, 1);
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
                adminTableModel.setRowCount(0);
                for (Kafar k : kafari) {
                    adminTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
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

    private void setButtonsVisible(boolean zaplatit, boolean vypit, boolean odhlasit, boolean prihlasit, boolean zalozit) {
        zaplatitButton.setVisible(zaplatit);
        vypitButton.setVisible(vypit);
        odhlasitButton.setVisible(odhlasit);
        prihlasitButton.setVisible(prihlasit);
        zalozitUzivateleButton.setVisible(zalozit);
    }

    private void updateView() {
        emptyPanel.removeAll();

        if (prihlasenyUzivatel == null) {
            emptyPanel.add(welcomePanel);
            setButtonsVisible(false, false, false, true, true);
        } else if (prihlasenyUzivatel.equals(admin.getLogin())) {
            emptyPanel.add(adminPanel);
            setButtonsVisible(true, false, true, false, true);
        }
        else {
            userTableModel.setRowCount(0);
            for (Kafar k : kafari) {
                if (k.getLogin().equals(prihlasenyUzivatel)) {
                    userTableModel.addRow(new Object[]{k.getLogin(), k.getPocetVypitychKav()});
                    emptyPanel.add(userPanel);
                    setButtonsVisible(true, true, true, false, false);
                }
            }
        }
        emptyPanel.revalidate();
        emptyPanel.repaint();
    }
}