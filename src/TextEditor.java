import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;

/**
 * Created by Łukasz on 2016-11-24.
 */
public class TextEditor extends JFrame {
    private JButton newFileButton;
    private JPanel mainPanel;
    private JMenuBar menuBar;
    private JButton openFileButton;
    private JButton saveFileButton;
    private JEditorPane editorPane;
    private JButton saveAsButton;
    private JButton cutButton;
    private JButton copyButton;
    private JButton pasteButton;
    private JButton newRTFButton;
    private JButton boldButton;
    private JButton italicButton;
    private JButton underlineButton;
    private JComboBox fontComboBox;


    private static int windowsCounter = 0;
    private File currentFile = null;
    private String mode = null;

    public TextEditor(String type) {
        setContentPane(mainPanel);
        menuBar = new MenuBarBuilder().menuBar;
        createMenusListeners();
        setJMenuBar(menuBar);
        setTitle("Untitled - TextEditor");
        System.out.println(type);
        if(type.equals("text/plain")){
            mode = "PlainTextMode";
            EventQueue.invokeLater(new LockFontElements());
        }
        else{
            mode = "RichTextMode";
            EventQueue.invokeLater(new UnlockFontElements());
        }
        setTitle("Untitled - " + mode + " - TextEditor");
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for(String fontName: GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()){
            fontComboBox.addItem(fontName);
        }
        fontComboBox.setSelectedItem("Arial");
        editorPane.setFont(new Font("Arial", Font.BOLD, 20));
        System.out.println(Font.getFont("Tahoma"));
        windowsCounter++;
        editorPane.setEditorKit(editorPane.getEditorKitForContentType(type));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public static void main(String[] args) {
        System.out.println(args.length);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame;
        if (args.length == 0) {
            frame = new TextEditor("text/plain");
        } else {
            frame = new TextEditor(args[0]);
        }
        frame.pack();
        frame.setVisible(true);
        if (windowsCounter == 0) {
            System.exit(0);
        }
    }

    private void createMenusListeners() {
        JMenu menu = menuBar.getMenu(0);
        ActionListener al = new NewActionListener();
        menu.getItem(0).addActionListener(al);
        newFileButton.addActionListener(al);
        al = new NewRTFActionListener();
        menu.getItem(1).addActionListener(al);
        newRTFButton.addActionListener(al);
        al = new OpenActionListener();
        menu.getItem(2).addActionListener(al);
        openFileButton.addActionListener(al);
        al = new SaveActionListener();
        menu.getItem(3).addActionListener(al);
        saveFileButton.addActionListener(al);
        al = new SaveAsActionListener();
        menu.getItem(4).addActionListener(al);
        saveAsButton.addActionListener(al);
        menu.getItem(5).addActionListener(new ExitActionListener());
        menu = menuBar.getMenu(1);
        al = new CutActionListener();
        menu.getItem(0).addActionListener(al);
        cutButton.addActionListener(al);
        al = new CopyActionListener();
        menu.getItem(1).addActionListener(al);
        copyButton.addActionListener(al);
        al = new PasteActionListener();
        menu.getItem(2).addActionListener(al);
        pasteButton.addActionListener(al);
        menu = menuBar.getMenu(2);
        menu.getItem(0).addActionListener(new AboutActionListener());
        boldButton.addActionListener(new BoldActionListener());
        italicButton.addActionListener(new ItalicActionListener());
        underlineButton.addActionListener(new UnderlineActionListener());
    }

    private class MenuBarBuilder {
        private JMenuBar menuBar;

        private MenuBarBuilder() {
            JMenu menu;
            JMenuItem menuItem;
            menuBar = new JMenuBar();
            menu = new JMenu("File");
            menu.setMnemonic(KeyEvent.VK_F);
            menu.add(new JMenuItem("New..."));
            menu.add(new JMenuItem("New RTF..."));
            menu.add(new JMenuItem("Open..."));
            menu.add(new JMenuItem("Save..."));
            menu.add(new JMenuItem("Save as..."));
            menu.add(new JMenuItem("Exit"));
            menuBar.add(menu);
            menu = new JMenu("Edit");
            menu.setMnemonic(KeyEvent.VK_E);
            menu.add(new JMenuItem("Cut..."));
            menu.add(new JMenuItem("Copy..."));
            menu.add(new JMenuItem("Paste..."));
            menuBar.add(menu);
            menu = new JMenu("Help");
            menu.setMnemonic(KeyEvent.VK_H);
            menu.add(new JMenuItem("About..."));
            menuBar.add(menu);
        }
    }

    private class LockFontElements implements Runnable {
        @Override
        public void run() {
            boldButton.setEnabled(false);
            italicButton.setEnabled(false);
            underlineButton.setEnabled(false);
            fontComboBox.setEnabled(false);
        }
    }

    private class UnlockFontElements implements Runnable {
        @Override
        public void run() {
            boldButton.setEnabled(true);
            italicButton.setEnabled(true);
            underlineButton.setEnabled(true);
            fontComboBox.setEnabled(true);
        }
    }

    @Override
    public void dispose() {
        windowsCounter--;
        super.dispose();
    }

    private class NewActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[1];
            args[0] = "text/plain";
            main(args);
        }
    }

    private class NewRTFActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[1];
            args[0] = "text/rtf";
            main(args);
        }
    }

    private class OpenActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                EventQueue.invokeLater(new OpenFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Cannot open the file");
            }
        }
    }

    private class OpenFile implements Runnable {
        public void run() {
            JFileChooser fc = new JFileChooser("~");
            if (fc.showDialog(mainPanel, "Open file") == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                if (new FileNameExtensionFilter("RTF File", "rtf").accept(currentFile)) {
                    editorPane.setEditorKit(editorPane.getEditorKitForContentType("text/rtf"));
                } else {
                    editorPane.setEditorKit(editorPane.getEditorKitForContentType("text/plain"));
                }
                try {
                    FileReader fr = new FileReader(currentFile);
                    editorPane.getEditorKit().read(fr, editorPane.getDocument(), 0);
                    fr.close();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainPanel, "Cannot open this file!");
                }
                setTitle(currentFile.getName() + " - TextEditor");
            }
            System.out.println(currentFile);
        }
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //JOptionPane.showMessageDialog(mainPanel, "SAVE");
            if (currentFile == null) {
                EventQueue.invokeLater(new SaveAs());
            } else {
                EventQueue.invokeLater(new SaveFile());
            }
        }
    }

    private class SaveFile implements Runnable {
        public void run() {
            try {
                if (editorPane.getContentType().equals("text/plain")) {
                    FileWriter fw = new FileWriter(currentFile);
                    editorPane.getEditorKit().write(fw, editorPane.getDocument(), 0, editorPane.getDocument().getLength());
                    fw.close();
                } else {
                    FileOutputStream fos = new FileOutputStream(currentFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    editorPane.getEditorKit().write(bos, editorPane.getDocument(), 0, editorPane.getDocument().getLength());
                    bos.close();
                    fos.close();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainPanel, "Cannot save file!");
            }
        }
    }

    private class SaveAsActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(new SaveAs());
        }
    }

    private class SaveAs implements Runnable {
        @Override
        public void run() {
            JFileChooser fc = new JFileChooser("~");
            if (fc.showDialog(mainPanel, "Save file") == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
                try {
                    if (editorPane.getContentType().equals("text/rtf")) {
                        if (!currentFile.getPath().matches("(.*)[.]rtf")) {
                            currentFile = new File(currentFile.getPath() + ".rtf");
                        }
                        FileOutputStream fos = new FileOutputStream(currentFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        editorPane.getEditorKit().write(bos, editorPane.getDocument(), 0, editorPane.getDocument().getLength());
                        bos.close();
                        fos.close();
                    } else {
                        FileWriter fw = new FileWriter(currentFile);
                        editorPane.getEditorKit().write(fw, editorPane.getDocument(), 0, editorPane.getDocument().getLength());
                        fw.close();
                    }
                    setTitle(currentFile.getName() + " - TextEditor");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainPanel, "Cannot save the file!");
                }

            }
        }
    }

    private class ExitActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    private class CutActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(new Cut());
        }
    }

    private class Cut implements Runnable{
        @Override
        public void run() {
            Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(editorPane.getSelectedText());
            cp.setContents(stringSelection, null);
            editorPane.replaceSelection("");
        }
    }

    private class CopyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(new Copy());
        }
    }

    private class Copy implements Runnable{
        @Override
        public void run() {
            Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(editorPane.getSelectedText());
            cp.setContents(stringSelection, null);
        }
    }

    private class PasteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(new Paste());
        }
    }

    private class Paste implements Runnable{
        @Override
        public void run() {
            Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable t = cp.getContents(this);
            try {
                editorPane.replaceSelection((String) t.getTransferData(DataFlavor.stringFlavor));
            }catch(Exception exc){
                JOptionPane.showMessageDialog(mainPanel, "Pasting error!");
            }
        }
    }

    private class AboutActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(mainPanel, "TextEditor v.1.0\n" +
                    "Author: Łukasz Mielczarek\n" +
                    "Github: http://github.com/Ukasshu", "About", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private class BoldActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e){

        }
    }

    private class ItalicActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

    private class UnderlineActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }
}

