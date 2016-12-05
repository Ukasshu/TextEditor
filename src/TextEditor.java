import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.stream.IntStream;

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
    private JComboBox fontSizeComboBox;
    private JButton chooseColorButton;
    private JButton rightAlignmentButton;
    private JButton centerAlignmentButton;
    private JButton leftAlignmentButton;
    private JButton justifiedAlignmentButton;


    private static int windowsCounter = 0;
    private File currentFile = null;
    private String mode = null;

    Boolean isDocumentChanged;

    public TextEditor(String type) {
        setContentPane(mainPanel);
        setMinimumSize(new Dimension(800, 600));
        menuBar = new MenuBarBuilder().menuBar;
        setJMenuBar(menuBar);
        setTitle("Untitled - TextEditor");
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
        for(Integer i: IntStream.rangeClosed(1, 100).toArray()){
            fontSizeComboBox.addItem(i);
        }
        //System.out.println(Color.getColor());
        fontComboBox.setSelectedItem("Monospaced");
        fontSizeComboBox.setSelectedItem(12);
        editorPane.setEditorKit(editorPane.getEditorKitForContentType(type));
        editorPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        windowsCounter++;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createListeners();
        isDocumentChanged = false;
    }

    public static void main(String[] args) {
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

    private void createListeners() {
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
        boldButton.addActionListener(new StyledEditorKit.BoldAction());
        italicButton.addActionListener(new StyledEditorKit.ItalicAction());
        underlineButton.addActionListener(new StyledEditorKit.UnderlineAction());
        fontComboBox.addActionListener(new FontFamilyListener());
        fontSizeComboBox.addActionListener(new FontSizeListener());
        chooseColorButton.addActionListener(new FontColorListener());
        rightAlignmentButton.addActionListener(new RightAlignmentListener());
        leftAlignmentButton.addActionListener(new LeftAlignmentListener());
        centerAlignmentButton.addActionListener(new CenterAlignmentListener());
        justifiedAlignmentButton.addActionListener(new JustifiedAlignmentListener());
        editorPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isDocumentChanged = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isDocumentChanged = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isDocumentChanged = true;
            }

        });
    }

    private class MenuBarBuilder {
        private JMenuBar menuBar;

        private MenuBarBuilder() {
            JMenu menu;
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

    private class FontFamilyListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Action a = new StyledEditorKit.FontFamilyAction(fontComboBox.getSelectedItem().toString(), new Font(fontComboBox.getSelectedItem().toString(), Font.PLAIN, 20).getFamily());
            a.actionPerformed(e);
        }
    }

    private class FontSizeListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Action a = new StyledEditorKit.FontSizeAction(fontComboBox.getSelectedItem().toString(), (Integer)fontSizeComboBox.getSelectedItem());
            a.actionPerformed(e);
        }
    }

    private class FontColorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Color chosenColor = JColorChooser.showDialog(mainPanel, "Choose color", Color.black);
            Action a = new StyledEditorKit.ForegroundAction("xD", chosenColor);
            a.actionPerformed(e);
            chooseColorButton.setBackground(chosenColor);
        }
    }

    private class RightAlignmentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Action a = new StyledEditorKit.AlignmentAction("xD", StyleConstants.ALIGN_RIGHT);
            a.actionPerformed(e);
        }
    }

    private class CenterAlignmentListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Action a = new StyledEditorKit.AlignmentAction("xD", StyleConstants.ALIGN_CENTER);
            a.actionPerformed(e);
        }
    }

    private class LeftAlignmentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Action a = new StyledEditorKit.AlignmentAction("xD", StyleConstants.ALIGN_LEFT);
            a.actionPerformed(e);
        }
    }

    private class JustifiedAlignmentListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Action a = new StyledEditorKit.AlignmentAction("xD", StyleConstants.ALIGN_LEFT);
            a.actionPerformed(e);
        }
    }

    private class LockFontElements implements Runnable {
        @Override
        public void run() {
            boldButton.setEnabled(false);
            italicButton.setEnabled(false);
            underlineButton.setEnabled(false);
            fontComboBox.setEnabled(false);
            fontSizeComboBox.setEnabled(false);
            chooseColorButton.setEnabled(false);
            rightAlignmentButton.setEnabled(false);
            centerAlignmentButton.setEnabled(false);
            leftAlignmentButton.setEnabled(false);
            justifiedAlignmentButton.setEnabled(false);
            try {
                CaretListener cl = editorPane.getCaretListeners()[0];
                editorPane.removeCaretListener(cl);
            }
            catch (ArrayIndexOutOfBoundsException e){

            }
        }
    }

    private class UnlockFontElements implements Runnable {
        @Override
        public void run() {
            boldButton.setEnabled(true);
            italicButton.setEnabled(true);
            underlineButton.setEnabled(true);
            fontComboBox.setEnabled(true);
            fontSizeComboBox.setEnabled(true);
            chooseColorButton.setEnabled(true);
            rightAlignmentButton.setEnabled(true);
            centerAlignmentButton.setEnabled(true);
            leftAlignmentButton.setEnabled(true);
            justifiedAlignmentButton.setEnabled(true);
            editorPane.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent e) {
                    EventQueue.invokeLater(new UpdateFont());
                }
            });
        }
    }

    @Override
    public void dispose() {
        int ans = JOptionPane.NO_OPTION;
        if(isDocumentChanged){
            ans = JOptionPane.showConfirmDialog(mainPanel, "Do you want to save changes?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            if(ans==JOptionPane.YES_OPTION){
                new SaveActionListener().actionPerformed(new ActionEvent(saveFileButton, 1001, null));
                if(isDocumentChanged){
                    ans = JOptionPane.CANCEL_OPTION;
                }
            }
        }
        if(ans!=JOptionPane.CANCEL_OPTION) {
            windowsCounter--;
            super.dispose();
        }
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
                int ans = JOptionPane.NO_OPTION;
                if(isDocumentChanged){
                    ans = JOptionPane.showConfirmDialog(mainPanel, "Do you want to save changes?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
                    if(ans==JOptionPane.YES_OPTION){
                        new SaveActionListener().actionPerformed(new ActionEvent(saveFileButton, 1001, null));
                        if(isDocumentChanged){
                            ans = JOptionPane.CANCEL_OPTION;
                        }
                    }
                }
                if(ans!=JOptionPane.CANCEL_OPTION) {
                    EventQueue.invokeLater(new OpenFile());
                }
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
                    EventQueue.invokeLater(new UnlockFontElements());
                    mode = "RichTextMode";
                } else {
                    editorPane.setEditorKit(editorPane.getEditorKitForContentType("text/plain"));
                    EventQueue.invokeLater(new LockFontElements());
                    mode = "PlainTextMode";
                }
                try {
                    FileReader fr = new FileReader(currentFile);
                    editorPane.getEditorKit().read(fr, editorPane.getDocument(), 0);
                    fr.close();
                    isDocumentChanged = false;
                    editorPane.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            isDocumentChanged = true;
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            isDocumentChanged = true;
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            isDocumentChanged = true;
                        }
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainPanel, "Cannot open this file!");
                    e.printStackTrace();
                    //System.out.println(documentHashCode);
                    //System.out.println(editorPane);
                    //System.out.println(editorPane.getText());
                    //System.out.println(editorPane.getText().hashCode());

                }
                setTitle(currentFile.getName() +" - " + mode + " - TextEditor");
            }
        }
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
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
                isDocumentChanged = false;
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
                    isDocumentChanged = false;
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


    private class UpdateFont implements Runnable{
        @Override
        public void run() {
            AttributeSet at = ((StyledEditorKit)editorPane.getEditorKit()).getInputAttributes();
            if(StyleConstants.isBold(at)){
                boldButton.setSelected(true);
            }
            else{
                boldButton.setSelected(false);
            }
            if(StyleConstants.isItalic(at)){
                italicButton.setSelected(true);
            }
            else{
                italicButton.setSelected(false);
            }
            if(StyleConstants.isUnderline(at)){
                underlineButton.setSelected(true);
            }
            else{
                underlineButton.setSelected(false);
            }
            fontComboBox.setSelectedItem(StyleConstants.getFontFamily(at));
            fontSizeComboBox.setSelectedItem(StyleConstants.getFontSize(at));
            chooseColorButton.setBackground(StyleConstants.getForeground(at));
            switch(StyleConstants.getAlignment(at)){
                case StyleConstants.ALIGN_LEFT:
                    leftAlignmentButton.setSelected(true);
                    rightAlignmentButton.setSelected(false);
                    centerAlignmentButton.setSelected(false);
                    justifiedAlignmentButton.setSelected(false);
                    break;
                case StyleConstants.ALIGN_RIGHT:
                    leftAlignmentButton.setSelected(false);
                    rightAlignmentButton.setSelected(true);
                    centerAlignmentButton.setSelected(false);
                    justifiedAlignmentButton.setSelected(false);
                    break;
                case StyleConstants.ALIGN_CENTER:
                    leftAlignmentButton.setSelected(false);
                    rightAlignmentButton.setSelected(false);
                    centerAlignmentButton.setSelected(true);
                    justifiedAlignmentButton.setSelected(false);
                    break;
                case StyleConstants.ALIGN_JUSTIFIED:
                    leftAlignmentButton.setSelected(false);
                    rightAlignmentButton.setSelected(false);
                    centerAlignmentButton.setSelected(false);
                    justifiedAlignmentButton.setSelected(true);
                    break;
                default:
                    break;
            }
        }
    }
}

