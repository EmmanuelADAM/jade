package td.negociationWindowSolution.gui;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * a simple window for a Jade GuiAgent with two texts areas to display
 * informations
 *
 * @author emmanuel adam
 * @version 1
 */
public class GuiSellerAgent extends JFrame implements ActionListener {
    /**
     * code associated to the Quit button
     */
    public static final int QUITCODE = -1;
    /**
     * code associated to the "send to lobby" button
     */
    public static final int SENDOFFER = 1;
    /**
     * string associated to the Quit button
     */
    private static final String QUITCMD = "-1";
    /**
     * string associated to the send of an offer to the seller (BUTTON ACTION)
     */
    private static final String SENDOFFERCMD = "1";

    /**
     * nb of windows created
     */
    static int nb = 0;
    /**
     * Low Text area
     */
    public JTextField jtfInitialPrice;
    public JTextField jtfMinimalPrice;
    public JTextField jtfNbRounds;
    /**
     * Main Text area
     */
    public JTextArea mainTextArea;
    /**
     * monAgent linked to this frame
     */
    GuiAgent myAgent;
    /**
     * no of the window
     */
    int no;

    /**
     * creates a window and displays it in a free space of the screen
     */
    public GuiSellerAgent() {
        final int preferedWidth = 500;
        final int preferedHeight = 300;
        no = nb++;

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int x = (no * preferedWidth) % screenWidth;
        int y = (((no * preferedWidth) / screenWidth) * preferedHeight) % screenHeight;

        setBounds(x, y, preferedWidth, preferedHeight);
        buildGui();
        setVisible(true);
    }

    public GuiSellerAgent(GuiAgent agent) {
        this();
        myAgent = agent;
        setTitle(myAgent.getLocalName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    /**
     * build the gui : a text area in the center of the window, with scroll bars
     */
    private void buildGui() {
        getContentPane().setLayout(new BorderLayout());
        mainTextArea = new JTextArea();
        mainTextArea.setRows(5);
        JScrollPane jScrollPane = new JScrollPane(mainTextArea);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);
        GridLayout gridLayout = new GridLayout(0, 2);
        JPanel jpanel = new JPanel();
        jpanel.setLayout(gridLayout);
        // (just add columns to add button, or other thing...
        jtfInitialPrice  = new JTextField("00000");
        jtfMinimalPrice   = new JTextField("00000");
        jtfNbRounds  = new JTextField("00000");;
        jpanel.add(new JLabel("Initial price: "));
        jpanel.add(jtfInitialPrice);
        jpanel.add(new JLabel("Minimal price: "));
        jpanel.add(jtfMinimalPrice);
        jpanel.add(new JLabel("Nb rounds max: "));
        jpanel.add(jtfNbRounds);

        getContentPane().add(BorderLayout.SOUTH, jpanel);

        jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(0, 3));
        // (just add columns to add button, or other thing...
        JButton button = new JButton("--- QUIT ---");
        button.addActionListener(this);
        button.setActionCommand(QUITCMD);
        jpanel.add(button);
        button = new JButton("SEND OFFER");
        button.addActionListener(this);
        button.setActionCommand(SENDOFFERCMD);
        jpanel.add(button);

        getContentPane().add(BorderLayout.NORTH, jpanel);
    }

    /**
     * add a string to the main text area
     */
    public void println(final String chaine) {
        String texte = mainTextArea.getText();
        texte = texte + chaine + "\n";
        mainTextArea.setText(texte);
        mainTextArea.setCaretPosition(texte.length());
    }


    /**
     * reaction to the button event and communication with the agent
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        final String source = evt.getActionCommand();
        if (source.equals(QUITCMD) || source.equals(SENDOFFERCMD) ) {
            GuiEvent ev = new GuiEvent(this, Integer.parseInt(source));
            myAgent.postGuiEvent(ev);
        }
    }

}
