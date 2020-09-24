package blade.interview.supermarket;

import java.util.Objects;

public class Item {

    public static final int MEAT = 0;
    public static final int DAIRY = 1;
    public static final int GREEN = 2;

    private String _name;
    private Integer _categoryCode;
    private double _price;

    public Item(String name, Integer categoryCode, double price) {
        _name = name;
        _categoryCode = categoryCode;
        _price = price;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public Integer getCategoryCode() {
        return _categoryCode;
    }

    public void setCategoryCode(Integer categoryCode) {
        this._categoryCode = categoryCode;
    }

    public double getPrice() {
        return _price;
    }

    public void setPrice(double price) {
        this._price = price;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Item item = (Item) o;
        return Double.compare(item._price, _price) == 0 &&
            _name.equals(item._name) &&
            _categoryCode.equals(item._categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _categoryCode, _price);
    }
}
