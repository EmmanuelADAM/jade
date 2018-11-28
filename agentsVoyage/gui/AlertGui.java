package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
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

import agents.AlertAgent;
import jade.gui.GuiEvent;

/**
 * Journey alert Gui, communication with AlertAgent throw GuiEvent
 * 
 * @author modif. Emmanuel Adam - LAMIH
 */
@SuppressWarnings("serial")
public class AlertGui extends JFrame {

	private static int nbAlertGui = 0;
	private int noAlertGui;

	/** Text area */
	JTextArea jTextArea;

	/** window color */
	Color color;

	private AlertAgent myAgent;
	private JLabel lblPrice;
	private JComboBox<String> jListFrom;
	private JComboBox<String> jListTo;
	private JComboBox<String> jListCriteria;
	private JSlider sliderTime;

	private String departure;
	private String arrival;
	private int time;
	private int nbElements;

	public AlertGui(AlertAgent a) {
		this.setBounds(10, 10, 600, 200);
		noAlertGui = ++nbAlertGui;

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
				time = sliderTime.getValue();
				nbElements = 1;
				// SEND AN GUI EVENT TO THE AGENT !!!
				GuiEvent guiEv = new GuiEvent(this, AlertAgent.ALERT);
				guiEv.addParameter(departure);
				guiEv.addParameter(arrival);
				guiEv.addParameter(time);
				guiEv.addParameter(jListCriteria.getSelectedItem());
				myAgent.postGuiEvent(guiEv);
				// END SEND AN GUI EVENT TO THE AGENT !!!
			} catch (Exception e) {
				JOptionPane.showMessageDialog(AlertGui.this, "Invalid values. " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		//TODO: placer les vraies villes existantes dans les fichiers csv
		jListFrom = new JComboBox<>(new String[] { "-", "dep", "pt1", "pt2", "pt3", "arr" });
		jListFrom.setSelectedIndex(0);
		p.add(jListFrom);

      //TODO: placer les vraies villes existantes dans les fichiers csv
		jListTo = new JComboBox<>(new String[] { "-", "dep", "pt1", "pt2", "pt3", "arr" });
		jListTo.setSelectedIndex(0);
		p.add(jListTo);

		sliderTime = new JSlider();
		sliderTime.setPreferredSize(new Dimension(100, 10));
		sliderTime.setMinimum(800);
		sliderTime.setMaximum(2200);
		sliderTime.setMajorTickSpacing(100);
		sliderTime.setMinorTickSpacing(25);
		sliderTime.setSnapToTicks(true);
		sliderTime.setPaintTicks(true);
		sliderTime.addChangeListener(event -> {
			int hh = sliderTime.getValue() / 100;
			int mm = (int) (sliderTime.getValue() % 100 / 100d * 60d);
			String smm = (mm < 10) ? ("0" + mm) : String.valueOf(mm);
			lblPrice.setText("Departure: " + hh + ":" + smm);
			lblPrice.repaint();
		});
		p.add(sliderTime);

		jListCriteria = new JComboBox<>(new String[] { "-", "cost", "co2", "confort", "duration" });
		jListCriteria.setSelectedIndex(0);
		p.add(jListCriteria);

		p.add(addButton);

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

	public void display() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int width = this.getWidth();
		int xx = (noAlertGui * width) % screenWidth;
		int yy = ((noAlertGui * width) / screenWidth) * getHeight();
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

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		jTextArea.setBackground(color);
	}

	/**
	 * @return the bookName
	 */
	public String getBookName() {
		return arrival;
	}

	/**
	 * @return the price
	 */
	public int getPrice() {
		return time;
	}

	/**
	 * @return the nbElements
	 */
	public int getNbElements() {
		return nbElements;
	}

	public static void main(String[] args) {
		AlertGui test = new AlertGui(null);
		test.setVisible(true);
	}

}
