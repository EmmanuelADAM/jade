package ollama.gui;

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
public class GuiOllamaAgent extends JFrame implements ActionListener {
    /**
     * code associated to the Quit button
     */
    public static final int QUITCODE = -1;
    /**
     * code associated to the "send to lobby" button
     */
    public static final int SENDQUERY = 1;
    /**
     * string associated to the Quit button
     */
    private static final String QUITCMD = "-1";
    /**
     * string associated to the send of an offer to the seller (BUTTON ACTION)
     */
    private static final String SENDQUERYCMD = "1";
    /**
     * code associated to the choice of a model in the combo box (the model name is the 1st parameter of the event)
     */
    public static final int CHANGEMODEL = 2;
    /**
     * string associated to the choice of a model in the combo box
     */
    private static final String CHANGEMODELCMD = "2";
    /**
     * code associated to the choice of a town (the town name is the 1st parameter of the event)
     */
    public static final int CHANGECITY = 3;
    /**
     * string associated to the choice of a town (enter in the town field, or METEO button)
     */
    private static final String CHANGECITYCMD = "3";

    /**
     * nb of windows created
     */
    static int nb = 0;
    /**
     * Low Text area
     */
    public JTextField lowTextArea;
    /**
     * Main area, displays html
     */
    JEditorPane mainPane;
    /**
     * style of the main area
     */
    private static final String STYLE = """
            <style>
            body { font-family: SansSerif; font-size: 11pt; margin: 6px; }
            .log { font-family: Monospaced; font-size: 10pt; color: #555555; }
            pre  { font-family: Monospaced; background-color: #f0f0f0; padding: 4px; }
            code { font-family: Monospaced; color: #a0306a; }
            blockquote { color: #555555; margin-left: 10px; }
            th { background-color: #e8e8e8; }
            </style>""";
    /**
     * html of the texts already displayed
     */
    private final StringBuilder htmlDone = new StringBuilder();
    /**
     * markdown text being received (answer of the LLM)
     */
    private final StringBuilder markdown = new StringBuilder();
    /**
     * true if a refresh of the main area is already planned
     */
    private boolean refreshPlanned = false;
    /**
     * list of the available LLM models
     */
    JComboBox<String> modelsBox;
    /**
     * town of the user
     */
    JTextField cityField;
    /**
     * animated bar displayed while waiting for the LLM
     */
    JProgressBar waitingBar;
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
    public GuiOllamaAgent() {
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

    public GuiOllamaAgent(GuiAgent agent) {
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
        mainPane = new JEditorPane("text/html", "");
        mainPane.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(mainPane);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);
        lowTextArea = new JTextField();
        jScrollPane = new JScrollPane(lowTextArea);
        // bottom : an animated bar (hidden by default) above the text field
        waitingBar = new JProgressBar();
        waitingBar.setIndeterminate(true);
        waitingBar.setStringPainted(true);
        waitingBar.setVisible(false);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(BorderLayout.NORTH, waitingBar);
        southPanel.add(BorderLayout.CENTER, jScrollPane);
        getContentPane().add(BorderLayout.SOUTH, southPanel);

        JPanel jpanel = new JPanel();
        jpanel.setLayout(new GridLayout(0, 3));
        // (just add columns to add button, or other thing...
        JButton button = new JButton("--- QUIT ---");
        button.addActionListener(this);
        button.setActionCommand(QUITCMD);
        jpanel.add(button);
        button = new JButton("SEND QUERY");
        button.addActionListener(this);
        button.setActionCommand(SENDQUERYCMD);
        jpanel.add(button);
        modelsBox = new JComboBox<>();
        modelsBox.setActionCommand(CHANGEMODELCMD);
        modelsBox.addActionListener(this);
        jpanel.add(modelsBox);

        // second line : the town of the user (validated by enter or by the METEO button)
        JPanel cityPanel = new JPanel(new BorderLayout());
        cityPanel.add(BorderLayout.WEST, new JLabel(" Ville : "));
        cityField = new JTextField();
        cityField.setActionCommand(CHANGECITYCMD);
        cityField.addActionListener(this);
        cityPanel.add(BorderLayout.CENTER, cityField);
        button = new JButton("MÉTÉO");
        button.addActionListener(this);
        button.setActionCommand(CHANGECITYCMD);
        cityPanel.add(BorderLayout.EAST, button);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(BorderLayout.NORTH, jpanel);
        northPanel.add(BorderLayout.SOUTH, cityPanel);
        getContentPane().add(BorderLayout.NORTH, northPanel);
    }

    /**
     * fill the combo box with the models, and select one of them (without sending an event to the agent)
     * @param models names of the models
     * @param selected name of the selected model
     */
    public void setModels(String[] models, String selected) {
        modelsBox.removeActionListener(this);
        modelsBox.removeAllItems();
        for (String model : models) modelsBox.addItem(model);
        modelsBox.setSelectedItem(selected);
        modelsBox.addActionListener(this);
    }

    /**
     * display the town of the user (without sending an event to the agent)
     */
    public void setCity(String city) {
        SwingUtilities.invokeLater(() -> cityField.setText(city));
    }

    /**
     * add a string (raw text, not markdown) to the main area
     */
    public synchronized void println(final String chaine) {
        closeMarkdown();
        htmlDone.append("<div class=\"log\">")
                .append(MarkdownToHtml.escape(chaine).replace("\n", "<br>"))
                .append("</div>\n");
        planRefresh();
    }

    /**
     * the markdown text being received is finished : it is converted once for all in html
     */
    private void closeMarkdown() {
        if (markdown.length() > 0) {
            htmlDone.append(MarkdownToHtml.convert(markdown.toString()));
            markdown.setLength(0);
        }
    }

    /**
     * plan a refresh of the main area in the swing thread
     * (if many texts arrive quickly, only one refresh is done)
     */
    private void planRefresh() {
        if (refreshPlanned) return;
        refreshPlanned = true;
        SwingUtilities.invokeLater(() -> {
            String html;
            synchronized (this) {
                refreshPlanned = false;
                html = "<html><head>" + STYLE + "</head><body>" + htmlDone
                        + MarkdownToHtml.convert(markdown.toString()) + "</body></html>";
            }
            mainPane.setText(html);
            mainPane.setCaretPosition(mainPane.getDocument().getLength());
        });
    }

    /**
     * display the animated bar with a message (ex. while the model is loading)
     * @param message text displayed on the bar
     */
    public void startWaiting(final String message) {
        SwingUtilities.invokeLater(() -> {
            waitingBar.setString(message);
            waitingBar.setVisible(true);
            revalidate();
        });
    }

    /**
     * hide the animated bar
     */
    public void stopWaiting() {
        SwingUtilities.invokeLater(() -> {
            waitingBar.setVisible(false);
            revalidate();
        });
    }

    /**
     * add a markdown text to the main area, without going to the next line
     * (used to display the answer of a LLM piece by piece, the answer is displayed in html)
     */
    public synchronized void print(final String chaine) {
        markdown.append(chaine);
        planRefresh();
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
        if (source.equals(QUITCMD) || source.equals(SENDQUERYCMD) ) {
            GuiEvent ev = new GuiEvent(this, Integer.parseInt(source));
            myAgent.postGuiEvent(ev);
        }
        else if (source.equals(CHANGEMODELCMD) && modelsBox.getSelectedItem() != null) {
            GuiEvent ev = new GuiEvent(this, CHANGEMODEL);
            ev.addParameter(modelsBox.getSelectedItem());
            myAgent.postGuiEvent(ev);
        }
        else if (source.equals(CHANGECITYCMD) && !cityField.getText().isBlank()) {
            GuiEvent ev = new GuiEvent(this, CHANGECITY);
            ev.addParameter(cityField.getText().trim());
            myAgent.postGuiEvent(ev);
        }
    }

}
