package handsOn.circularEconomy.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** a reparable product is identified by a name and an id.
 * it owns a type and a price
 * This class creates list of products that can be used in the project
 * @author emmanueladam
 * */
public class Product implements Serializable {

    /**nb of existing products*/
    static int nbProducts = 0;
    /**no of this product*/
    long id;
    /**name of the product*/
    String name;
    /**type of the product*/
    ProductType type;
    /***price of the product (a little greater than the standard price)*/
    double price;
    /**nb of products to create*/
    public static final int NB_PRODS = 100;
    /**list of products*/
    public static List<Product> listProducts;
    /**faulty part of the product*/
    Part faultyPart;

    /**constructor. set the name, the type and the price (between 0% and 20% more than the standard price)*/
    Product(String name, ProductType type){
        this.name = name;
        this.type = type;
        price = type.getStandardPrice()*(1.+Math.random()*.2);
        id = ++nbProducts;
    }

    @Override
    public String toString() {
        return String.format("Product{ %d : %s - %s - %.2f€}", id, name, type, price);
    }


    /**
     * create the list of NB_PRODS products if it does not exist.
     * for build the list,  it creates with an equal distribution the object of each type
     * @return list of product
     */
    static public List<Product> getListProducts() {
        if (listProducts == null) {
            listProducts = new ArrayList<>(NB_PRODS);
            int nbSpec = ProductType.values().length;
            int nbBySpec = NB_PRODS /nbSpec;
            var listeType = ProductType.values();
            for(var type:listeType){
                for(int i=0; i<nbBySpec; i++) {
                    listProducts.add(new Product(type+"-"+ i, type));
                }
            }
        }
        return listProducts;
    }


    /**function that select which part to repair
     * @return part selected randomly from the existing list of parts*/
    public Part getDefault() {
        if(faultyPart==null) {
            //choose a part
            var flux = Part.getListParts().stream().filter(p -> p.getType() == type);
            var l = flux.toList();
            faultyPart = l.get(new Random().nextInt(l.size()));
        }
        return faultyPart;
    }

    public String getName() {
        return name;
    }

    public ProductType getType() {
        return type;
    }
    public double getPrice() {
        return price;
    }

    /**check the creation of the list of products.
     * (you can store the creation in a file and reload it later)*/

    public static void main(String[] args)
    {
        var tab = Product.getListProducts();
        for(var p:tab)  System.out.println(p);
        System.out.println("-".repeat(20));
    }
}
