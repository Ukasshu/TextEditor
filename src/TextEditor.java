import com.sun.xml.internal.bind.v2.model.annotation.RuntimeAnnotationReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
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


    private static int windowsCounter = 0;
    private File currentFile = null;

    public TextEditor(String type){
        setContentPane(mainPanel);
        menuBar = new MenuBarBuilder().menuBar;
        createMenusListeners();
        setJMenuBar(menuBar);
        setTitle("Untitled - TextEditor");
        windowsCounter++;
        editorPane.setEditorKit(editorPane.getEditorKitForContentType(type));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public static void main(String[] args){
        System.out.println(args.length);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch (Exception e){
            e.printStackTrace();
        }
        JFrame frame;
        if(args.length == 0) {
            frame = new TextEditor("type/plain");
        }
        else{
            frame = new TextEditor(args[0]);
        }
        frame.pack();
        frame.setVisible(true);
        if(windowsCounter == 0){
            System.exit(0);
        }
    }

    private void createMenusListeners(){
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
        menu.getItem(4).addActionListener(new FormatActionListener());
        menu = menuBar.getMenu(2);
        menu.getItem(0).addActionListener(new AboutActionListener());
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
            menu.addSeparator();
            menu.add(new JMenuItem("Format..."));
            menuBar.add(menu);
            menu = new JMenu("Help");
            menu.setMnemonic(KeyEvent.VK_H);
            menu.add(new JMenuItem("About..."));
            menuBar.add(menu);
        }
    }

    @Override
    public void dispose(){
        windowsCounter--;
        super.dispose();
    }

    private class NewActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            String[] args = new String[1];
            args[0] = "text/plain";
            main(args);
        }
    }

    private class NewRTFActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[1];
            args[0] = "text/rtf";
            main(args);
        }
    }

    private class OpenActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            try{
                EventQueue.invokeLater(new OpenFile());
            }
            catch(Exception ex){
                    JOptionPane.showMessageDialog(mainPanel, "Cannot open the file");
            }
        }
    }

    private class OpenFile implements Runnable {
        public void run() {
            JFileChooser fc = new JFileChooser("~");
            if(fc.showDialog(mainPanel, "Open file") == JFileChooser.APPROVE_OPTION){
                currentFile = fc.getSelectedFile();
                if(new FileNameExtensionFilter("RTF File", "rtf").accept(currentFile)){
                    editorPane.setEditorKit(editorPane.getEditorKitForContentType("text/rtf"));
                }
                else{
                    editorPane.setEditorKit(editorPane.getEditorKitForContentType("text/plain"));
                }
                try {
                    FileReader fr = new FileReader(currentFile);
                    editorPane.getEditorKit().read(fr, editorPane.getDocument(), 0);
                    fr.close();
                }catch(Exception e){
                    JOptionPane.showMessageDialog(mainPanel, "Cannot open this file!");
                }
                setTitle(currentFile.getName() + " - TextEditor");
            }
            System.out.println(currentFile);
        }
    }

    private class SaveActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            //JOptionPane.showMessageDialog(mainPanel, "SAVE");
            if(currentFile==null){
                EventQueue.invokeLater(new SaveAs());
            }
            else{
                EventQueue.invokeLater(new SaveFile());
            }
        }
    }

    private class SaveFile implements Runnable{
        public void run(){
            try{
                if(editorPane.getContentType().equals("text/plain")) {
                    FileWriter fw = new FileWriter(currentFile);
                    editorPane.getEditorKit().write(fw, editorPane.getDocument(), 0, editorPane.getDocument().getLength());
                    fw.close();
                }
                else{
                    FileOutputStream fos = new FileOutputStream(currentFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    editorPane.getEditorKit().write(bos, editorPane.getDocument(), 0, editorPane.getDocument().getLength());
                    bos.close();
                    fos.close();
                }

            }catch (Exception e){
                JOptionPane.showMessageDialog(mainPanel, "Cannot save file!");
                e.printStackTrace();
            }
        }
    }

    private class SaveAsActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JFileChooser fc = new JFileChooser("~");

        }
    }

    private class SaveAs implements Runnable{
        @Override
        public void run() {

        }
    }

    private class ExitActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(mainPanel, "EXIT");
        }
    }

    private class CutActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(mainPanel, "CUT");
        }
    }

    private class CopyActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(mainPanel, "COPY");
        }
    }

    private class PasteActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(mainPanel, "PASTE");
        }
    }

    private class FormatActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(mainPanel, "FORMAT");
        }
    }

    private class AboutActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(mainPanel, "ABOUT");
        }
    }
}

