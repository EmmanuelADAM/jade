package radio.gui;

import radio.agents.AgentWindowed;
import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/** a simple window with a text area to display informations*/
@SuppressWarnings("serial")
public class SimpleWindow4Agent extends JFrame  implements ActionListener{
	static int nb=0;
	int no=0;
	/** Text area */
	JTextArea jTextArea;
	/** Text area */
	JButton jbutton;
	/**monAgent linked to this frame */
	AgentWindowed myAgent;
	private int widthJFrame=400;
	private int heightJFrame=150;
	private boolean buttonActivated;


	public SimpleWindow4Agent() {
		no=nb++;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int xx = 10 + (int)((no*widthJFrame)%screen.getWidth());
		int yy = 10 + (heightJFrame * (int)(((no+1) *widthJFrame)/screen.getWidth()) ) ;
		setBounds(xx, yy, widthJFrame, heightJFrame);
		buildGui();
		setVisible(true);
	}

	public SimpleWindow4Agent(AgentWindowed _a) {
		this();
		myAgent = _a;
	}
	

	public SimpleWindow4Agent(String _titre) {
		this();
		setTitle(_titre);
	}

	public SimpleWindow4Agent(String _titre, AgentWindowed _a) {
		this(_titre);
		myAgent = _a;
	}


	/** build the gui : a text area in the center of the window, with scroll bars*/
	private void buildGui()
	{
		getContentPane().setLayout(new BorderLayout());
		jTextArea = new JTextArea();
		jTextArea.setRows(5);
		JScrollPane jScrollPane  = new JScrollPane(jTextArea);        
		getContentPane().add(BorderLayout.CENTER, jScrollPane);
		jbutton = new JButton("--");
		getContentPane().add(BorderLayout.SOUTH, jbutton);
		jbutton.setEnabled(false);
	}


	/** add a string to the text area */
	public void println(String chaine) {
		String texte = jTextArea.getText();
		texte = texte +  chaine + "\n";
		jTextArea.setText(texte);
		jTextArea.setCaretPosition(texte.length());
	}

	 /**SEND A MESSAGE TO THE AGENT*/
	  public void actionPerformed(ActionEvent evt) {
	      GuiEvent ev = new GuiEvent(this,-1);
	      myAgent.postGuiEvent(ev);
	    }

	public boolean isButtonActivated() {
		return buttonActivated;
	}

	public void setButtonActivated(boolean buttonActivated) {
		if(buttonActivated)
		{
			jbutton.setEnabled(true);
			jbutton.setText("-- go --");
			jbutton.addActionListener(this);
		}
		else
			{
				jbutton.setEnabled(false);
				jbutton.setText("--");
				jbutton.addActionListener(null);
			}			
		this.buttonActivated = buttonActivated;
	}

}
