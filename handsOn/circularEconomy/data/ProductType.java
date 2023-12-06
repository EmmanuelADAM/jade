package handsOn.circularEconomy.data;

import java.io.Serializable;


/**type of products capable ot be repaired
 * a type is associated with a nb of reparable parts
 * and a standard price
 * @author emmanueladam */
public enum ProductType implements Serializable {
    Mouse (1,30), screen(3,150), coffeeMaker(2,50),
    washingMachine(4,300), dishwasher(4,300), vacuumCleaner(3,100);
    int nbParts;
    double standardPrice;

    ProductType(int nbParts, double standardPrice){
        this.nbParts = nbParts;
        this.standardPrice = standardPrice;
    }


    public int getNbParts() {
        return nbParts;
    }

    public double getStandardPrice() {
        return standardPrice;
    }
}
