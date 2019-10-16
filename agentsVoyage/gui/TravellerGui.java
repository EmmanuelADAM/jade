package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JSlider;
import javax.swing.JTextArea;

import agents.TravellerAgent;
import jade.gui.GuiEvent;

/**
 * Journey resarch Gui, communication with TravellerAgent throw GuiEvent
 * 
 * @author modif. Emmanuel Adam - LAMIH
 */
@SuppressWarnings("serial")
public class TravellerGui extends JFrame {

	/** Text area */
	private JTextArea jTextArea;


	private TravellerAgent myAgent;
	private JLabel lblPrice;
	private JComboBox<String> jListFrom;
	private JComboBox<String> jListTo;
	private JComboBox<String> jListCriteria;
	private JSlider sliderTimeDeparture;

	private String departure;
	private String arrival;
	private int time;

	public TravellerGui(TravellerAgent a) {
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
		p.add(new JLabel("To:"));

		lblPrice = new JLabel("Departure: 8:00");
		p.add(lblPrice);

		p.add(new JLabel("Criteria"));

		getContentPane().add(p, BorderLayout.SOUTH);

		JButton addButton = new JButton("Buy");
		addButton.addActionListener(event -> {
			try {
				departure = (String) jListFrom.getSelectedItem();
				arrival = (String) jListTo.getSelectedItem();
				time = sliderTimeDeparture.getValue();
				// SEND AN GUI EVENT TO THE AGENT !!!
				GuiEvent guiEv = new GuiEvent(this, TravellerAgent.BUY_TRAVEL);
				guiEv.addParameter(departure);
				guiEv.addParameter(arrival);
				guiEv.addParameter(time);
				guiEv.addParameter(jListCriteria.getSelectedItem());
				myAgent.postGuiEvent(guiEv);
				// END SEND AN GUI EVENT TO THE AGENT !!!
			} catch (Exception e) {
				JOptionPane.showMessageDialog(TravellerGui.this, "Invalid values. " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		jListFrom = new JComboBox<>(new String[] { "-", "a", "b", "c", "d", "e", "f" });
		jListFrom.setSelectedIndex(0);
		p.add(jListFrom);

		jListTo = new JComboBox<>(new String[] { "-", "a", "b", "c", "d", "e", "f" });
		jListTo.setSelectedIndex(0);
		p.add(jListTo);

		sliderTimeDeparture = new JSlider();
		sliderTimeDeparture.setPreferredSize(new Dimension(100, 10));
		sliderTimeDeparture.setMinimum(800);
		sliderTimeDeparture.setMaximum(2200);
		sliderTimeDeparture.setMajorTickSpacing(100);
		sliderTimeDeparture.setMinorTickSpacing(25);
		sliderTimeDeparture.setSnapToTicks(true);
		sliderTimeDeparture.setPaintTicks(true);
		sliderTimeDeparture.addChangeListener(event -> {
			int hh = sliderTimeDeparture.getValue() / 100;
			int mm = (int) (sliderTimeDeparture.getValue() % 100 / 100d * 60d);
			String smm = (mm < 10) ? ("0" + mm) : String.valueOf(mm);
			lblPrice.setText("Departure: " + hh + ":" + smm);
			lblPrice.repaint();
		});
		p.add(sliderTimeDeparture);

		jListCriteria = new JComboBox<>(new String[] { "-", "cost", "co2", "confort", "duration", "duration-cost" });
		jListCriteria.setSelectedIndex(0);
		p.add(jListCriteria);

		p.add(addButton);
		p.add(new JLabel());
		p.add(new JLabel("Arrival"));

		// Make the agent terminate when the user closes
		// the GUI using the button on the upper right corner
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// SEND AN GUI EVENT TO THE AGENT !!!
				GuiEvent guiEv = new GuiEvent(this, TravellerAgent.EXIT);
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
		TravellerGui test = new TravellerGui(null);
		test.setVisible(true);
	}

}
