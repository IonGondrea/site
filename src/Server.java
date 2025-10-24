import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Desktop;
import java.net.URI;

public class Server {
    private static final Gson gson = new Gson();
    private static Connection conn;

    public static void main(String[] args) throws Exception {
        // start DB
        initDb();

        port(4567);
        // use absolute path so Spark finds static files when started from IDE or mvn
        String publicDir = System.getProperty("user.dir") + "/src/public";
        staticFiles.externalLocation(publicDir); // serve static files from project folder

        // API: list countries (from countries.json)
        get("/api/countries", (req, res) -> {
            res.type("application/json");
            try {
                String countriesPath = System.getProperty("user.dir") + "/src/public/countries.json";
                java.nio.file.Path path = java.nio.file.Paths.get(countriesPath);
                String content = new String(java.nio.file.Files.readAllBytes(path));
                return content;
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to load countries: " + e.getMessage()));
            }
        });

        // API: list products
        get("/api/products", (req, res) -> {
            res.type("application/json");
            List<Map<String,Object>> products = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT id,name,price,image FROM products ORDER BY id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> p = new HashMap<>();
                    p.put("id", rs.getInt("id"));
                    p.put("name", rs.getString("name"));
                    p.put("price", rs.getDouble("price"));
                    p.put("image", rs.getString("image")); // NEW: include image path
                    products.add(p);
                }
            }
            return gson.toJson(products);
        });

        // API: add to cart (JSON body: { "productId":1, "qty":2 })
        post("/api/cart/add", (req, res) -> {
            res.type("application/json");
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            int productId = body.get("productId").getAsInt();
            int qty = body.get("qty").getAsInt();
            if (qty <= 0) {
                res.status(400);
                return gson.toJson(Map.of("error", "qty must be positive"));
            }
            // check product exists
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(1) FROM products WHERE id = ?")) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        res.status(404);
                        return gson.toJson(Map.of("error", "Product not found"));
                    }
                }
            }
            // update or insert cart item
            try (PreparedStatement ps = conn.prepareStatement("SELECT qty FROM cart_items WHERE product_id = ?")) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int existing = rs.getInt("qty");
                        try (PreparedStatement up = conn.prepareStatement("UPDATE cart_items SET qty = ? WHERE product_id = ?")) {
                            up.setInt(1, existing + qty);
                            up.setInt(2, productId);
                            up.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO cart_items(product_id, qty) VALUES(?,?)")) {
                            ins.setInt(1, productId);
                            ins.setInt(2, qty);
                            ins.executeUpdate();
                        }
                    }
                }
            }
            return gson.toJson(Map.of("ok", true));
        });

        // API: view cart
        get("/api/cart", (req, res) -> {
            res.type("application/json");
            List<Map<String,Object>> items = new ArrayList<>();
            double total = 0.0;
            String sql = "SELECT c.product_id, c.qty, p.name, p.price FROM cart_items c JOIN products p ON c.product_id = p.id";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pid = rs.getInt("product_id");
                    int qty = rs.getInt("qty");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    double subtotal = price * qty;
                    total += subtotal;
                    Map<String,Object> it = new HashMap<>();
                    it.put("productId", pid);
                    it.put("name", name);
                    it.put("price", price);
                    it.put("qty", qty);
                    it.put("subtotal", subtotal);
                    items.add(it);
                }
            }
            Map<String,Object> resp = new HashMap<>();
            resp.put("items", items);
            resp.put("total", total);
            return gson.toJson(resp);
        });

        // API: checkout (clears cart, returns total)
        post("/api/checkout", (req, res) -> {
            res.type("application/json");
            double total = 0.0;
            String sql = "SELECT c.qty, p.price FROM cart_items c JOIN products p ON c.product_id = p.id";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    total += rs.getInt("qty") * rs.getDouble("price");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items")) {
                ps.executeUpdate();
            }
            return gson.toJson(Map.of("total", total, "message", "Purchase completed"));
        });

        // simple health endpoint
        get("/health", (req, res) -> "ok");

        // wait for server to start, then open browser to the app
        awaitInitialization();
        String url = "http://localhost:" + port() + "/";
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Open this URL in your browser: " + url);
            }
        } catch (Exception e) {
            System.err.println("Failed to open browser: " + e.getMessage());
        }
    }

    private static void initDb() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:./data/market;AUTO_SERVER=TRUE", "sa", "");
        try (Statement s = conn.createStatement()) {
            // create table with image column (if not exists)
            s.execute("CREATE TABLE IF NOT EXISTS products (id INT PRIMARY KEY, name VARCHAR(255), price DOUBLE, image VARCHAR(255))");
            s.execute("CREATE TABLE IF NOT EXISTS cart_items (product_id INT PRIMARY KEY, qty INT)");
            // ensure image column exists if older DB used (safe in H2)
            try {
                s.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS image VARCHAR(255)");
            } catch (SQLException ignore) {
                // ignore if not supported
            }
            // seed products if empty
            try (ResultSet rs = s.executeQuery("SELECT COUNT(1) FROM products")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    try (PreparedStatement ins = conn.prepareStatement("INSERT INTO products(id,name,price,image) VALUES(?,?,?,?)")) {
                        ins.setInt(1, 1); ins.setString(2, "Apple"); ins.setDouble(3, 0.50); ins.setString(4, "images/apple.svg"); ins.executeUpdate();
                        ins.setInt(1, 2); ins.setString(2, "Bread"); ins.setDouble(3, 1.20); ins.setString(4, "images/bread.svg"); ins.executeUpdate();
                        ins.setInt(1, 3); ins.setString(2, "Milk"); ins.setDouble(3, 0.99); ins.setString(4, "images/milk.svg"); ins.executeUpdate();
                        ins.setInt(1, 4); ins.setString(2, "Cheese"); ins.setDouble(3, 2.50); ins.setString(4, "images/cheese.svg"); ins.executeUpdate();
                        ins.setInt(1, 5); ins.setString(2, "Chocolate"); ins.setDouble(3, 1.75); ins.setString(4, "images/chocolate.svg"); ins.executeUpdate();
                    }
                }
            }
        }
    }
}
