package handsOn.circularEconomy.agents;

import issia23.data.Product;
import issia23.data.ProductType;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** class that represents a repair agent.
 * It is declared in the service repair-coffee.
 * It owns some specialities
 * @author emmanueladam@
 * */
public class RepairCoffeeAgent extends AgentWindowed {
    List<ProductType> specialities;
    @Override
    public void setup(){
        this.window = new SimpleWindow4Agent(getLocalName(),this);
        this.window.setBackgroundTextColor(Color.orange);
        println("hello, do you want coffee ?");
        var hasard = new Random();

        specialities = new ArrayList<>();
        for(ProductType type : ProductType.values())
            if(hasard.nextBoolean()) specialities.add(type);
        //we need at least one speciality
        if(specialities.isEmpty()) specialities.add(ProductType.values()[hasard.nextInt(ProductType.values().length)]);
        println("I have these specialities : ");
        specialities.forEach(p->println("\t"+p));
        //registration to the yellow pages (Directory Facilitator Agent)
        AgentServicesTools.register(this, "repair", "coffee");
        println("I'm just registered as a repair-coffee");
    }

    private void analyseProduct(Product p) {
        var part = p.getDefault();
        println("Here is the faulty part : " + part + " for the object : " + p);
    }

}
