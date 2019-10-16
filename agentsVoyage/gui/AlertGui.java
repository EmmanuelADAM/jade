package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import agents.AlertAgent;
import jade.gui.GuiEvent;

/**
 * Journey alert Gui, communication with AlertAgent throw GuiEvent
 * 
 * @author modif. Emmanuel Adam - LAMIH
 */
@SuppressWarnings("serial")
public class AlertGui extends JFrame {
	/** Text area */
	private JTextArea jTextArea;

	private AlertAgent myAgent;
	private JComboBox<String> jListFrom;
	private JComboBox<String> jListTo;

	private String departure;
	private String arrival;

	public AlertGui(AlertAgent a) {
		this.setBounds(10, 10, 600, 200);

		myAgent = a;
		if (a != null)
			setTitle(myAgent.getLocalName());

		jTextArea = new JTextArea();
		jTextArea.setBackground(new Color(255, 255, 240));
		jTextArea.setEditable(false);
		jTextArea.setColumns(10);
		jTextArea.setRows(5);
		JScrollPane jScrollPane = new JScrollPane(jTextArea);
		getContentPane().add(BorderLayout.CENTER, jScrollPane);

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(0, 4, 0, 0));
		p.add(new JLabel("From:"));

		String[]villes={"a","b","c","d","e","f"};

		jListFrom = new JComboBox<>(villes);
		jListFrom.setSelectedIndex(0);
		p.add(jListFrom);

		p.add(new JLabel("To:"));
		jListTo = new JComboBox<>(villes);
		jListTo.setSelectedIndex(0);
		p.add(jListTo);

		JButton addButton = new JButton("Alert !");
		addButton.addActionListener(event -> {
			try {
				departure = (String) jListFrom.getSelectedItem();
				arrival = (String) jListTo.getSelectedItem();
				// SEND A GUI EVENT TO THE AGENT !!!
				GuiEvent guiEv = new GuiEvent(this, AlertAgent.ALERT);
				guiEv.addParameter(departure);
				guiEv.addParameter(arrival);
				myAgent.postGuiEvent(guiEv);
				// END SEND A GUI EVENT TO THE AGENT !!!
			} catch (Exception e) {
				JOptionPane.showMessageDialog(AlertGui.this, "Invalid values. " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);


		// Make the agent terminate when the user closes
		// the GUI using the button on the upper right corner
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// SEND AN GUI EVENT TO THE AGENT !!!
				GuiEvent guiEv = new GuiEvent(this, AlertAgent.EXIT);
				myAgent.postGuiEvent(guiEv);
				// END SEND AN GUI EVENT TO THE AGENT !!!
			}
		});

		setResizable(true);
	}


	/** add a string to the text area */
	public void println(String chaine) {
		String texte = jTextArea.getText();
		texte = texte + chaine + "\n";
		jTextArea.setText(texte);
		jTextArea.setCaretPosition(texte.length());
	}

	public void setColor(Color color) {
		jTextArea.setBackground(color);
	}


	public static void main(String[] args) {
		AlertGui test = new AlertGui(null);
		test.setVisible(true);
	}

}
