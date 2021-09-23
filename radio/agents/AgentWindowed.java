package radio.agents;

import radio.gui.SimpleWindow4Agent;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;

/**
 * classe générique d'agents permettant une interaction avec une fenêtre
 * 
 * @author revised by Emmanuel ADAM
 */
@SuppressWarnings("serial")
public  class AgentWindowed extends GuiAgent{

	/**la fenetre liee a l'agents*/
	SimpleWindow4Agent window;

	public  AgentWindowed() {
	}


	/**print a msg n the associated window*/
	void println(String msg)
	{
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				window.println(msg);
			}
		} );
	}

	@Override
	protected void onGuiEvent(GuiEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the window
	 */
	public SimpleWindow4Agent getWindow() {
		return window;
	}


	/**
	 * @param window the window to set
	 */
	public void setWindow(SimpleWindow4Agent window) {
		this.window = window;
	}


}
