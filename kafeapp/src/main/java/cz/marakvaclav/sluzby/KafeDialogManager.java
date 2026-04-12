package cz.marakvaclav.sluzby;

import cz.marakvaclav.dialogy.*;
import cz.marakvaclav.entity.Admin;
import cz.marakvaclav.entity.Vyuctovani;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class KafeDialogManager {
    private final Frame parentComponent;
    private final KafeUIController controller;

    public KafeDialogManager(Frame parentComponent, KafeUIController controller) {
        this.parentComponent = parentComponent;
        this.controller = controller;
    }

    public void zobrazChybu(String zprava) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> zobrazChybu(zprava));
            return;
        }
        JOptionPane.showMessageDialog(parentComponent, zprava, "Chyba", JOptionPane.ERROR_MESSAGE);
    }

    public void zobrazInformaci(String zprava) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> zobrazInformaci(zprava));
            return;
        }
        JOptionPane.showMessageDialog(parentComponent, zprava, "Informace", JOptionPane.INFORMATION_MESSAGE);
    }

    public void otevriExportHistorieDialog(List<Vyuctovani> historie) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> otevriExportHistorieDialog(historie));
            return;
        }
        ExportHistorieKafareDialog dialog = new ExportHistorieKafareDialog(parentComponent instanceof JFrame ? (JFrame) parentComponent : null, historie);
        dialog.setVisible(true);
    }

    public String vyberSlozku(String titulek, String vychoziSlozka) {
        final String[] result = new String[1];
        Runnable r = () -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle(titulek);
            if (vychoziSlozka != null && !vychoziSlozka.isEmpty()) {
                chooser.setCurrentDirectory(new File(vychoziSlozka));
            } else {
                chooser.setCurrentDirectory(new File("."));
            }
            if (chooser.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
                result[0] = chooser.getSelectedFile().getAbsolutePath();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try { SwingUtilities.invokeAndWait(r); } catch (Exception e) { e.printStackTrace(); }
        }
        return result[0];
    }

    public void otevriPlatbu(Vyuctovani v) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> otevriPlatbu(v));
            return;
        }
        PlatbaDialog platbaDialog = new PlatbaDialog(parentComponent, v, controller.getAdmin(), controller.getPrihlasenyUzivatel(), (iban, cz) -> {
            controller.aktualizujPlatebniUdajeAdmina(iban, cz);
        }, () -> {
            controller.stornoPlatby(v);
        }, () -> {
            controller.oznamitPlatbu(v);
        });
        platbaDialog.setVisible(true);
        if (platbaDialog.isSucceeded()) {
            controller.zpracujPlatbu(v);
        }
    }

    public Admin vyzadejNovehoAdmina(KafeUIController.AdminFactory factory) {
        final Admin[] result = new Admin[1];
        Runnable r = () -> {
            VytvoreniAdminaDialog dialog = new VytvoreniAdminaDialog(parentComponent, factory::create);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) result[0] = dialog.getAdmin();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try { SwingUtilities.invokeAndWait(r); } catch (Exception e) { e.printStackTrace(); }
        }
        return result[0];
    }

    public char[] vyzadejVynucenouZmenuHesla() {
        final char[][] result = new char[1][];
        Runnable r = () -> {
            VynucenaZmenaHeslaDialog dialog = new VynucenaZmenaHeslaDialog(parentComponent);
            dialog.setVisible(true);
            if (dialog.isSucceeded()) {
                result[0] = dialog.getNoveHeslo();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try { SwingUtilities.invokeAndWait(r); } catch (Exception e) { e.printStackTrace(); }
        }
        return result[0];
    }
}