import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    private final Map<Product, Integer> items = new LinkedHashMap<>();

    public void add(Product p, int qty) {
        items.put(p, items.getOrDefault(p, 0) + qty);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double total() {
        return items.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();
    }

    public String viewContents() {
        if (items.isEmpty()) return "Cart is empty.";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Product, Integer> e : items.entrySet()) {
            Product p = e.getKey();
            int qty = e.getValue();
            sb.append(String.format("%s x%d = $%.2f%n", p.getName(), qty, p.getPrice() * qty));
        }
        return sb.toString();
    }

    public void clear() {
        items.clear();
    }
}

