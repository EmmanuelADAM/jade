package issia23.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;

public class UserAgent extends AgentWindowed {
    int skill;
    @Override
    public void setup()
    {
        this.window = new SimpleWindow4Agent(getLocalName(),this);
        window.setButtonActivated(true);

        skill = (int)(Math.random()*4);
        println("hello, I have a skill = "+ skill);
    }

    @Override
    public void onGuiEvent(GuiEvent evt)
    {
        //I suppose there is only one type of event, clic on go
        //search about repair coffee
        var coffees = AgentServicesTools.searchAgents(this, "repair", "coffee");
        println("-".repeat(30));
        for(AID aid:coffees)
            println("found this repair coffee : " + aid.getLocalName());
        println("-".repeat(30));
    }

    @Override
    public void takeDown(){println("bye !!!");}
}
