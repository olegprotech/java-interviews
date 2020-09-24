package blade.interview.supermarket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Cart {

    private String id;
    private Map<Item, Integer> items = new LinkedHashMap<>(); // remember ordering of items

    public Cart(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void addItem(Item i) {
        Integer count = items.get(i);
        if (count == null)
            count = 0;
        items.put(i, ++count);
    }

    public String getReceipt() {
        String receipt = "Receipt ID " + this.id + "\n";
        double totalPrice = 0;
        double totalPoints = 0;
        for (Entry<Item, Integer> entry : items.entrySet()) {
            Item item = entry.getKey();
            Integer count = entry.getValue();
            double currentPrice = item.getPrice() * count;
            // 10 percent discount when buy a lot for all green items!
            if (item.getCategoryCode() != null && item.getCategoryCode() == Item.GREEN && currentPrice > 5)
                currentPrice *= 0.9;
            double currentPoints = 0;
            switch (item.getCategoryCode()) {
                case Item.MEAT:
                    currentPoints = 1; // no joy for meat eater
                    break;
                case Item.DAIRY:
                    currentPoints = 0.5 * count;
                    break;
                case Item.GREEN:
                    currentPoints = 1.5 * count; // more perks for healthy eating
                    break;
            }
            receipt += "\t" + count + " " + item.getName() + "\t" + String.valueOf(currentPrice) + "\n";
            totalPrice += currentPrice;
            totalPoints += currentPoints;
        }
        // Bill summary
        receipt += "Total price: $" + String.valueOf(totalPrice) + "\n";
        receipt += "You earned " + String.valueOf(totalPoints) + " points";
        return receipt;
    }
}
