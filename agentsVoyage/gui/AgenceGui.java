package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import agents.AgenceAgent;
import jade.gui.GuiEvent;

/**
 * Agence Gui, communication with AgenceAgent throw GuiEvent
 * 
 * @author Emmanuel Adam - LAMIH
 */
@SuppressWarnings("serial")
public class AgenceGui extends JFrame {

	private static int nbAgenceGui = 0;
	private int noAgenceGui;

	/** Text area */
	private JTextArea jTextArea;

	private AgenceAgent myAgent;

	public AgenceGui(AgenceAgent a) {
		super(a.getName());
		noAgenceGui = ++nbAgenceGui;

		myAgent = a;

		jTextArea = new JTextArea();
		jTextArea.setBackground(new Color(255, 255, 240));
		jTextArea.setEditable(false);
		jTextArea.setColumns(40);
		jTextArea.setRows(5);
		JScrollPane jScrollPane = new JScrollPane(jTextArea);
		getContentPane().add(BorderLayout.CENTER, jScrollPane);

		// Make the agent terminate when the user closes
		// the GUI using the button on the upper right corner
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// SEND AN GUI EVENT TO THE AGENT !!!
				GuiEvent guiEv = new GuiEvent(this, AgenceAgent.EXIT);
				myAgent.postGuiEvent(guiEv);
				// END SEND AN GUI EVENT TO THE AGENT !!!
			}
		});

		setResizable(true);
	}

	public void display() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int width = this.getWidth();
		int xx = (noAgenceGui * width) % screenWidth;
		int yy = ((noAgenceGui * width) / screenWidth) * getHeight();
		setLocation(xx, yy);
		setTitle(myAgent.getLocalName());
		setVisible(true);
	}

	/** add a string to the text area */
	public void println(String chaine) {
		String texte = jTextArea.getText();
		texte = texte + chaine + "\n";
		jTextArea.setText(texte);
		jTextArea.setCaretPosition(texte.length());
	}


}
