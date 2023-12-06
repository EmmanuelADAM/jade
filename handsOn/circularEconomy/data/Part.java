package handsOn.circularEconomy.data;

import java.util.ArrayList;
import java.util.List;

/**create the reference of the parts of each object.
 * For example, vacum cleaner has 3 parts, so we have vacumcleaner-part1,vacumcleaner-part2,vacumcleaner-part3.
 * the sum of the standard price of each part equals 80% of the price of the product.
 * If n is the number of parts, standard price of part-n is 2/3 of this value.
 * price of part n-1 is 2/3 of the remaining value,
 * price of part n-2 is 2/3 of the remaining value, etc.
 * price of part-1 is the remaining value
 * For example, for a vacumcleaner (standard price = 100€), which has 3 parts,
 * price of vacumcleaner-part3 is 2/3 x 80% of 100€ = 40€
 * price of vacumcleaner-part2 is 2/3 x 60% of 100€ = 30€
 * price of vacumcleaner-part1 is 80€ - 70€ = 10€
 * @author emmanueladam
 * */
public class Part{
    /**name of the part*/
    String name;
    /**price of the part*/
    Double standardPrice;
    /**type of object to which the part belongs*/
    ProductType type;
    /**list of existing parts in the project*/
    public static List<Part>  listParts;

    public Part(String name, ProductType type, Double standardPrice) {
        this.name = name;
        this.type = type;
        this.standardPrice = standardPrice;
    }

    public Part(Part p){
        this.name = p.name;
        this.type = p.type;
        this.standardPrice = p.standardPrice;
    }

    /**
     * create the list of parts if it does not exist.
     * for build the list,  it creates it from the details in the ProductType enum
     * @return list of parts
     */
    static public List<Part> getListParts() {
        if (listParts == null) {
            listParts = new ArrayList<>();
            var listeType = ProductType.values();
            for(var type:listeType){
                var partsPrice = type.standardPrice *.8;
                int nbParts = type.nbParts;
                double partPrice = 0;
                for(int i=nbParts; i>1; i--) {
                    partPrice = partsPrice * 2/3;
                    partsPrice = partsPrice - partPrice;
                    listParts.add(new Part(type+"-part"+ i, type, partPrice));
                }
                listParts.add(new Part(type+"-part1", type, partsPrice));
            }
        }
        return listParts;
    }


    public String toString() {
        return String.format("Part{ %s - %s - %.2f€}", name, type, standardPrice);
    }

    public String getName() {
        return name;
    }

    public Double getStandardPrice() {
        return standardPrice;
    }

    public ProductType getType() {
        return type;
    }

    /**check the creation of the list of parts (used by the part stores).
     * (you can store the creation in a file and reload it later)*/

    public static void main(String[] args)
    {
        var tab = Part.getListParts();
        for(var p:tab)  System.out.println(p);
        System.out.println("-".repeat(20));
    }

}
