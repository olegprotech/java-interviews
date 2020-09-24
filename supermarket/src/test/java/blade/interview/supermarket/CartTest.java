package blade.interview.supermarket;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CartTest {

    @Test
    public void testReceipt() {
        Cart cart = new Cart("12345");
        cart.addItem(new Item("Fresh milk", Item.DAIRY, 2.5));
        cart.addItem(new Item("Lettuce", Item.GREEN, 2.7));
        cart.addItem(new Item("Almond milk", Item.DAIRY, 2.8));
        cart.addItem(new Item("Pork chop", Item.MEAT, 3.5));
        cart.addItem(new Item("Pork chop", Item.MEAT, 3.5));
        cart.addItem(new Item("Lettuce", Item.GREEN, 2.7));
        cart.addItem(new Item("Broccoli", Item.GREEN, 3));
        cart.addItem(new Item("Pork chop", Item.MEAT, 3.5));

        String expected = "Receipt ID 12345\n" +
            "\t1 Fresh milk\t2.5\n" +
            "\t2 Lettuce\t4.86\n" +
            "\t1 Almond milk\t2.8\n" +
            "\t3 Pork chop\t10.5\n" +
            "\t1 Broccoli\t3.0\n" +
            "Total price: $23.66\n" +
            "You earned 6.5 points";
        assertEquals(expected, cart.getReceipt());
    }
}
