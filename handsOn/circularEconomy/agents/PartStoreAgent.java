package handsOn.circularEconomy.agents;

import issia23.data.Part;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/** class that represents a part-Store agent.
 * It is declared in the service repair-partstore.
 * It an infitine number of specific part wih a pecific cost ( up to 30% more than the standard price)
 * @author emmanueladam
 * */
public class PartStoreAgent extends AgentWindowed {
    List<Part> parts;
    @Override
    public void setup(){
        this.window = new SimpleWindow4Agent(getLocalName(),this);
        this.window.setBackgroundTextColor(Color.LIGHT_GRAY);
        AgentServicesTools.register(this, "repair", "partstore");
        println("hello, I'm just registered as a parts-store");
        println("do you want some special parts ?");
        Random hasard = new Random();
        parts = new ArrayList<>();
        var existingParts = Part.getListParts();
        for(Part p : existingParts)
            if(hasard.nextBoolean())
                parts.add(new Part(p.getName(), p.getType(), p.getStandardPrice()*(1+Math.random()*.3)));
        //we need at least one speciality
        if(parts.isEmpty()) parts.add(existingParts.get(hasard.nextInt(existingParts.size())));
        println("here are the parts I sell : ");
        parts.forEach(p->println("\t"+p));



        //registration to the yellow pages (Directory Facilitator Agent)

    }

}
