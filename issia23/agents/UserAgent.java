package issia23.agents;

import issia23.data.Product;
import issia23.data.ProductType;
import issia23.gui.UserAgentWindow;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import java.util.*;

/**class related to the user, owner of products to repair
 * @author emmanueladam
 * */
public class UserAgent extends GuiAgent {
    /**list of products to repair*/
    List<Product> products;
    /**general skill of repairing*/
    int skill;
    /**gui window*/
    UserAgentWindow window;
    @Override
    public void setup()
    {
        this.window = new UserAgentWindow(getLocalName(),this);
        window.setButtonActivated(true);
        //add a random skill
        Random hasard = new Random();
        skill = hasard.nextInt(5);
        println("hello, I have a skill = "+ skill);
        //add some products choosen randomly in the list Product.getListProducts()
        products = new ArrayList<>();
        int nbTypeOfProducts = ProductType.values().length;
        int nbPoductsByType = Product.NB_PRODS / nbTypeOfProducts;
        var existingProducts = Product.getListProducts();
        //add products
        for(int i=0; i<nbTypeOfProducts; i++)
            if(hasard.nextBoolean())
               products.add(existingProducts.get(hasard.nextInt(nbPoductsByType) + (i*nbPoductsByType)));
        //we need at least one pooduct
        if(products.isEmpty())  products.add(existingProducts.get(hasard.nextInt(nbPoductsByType*nbTypeOfProducts)));
        window.addProductsToCombo(products);
        println("Here are my objects : ");
        products.forEach(p->println("\t"+p));

    }

    /**the window sends an evt to the agent*/
    @Override
    public void onGuiEvent(GuiEvent evt)
    {
        //if it is the OK button
        if(evt.getType()==UserAgentWindow.OK_EVENT)
        {
            //search about repair coffee
            var coffees = AgentServicesTools.searchAgents(this, "repair", "coffee");
            println("-".repeat(30));
            for(AID aid:coffees)
                println("found this repair coffee : " + aid.getLocalName());
            println("-".repeat(30));
            //TODO: Up to you to omplete the project....
        }
    }

    public void println(String s){window.println(s);}

    @Override
    public void takeDown(){println("bye !!!");}
}
