import java.io.*;
import java.util.*;

class User implements Serializable {
    String username;
    String password;
    boolean isAdmin;
    double cash;

    User(String username, String password, boolean isAdmin, double cash) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.cash = cash;
    }

    @Override
    public String toString() {
        return username + "," + password + "," + isAdmin + (isAdmin ? "" : "," + cash);
    }
}

class Product implements Serializable {
    int id;
    String name;
    double price;
    int stock;

    Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    @Override
    public String toString() {
        return id + "," + name + "," + price + "," + stock;
    }
}

class ECommerceApp {
    private Map<String, User> users = new HashMap<>();
    private Map<Integer, Product> products = new HashMap<>();
    private Map<String, List<Product>> carts = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);
    private User loggedInUser = null;
    private int productIdCounter = 1;

    public static void main(String[] args) {
        ECommerceApp app = new ECommerceApp();
        app.run();
    }

    void run() {
        loadData();
        System.out.println("Welcome to the E-Commerce App!");
        while (true) {
            if (loggedInUser == null) {
                showMainMenu();
            } else {
                System.out.println("\nWelcome, " + loggedInUser.username + "!");
                if (!loggedInUser.isAdmin) {
                    System.out.println("Cash Balance: $" + loggedInUser.cash);
                }
                if (loggedInUser.isAdmin) {
                    showAdminMenu();
                } else {
                    showUserMenu();
                }
            }
        }
    }

    void loadData() {
        try (ObjectInputStream userStream = new ObjectInputStream(new FileInputStream("users.dat"))) {
            users = (HashMap<String, User>) userStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading users data: " + e.getMessage());
        }

        try (ObjectInputStream productStream = new ObjectInputStream(new FileInputStream("products.dat"))) {
            products = (HashMap<Integer, Product>) productStream.readObject();
            productIdCounter = products.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading products data: " + e.getMessage());
        }
    }

    void saveData() {
        try (ObjectOutputStream userStream = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            userStream.writeObject(users);
        } catch (IOException e) {
            System.out.println("Error saving users data: " + e.getMessage());
        }

        try (ObjectOutputStream productStream = new ObjectOutputStream(new FileOutputStream("products.dat"))) {
            productStream.writeObject(products);
        } catch (IOException e) {
            System.out.println("Error saving products data: " + e.getMessage());
        }
    }

    void showMainMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. View Products");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> login();
            case 2 -> register();
            case 3 -> viewProducts();
            case 4 -> {
                saveData();
                System.out.println("Thank you for using the E-Commerce App! Goodbye!");
                System.exit(0);
            }
            default -> System.out.println("Invalid choice. Please try again.");
        }
    }

    void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = users.get(username);
        if (user != null && user.password.equals(password)) {
            loggedInUser = user;
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    void register() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Are you an admin? (yes/no): ");
        boolean isAdmin = scanner.nextLine().equalsIgnoreCase("yes");

        double cash = 0;
        if (!isAdmin) {
            System.out.print("Initial cash amount: ");
            cash = scanner.nextDouble();
            scanner.nextLine();
        }

        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
        } else {
            users.put(username, new User(username, password, isAdmin, cash));
            saveData();
            System.out.println("Registration successful.");
        }
    }

    void viewProducts() {
        System.out.println("Product List:");
        for (Product product : products.values()) {
            System.out.println(product.id + ". " + product.name + " - $" + product.price + " (" + product.stock + " in stock)");
        }
    }

    void showUserMenu() {
        while (loggedInUser != null) {
            System.out.println("\nUser Menu:");
            System.out.println("1. View Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Checkout");
            System.out.println("5. Deposit Cash");
            System.out.println("6. Withdraw Cash");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewProducts();
                case 2 -> addToCart();
                case 3 -> viewCart();
                case 4 -> checkout();
                case 5 -> depositCash();
                case 6 -> withdrawCash();
                case 7 -> {
                    System.out.println("Logging out...");
                    loggedInUser = null;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    void addToCart() {
        System.out.print("Enter product ID to add to cart: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        Product product = products.get(productId);
        if (product != null && product.stock > 0) {
            carts.computeIfAbsent(loggedInUser.username, k -> new ArrayList<>()).add(product);
            System.out.println("Product added to cart.");
        } else {
            System.out.println("Product not available.");
        }
    }

    void viewCart() {
        List<Product> cart = carts.get(loggedInUser.username);
        if (cart == null || cart.isEmpty()) {
            System.out.println("Your cart is empty.");
        } else {
            System.out.println("Your Cart:");
            for (Product product : cart) {
                System.out.println(product.id + ". " + product.name + " - $" + product.price);
            }
        }
    }

    void checkout() {
        List<Product> cart = carts.get(loggedInUser.username);
        if (cart == null || cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        double total = cart.stream().mapToDouble(p -> p.price).sum();
        if (loggedInUser.cash >= total) {
            System.out.println("Total amount: $" + total);
            System.out.print("Proceed to checkout? (yes/no): ");
            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                for (Product product : cart) {
                    product.stock--;
                }
                carts.remove(loggedInUser.username);
                loggedInUser.cash -= total;
                System.out.println("Checkout successful. New balance: $" + loggedInUser.cash);
                saveData();
            } else {
                System.out.println("Checkout cancelled.");
            }
        } else {
            System.out.println("Insufficient cash.");
        }
    }

    void depositCash() {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount > 0) {
            loggedInUser.cash += amount;
            System.out.println("Deposit successful. New balance: $" + loggedInUser.cash);
            saveData();
        } else {
            System.out.println("Invalid amount.");
        }
    }

    void withdrawCash() {
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount > 0 && amount <= loggedInUser.cash) {
            loggedInUser.cash -= amount;
            System.out.println("Withdrawal successful. New balance: $" + loggedInUser.cash);
            saveData();
        } else {
            System.out.println("Invalid amount or insufficient balance.");
        }
    }

    void showAdminMenu() {
        while (loggedInUser != null) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. View Products");
            System.out.println("2. Add Product");
            System.out.println("3. Remove Product");
            System.out.println("4. Manage Stock");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewProducts();
                case 2 -> addProduct();
                case 3 -> removeProduct();
                case 4 -> manageStock();
                case 5 -> {
                    System.out.println("Logging out...");
                    loggedInUser = null;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    void addProduct() {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter product price: ");
        double price = scanner.nextDouble();
        System.out.print("Enter product stock: ");
        int stock = scanner.nextInt();
        scanner.nextLine();

        int id = productIdCounter++;
        products.put(id, new Product(id, name, price, stock));
        System.out.println("Product added successfully.");
    }

    void removeProduct() {
        System.out.print("Enter product ID to remove: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        if (products.remove(productId) != null) {
            System.out.println("Product removed successfully.");
        } else {
            System.out.println("Product not found.");
        }
    }

    void manageStock() {
        System.out.print("Enter product ID to manage stock: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        Product product = products.get(productId);
        if (product != null) {
            System.out.print("Enter new stock quantity: ");
            int newStock = scanner.nextInt();
            scanner.nextLine();
            product.stock = newStock;
            System.out.println("Stock updated successfully.");
        } else {
            System.out.println("Product not found.");
        }
    }
}
