package protocoles.anglaisesscellees.agents;

import protocoles.anglaisesscellees.gui.SimpleWindow4Agent;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import javax.swing.*;


/**
 * GuiAgent linked to a simple window to display text
 *
 * @author revised by Emmanuel ADAM
 */
public   class AgentWindowed extends GuiAgent{


    SimpleWindow4Agent window;

    /**  GUI */

    public  AgentWindowed() {
    }


    /**print a msg n the associated window*/
    void println(String msg)
    {
        SwingUtilities.invokeLater(() -> window.println(msg));
    }

    /**fonction a remplir pour repondre aux evenements de la fenetre*/
    @Override
    protected  void onGuiEvent(GuiEvent arg0){}

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
