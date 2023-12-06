package handsOn.circularEconomy.gui;

import handsOn.circularEconomy.agents.UserAgent;
import handsOn.circularEconomy.data.Product;
import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;


/**
 * a simple window with a text area to display information
 * and a button
 * the window find automatically its place in the screen
 *
 * @author Emmanuel Adam
 */
public class UserAgentWindow extends JFrame implements ActionListener {
    public final static int OK_EVENT = 1;
    public final static int QUIT_EVENT = -1;
    static int nb = 0;
    int no = 0;
    /**
     * Text area
     */
    JTextArea jTextArea;
    /**
     * Text area
     */
    JButton jbutton;
    /**
     * monAgent linked to this frame
     */
    UserAgent myAgent;
    private boolean buttonActivated;
    private JComboBox<Product> comboProducts;


    /**
     * a simple window with a text area to display informations
     * and a button
     * the window find automaticcaly its place in the screen
     *
     * @author emmanueladam
     */
    public UserAgentWindow() {
        no = nb++;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int widthJFrame = Math.min((int) Math.floor(screen.getWidth() / 3) - 10, 450);
        int heightJFrame = 200;
        setBounds(30 + no * 20, 30 + no * 20, widthJFrame, heightJFrame);
        buildGui();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                GuiEvent ev = new GuiEvent(this, QUIT_EVENT);
                myAgent.postGuiEvent(ev);
            }
        });
        setVisible(true);
    }

    public UserAgentWindow(UserAgent _a) {
        this();
        myAgent = _a;
    }


    public UserAgentWindow(String _titre) {
        this();
        setTitle(_titre);
    }

    public UserAgentWindow(String _titre, UserAgent _a) {
        this(_titre);
        myAgent = _a;
    }


    /**
     * build the gui :
     * - a text area in the center of the window, with scroll bars
     * - a button in the bottom
     * - a combobox in the bottom
     */
    public void buildGui() {
        getContentPane().setLayout(new BorderLayout());
        jTextArea = new JTextArea();
        jTextArea.setRows(5);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);
        JPanel bottomPanel = new JPanel(new GridLayout(0, 1));
        jbutton = new JButton("go");
        comboProducts = new JComboBox<>();
        bottomPanel.add(jbutton);
        bottomPanel.add(comboProducts);
        getContentPane().add(BorderLayout.SOUTH, bottomPanel);
        jbutton.setEnabled(false);
    }


    /**
     * add a string to the text area
     */
    public void println(String chaine) {
        String texte = jTextArea.getText();
        texte = texte + chaine + "\n";
        jTextArea.setText(texte);
        jTextArea.setCaretPosition(texte.length());
    }

    /**
     * add a formatted string to the text area
     */
    public void printf(String format, Object[] tabO) {
        String texte = jTextArea.getText();
        texte = texte + format.formatted(tabO) + "\n";
        jTextArea.setText(texte);
        jTextArea.setCaretPosition(texte.length());
    }

    /**
     * SEND A MESSAGE TO THE AGENT
     */
    public void actionPerformed(ActionEvent evt) {
        GuiEvent ev = new GuiEvent(this, OK_EVENT);
        myAgent.postGuiEvent(ev);
    }

    public void setBackgroundTextColor(Color c) {
        jTextArea.setBackground(c);
    }

    public boolean isButtonActivated() {
        return buttonActivated;
    }

    public void setButtonActivated(boolean buttonActivated) {
        if (buttonActivated) {
            jbutton.setEnabled(true);
            jbutton.setText("-- go --");
            jbutton.addActionListener(this);
        } else {
            jbutton.setEnabled(false);
            jbutton.setText("--");
            jbutton.addActionListener(null);
        }
        this.buttonActivated = buttonActivated;
    }

    public Product getSelectedProduct() {
        return (Product)comboProducts.getSelectedItem();
    }

    public void addProductToCombo(Product p) {
        comboProducts.addItem(p);
    }

    public void addProductsToCombo(List<Product> list) {
        for (Product p : list) comboProducts.addItem(p);
    }

}
