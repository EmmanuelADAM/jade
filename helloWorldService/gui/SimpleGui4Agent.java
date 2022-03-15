package helloWorldService.gui;

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
public class SimpleGui4Agent extends JFrame implements ActionListener {
    /**
     * code associated to the Quit button
     */
    public static final int QUITCODE = -1;
    /**
     * code associated to the Quit button
     */
    public static final int SENDCODE = 10;
    /**
     * string associated to the Quit button
     */
    private static final String QUITCMD = "QUIT";
    /**
     * string associated to the Quit button
     */
    private static final String SENDCMD = "SEND";
    /**
     * nb of windows created
     */
    static int nb = 0;
    /**
     * Low Text area
     */
    public JTextField lowTextArea;
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
    public SimpleGui4Agent() {
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

    public SimpleGui4Agent(GuiAgent agent) {
        this();
        myAgent = agent;
        setTitle(myAgent.getLocalName());
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
        lowTextArea = new JTextField();
        jScrollPane = new JScrollPane(lowTextArea);
        getContentPane().add(BorderLayout.SOUTH, jScrollPane);

        JPanel jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(0, 2));
        // (just add columns to add button, or other thing...
        JButton button = new JButton("--- QUIT ---");
        button.addActionListener(this);
        button.setActionCommand(QUITCMD);
        jpanel.add(button);
        button = new JButton("--- SEND ---");
        button.addActionListener(this);
        button.setActionCommand(SENDCMD);
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
     * add a string to a text area  (main parameter is no more used)
     * @param chaine text to add
     * @param main if true text is added to the main text area, if false, text is set in the small text area
     */
    public void println(final String chaine, final boolean main) {
        if(main)println(chaine);
        else {
            lowTextArea.setText(chaine);
        }
    }

    /**
     * reaction to the button event and communication with the agent
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        final String source = evt.getActionCommand();
        if (source.equals(SimpleGui4Agent.QUITCMD)) {
            GuiEvent ev = new GuiEvent(this, SimpleGui4Agent.QUITCODE);
            myAgent.postGuiEvent(ev);
        }
        if (source.equals(SimpleGui4Agent.SENDCMD)) {
            GuiEvent event = new GuiEvent(this, SimpleGui4Agent.SENDCODE);
            myAgent.postGuiEvent(event);
        }
    }

}
