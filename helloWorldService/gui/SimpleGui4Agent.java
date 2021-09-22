package helloWorldService.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

/**
 * a simple window for a Jade GuiAgent with two texts areas to display
 * informations
 * 
 * @author emmanuel adam
 * @version 1
 */
public class SimpleGui4Agent extends JFrame implements ActionListener {
	/** Main Text area */
	JTextArea mainTextArea;

	/** Low Text area */
	public JTextField lowTextArea;

	/** monAgent linked to this frame */
	GuiAgent myAgent;

	/** nb of windows created */
	static int nb = 0;

	/** no of the window */
	int no;

	/** string associated to the Quit button */
	private static final String QUITCMD = "QUIT";
	/** code associated to the Quit button */
	public static final int QUITCODE = -1;

	/** string associated to the Quit button */
	private static final String SENDCMD = "SEND";
	/** code associated to the Quit button */
	public static final int SENDCODE = 10;

	/** creates a window and displays it in a free space of the screen */
	public SimpleGui4Agent() {
		final int preferedWidth = 500;
		final int preferedHeight = 300;
		no = nb++;

		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		int dX = preferedWidth;
		int x = (no * dX) % screenWidth;
		int y = (((no * dX) / screenWidth) * preferedHeight) % screenHeight;

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
	 * build the agencesVoyages.gui : a text area in the center of the window, with scroll bars
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

	/** add a string to the low text area */
	public void println(final String chaine) {
		String texte = lowTextArea.getText();
		texte = texte + chaine + "\n";
		lowTextArea.setText(texte);
		lowTextArea.setCaretPosition(texte.length());
	}

	/** add a string to the low text area  (main parameter is no more used)*/
	public void println(final String chaine, final boolean main) {
			String texte = mainTextArea.getText();
			texte = texte + chaine + "\n";
			mainTextArea.setText(texte);
			mainTextArea.setCaretPosition(texte.length());
	}

	/** reaction to the button event and communication with the agent */
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
