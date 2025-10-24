import java.util.ArrayList;
import java.util.List;

public class Market {
    private final List<Product> products = new ArrayList<>();

    public Market() {
        // seed some sample products
        products.add(new Product(1, "Apple", 0.50));
        products.add(new Product(2, "Bread", 1.20));
        products.add(new Product(3, "Milk", 0.99));
        products.add(new Product(4, "Cheese", 2.50));
        products.add(new Product(5, "Chocolate", 1.75));
    }

    public List<Product> listProducts() {
        return new ArrayList<>(products);
    }

    public Product getProductById(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }
}

