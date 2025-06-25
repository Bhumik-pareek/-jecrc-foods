import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.*;
import java.util.stream.Collectors;

    /**
     * JECRC FOODS - Food Ordering Desktop App using JavaFX
     * <p>
     * Features implemented:
     * - Header with logo, search bar, navigation
     * - Left filter sidebar with category filter
     * - Center product grid with foods
     * - Right cart sidebar with live updates and checkout preview
     * - Add to cart with quantity control
     * - Cart updates and total price calculation
     * <p>
     */
    public class JECRC_FoodsApp extends Application {

        // Data Structures
        static class Product {
            String id;
            String name;
            String description;
            double price;
            String category;
            String imageUrl;
            int stock;

            Product(String id, String name, String desc, double price, String category, String imageUrl, int stock) {
                this.id = id;
                this.name = name;
                this.description = desc;
                this.price = price;
                this.category = category;
                this.imageUrl = imageUrl;
                this.stock = stock;
            }
        }

        static class CartItem {
            Product product;
            IntegerProperty quantity = new SimpleIntegerProperty(1);

            CartItem(Product product) {
                this.product = product;
            }

            double getTotalPrice() {
                return product.price * quantity.get();
            }
        }

        // Sample product data
        private final ObservableList<Product> products = FXCollections.observableArrayList(
                new Product("p1", "Margherita Pizza", "Classic cheese and tomato pizza.", 5.99, "Pizza",
                        "https://i.pinimg.com/736x/cc/54/dd/cc54dd8f45ed9cdd514e57c0e2afccd3.jpg", 20),
                new Product("p2", "Veggie Burger", "Delicious vegetable patty with fresh salad.", 3.49, "Burgers",
                        "https://i.pinimg.com/736x/27/ab/5e/27ab5edd0885b823023a2b5ba47a1f04.jpg", 15),
                new Product("p3", "Caesar Salad", "Fresh lettuce with Caesar dressing.", 3.99, "Salads",
                        "https://i.pinimg.com/736x/fd/35/0d/fd350dbea5b3b14ce39d97c5eb8e1335.jpg", 12),
                new Product("p4", "Chicken Biryani", "Aromatic spiced chicken and rice.", 9.99, "Indian",
                        "https://i.pinimg.com/736x/c5/d2/62/c5d262f3377da91a7229772026a2ec5c.jpg", 10),
                new Product("p5", "Chocolate Cake", "Rich chocolate layered cake slice.", 5.00, "Desserts",
                        "https://i.pinimg.com/736x/d0/2c/fd/d02cfdbc13aef67e3f01531a137b2d82.jpg", 8)
        );

        // Cart items map keyed by product id
        private final ObservableMap<String, CartItem> cartItems = FXCollections.observableHashMap();

        // UI Controls that need to be accessed globally in the app
        private FlowPane productGrid;
        private VBox cartSidebar;
        private Label cartCountLabel;
        private Label totalPriceLabel;
        private TextField searchField;
        private ComboBox<String> categoryFilterCombo;

        @Override
        public void start(Stage primaryStage) {
            primaryStage.setTitle("JECRC FOODS - Food Ordering App");

            BorderPane root = new BorderPane();

            // Header
            root.setTop(createHeader());

            // Main layout - three columns: left filters, center products, right cart
            HBox mainContent = new HBox();
            mainContent.setSpacing(16);
            mainContent.setPadding(new Insets(16));

            // Left Sidebar: Filters
            VBox filterSidebar = createFilterSidebar();
            filterSidebar.setPrefWidth(180);

            // Center: Product Grid
            ScrollPane centerScroll = new ScrollPane();
            centerScroll.setFitToWidth(true);
            productGrid = new FlowPane();
            productGrid.setPadding(new Insets(10));
            productGrid.setHgap(20);
            productGrid.setVgap(20);
            productGrid.setPrefWrapLength(800);
            centerScroll.setContent(productGrid);
            VBox centerContainer = new VBox(centerScroll);
            VBox.setVgrow(centerScroll, Priority.ALWAYS);

            // Right Sidebar: Cart
            cartSidebar = createCartSidebar();
            cartSidebar.setPrefWidth(350);

            mainContent.getChildren().addAll(filterSidebar, centerContainer, cartSidebar);
            HBox.setHgrow(centerContainer, Priority.ALWAYS);

            root.setCenter(mainContent);

            // Initial populate product grid
            updateProductGrid("");

            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        private HBox createHeader() {
            HBox header = new HBox();
            header.setStyle("-fx-background-color: linear-gradient(to right, #7c3aed, #c084fc);");
            header.setPadding(new Insets(10, 20, 10, 20));
            header.setSpacing(16);
            header.setAlignment(Pos.CENTER_LEFT);

            Label logoLabel = new Label("JECRC Foods");
            logoLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
            logoLabel.setMinWidth(200);

            // Search bar
            searchField = new TextField();
            searchField.setPromptText("Search food items...");
            searchField.setPrefWidth(400);
            searchField.textProperty().addListener((_, oldVal, newVal) -> {
                updateProductGrid(newVal.trim());
            });

            // Cart icon with count
            StackPane cartIconPane = new StackPane();
            Label cartIconLabel = new Label("\uD83D\uDED2"); // Shopping cart emoji as fallback icon
            cartIconLabel.setStyle("-fx-font-size:24px; -fx-text-fill: white;");
            cartCountLabel = new Label("0");
            cartCountLabel.setStyle(
                    "-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 15px; -fx-min-height: 15px; -fx-alignment: center; -fx-background-radius: 10px;"
            );
            StackPane.setAlignment(cartCountLabel, Pos.TOP_RIGHT);
            StackPane.setMargin(cartCountLabel, new Insets(0, 0, 15, 15));
            cartIconPane.getChildren().addAll(cartIconLabel, cartCountLabel);

            header.getChildren().addAll(logoLabel, searchField, cartIconPane);

            return header;
        }

        private ComboBox<String> priceSortCombo;
        private VBox createFilterSidebar() {
            VBox sidebar = new VBox();
            sidebar.setSpacing(24);

            // Filters title
            Label filterTitle = new Label("Filters");
            filterTitle.setFont(Font.font(20));

            // Category Filter
            Label categoryLabel = new Label("Category");
            categoryFilterCombo = new ComboBox<>();
            categoryFilterCombo.getItems().addAll(
                    "All", "Pizza", "Burgers", "Salads", "Indian", "Desserts"
            );
            categoryFilterCombo.setValue("All");
            categoryFilterCombo.valueProperty().addListener((obs, prev, curr) -> {
                updateProductGrid(searchField.getText().trim());
            });


            // Sort by Price Filter
            Label sortLabel = new Label("Sort by Price");
            priceSortCombo = new ComboBox<>();
            priceSortCombo.getItems().addAll("Default", "Low to High", "High to Low");
            priceSortCombo.setValue("Default");
            priceSortCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateProductGrid(searchField.getText().trim());  // You will modify this function to sort too
            });

            // Add all to sidebar
            sidebar.getChildren().addAll(filterTitle, categoryLabel, categoryFilterCombo, sortLabel, priceSortCombo);

            return sidebar;
        }

        private VBox createCartSidebar() {
            VBox sidebar = new VBox();
            sidebar.setSpacing(12);
            sidebar.setPadding(new Insets(10));
            sidebar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ccc; -fx-border-width: 0 0 0 1px;");

            Label title = new Label("Shopping Cart");
            title.setFont(Font.font(20));

            ListView<CartItem> cartListView = new ListView<>();
            cartListView.setPrefHeight(500);
            cartListView.setCellFactory(param -> new CartItemCell());

            // Bind cart items list to ListView
            ObservableList<CartItem> cartObservableList = FXCollections.observableArrayList();
            cartListView.setItems(cartObservableList);

            // Update list whenever cartItems map changes
            cartItems.addListener((MapChangeListener.Change<? extends String, ? extends CartItem> change) -> {
                cartObservableList.setAll(cartItems.values());
                updateCartCountAndTotal();
            });

            // Total price label
            totalPriceLabel = new Label("Total: ₹0.00");
            totalPriceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Checkout button
            Button checkoutBtn = new Button("Checkout");
            checkoutBtn.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold;");
            checkoutBtn.setMaxWidth(Double.MAX_VALUE);
            checkoutBtn.setOnAction(e -> handleCheckout());

            sidebar.getChildren().addAll(title, cartListView, totalPriceLabel, checkoutBtn);
            VBox.setVgrow(cartListView, Priority.ALWAYS);

            return sidebar;
        }

        private void updateProductGrid(String filterText) {
            productGrid.getChildren().clear();
            String filterCategory = categoryFilterCombo.getValue();
            String sortOption = priceSortCombo.getValue();

            List<Product> filtered = products.stream()
                    .filter(p -> {
                        boolean matchesText = filterText == null || filterText.isEmpty() ||
                                p.name.toLowerCase().contains(filterText.toLowerCase()) ||
                                p.description.toLowerCase().contains(filterText.toLowerCase());
                        boolean matchesCategory = filterCategory == null || filterCategory.equals("All") || p.category.equals(filterCategory);
                        return matchesText && matchesCategory;
                    })
                    .collect(Collectors.toList());

            // Sort by Price
            if ("Low to High".equals(sortOption)) {
                filtered.sort(Comparator.comparingDouble(p -> p.price));
            } else if ("High to Low".equals(sortOption)) {
                filtered.sort((p1, p2) -> Double.compare(p2.price, p1.price));
            }


            for (Product p : filtered) {
                productGrid.getChildren().add(createProductCard(p));
            }
        }

        private VBox createProductCard(Product product) {
            VBox card = new VBox();
            card.setPrefWidth(200);
            card.setSpacing(8);
            card.setPadding(new Insets(10));
            card.setStyle(
                    "-fx-background-color: white; -fx-border-color: rgba(221,221,221,0.78); -fx-border-radius: 8px; -fx-background-radius: 8px;"
            );

            ImageView imageView = new ImageView();
            Image image = new Image(product.imageUrl, 200, 140, true, true);

            if (image.isError()) {
                System.out.println("❌ Failed to load image for: " + product.name);
                System.out.println("URL: " + product.imageUrl);
                System.out.println("Reason: " + image.getException());
            }

            imageView.setImage(image);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);
            imageView.setFitHeight(140);
            imageView.setAccessibleText(product.name + ", image");

            Label nameLabel = new Label(product.name);
            nameLabel.setFont(Font.font(16));
            nameLabel.setWrapText(true);

            Label descLabel = new Label(product.description);
            descLabel.setStyle("-fx-text-fill: #555;");
            descLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("₹%.0f", product.price * 83.0)); // approximate conversion rate

            priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7c3aed;");

            // Add to Cart Button and quantity selector
            HBox actionBox = new HBox();
            actionBox.setSpacing(8);
            actionBox.setAlignment(Pos.CENTER_LEFT);

            Spinner<Integer> quantitySpinner = new Spinner<>(1, product.stock, 1);
            quantitySpinner.setPrefWidth(80);

            Button addToCartBtn = new Button("Add");
            addToCartBtn.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white;");

            addToCartBtn.setOnAction(e -> {
                int qty = quantitySpinner.getValue();
                addToCart(product, qty);
            });

            actionBox.getChildren().addAll(quantitySpinner, addToCartBtn);

            card.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, actionBox);

            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color: #fafafa; -fx-border-color: #7c3aed; -fx-border-radius: 8px; -fx-background-radius: 8px;"));
            card.setOnMouseExited(e -> card.setStyle(
                    "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8px; -fx-background-radius: 8px;"));

            return card;
        }

        private void addToCart(Product product, int quantity) {
            CartItem existing = cartItems.get(product.id);
            if (existing != null) {
                int newQty = existing.quantity.get() + quantity;
                if (newQty <= product.stock) {
                    existing.quantity.set(newQty);
                } else {
                    existing.quantity.set(product.stock);
                }
            } else {
                CartItem newItem = new CartItem(product);
                newItem.quantity.set(Math.min(quantity, product.stock));
                cartItems.put(product.id, newItem);
            }
            updateCartCountAndTotal();
        }

        private void updateCartCountAndTotal() {
            int totalCount = cartItems.values().stream()
                    .mapToInt(item -> item.quantity.get())
                    .sum();
            cartCountLabel.setText(String.valueOf(totalCount));

            double totalPrice = cartItems.values().stream()
                    .mapToDouble(CartItem::getTotalPrice)
                    .sum();
            totalPriceLabel.setText(String.format("Total: ₹%.0f", totalPrice * 83));

        }

        private void handleCheckout() {
            if (cartItems.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your cart is empty.");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            // For demo: simple confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Confirm Checkout");
            confirm.setContentText("Proceed with checkout? Total: " + totalPriceLabel.getText());
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                cartItems.clear();
                updateCartCountAndTotal();
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Thank you for your purchase!");
                success.setHeaderText(null);
                success.showAndWait();
            }
        }

        private class CartItemCell extends ListCell<CartItem> {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox container = new HBox();
                    container.setSpacing(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    ImageView img = new ImageView();
                    try {
                        Image image = new Image(item.product.imageUrl, 60, 45, true, true);
                        img.setImage(image);
                    } catch (Exception e) {}

                    img.setFitWidth(60);
                    img.setFitHeight(45);
                    img.setPreserveRatio(true);

                    VBox details = new VBox();
                    Label nameLabel = new Label(item.product.name);
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    Label priceLabel = new Label(String.format("₹%.0f each", item.product.price * 83));


                    details.getChildren().addAll(nameLabel, priceLabel);

                    // Quantity spinner
                    Spinner<Integer> qtySpinner = new Spinner<>(1, item.product.stock, item.quantity.get());
                    qtySpinner.setPrefWidth(75);
                    qtySpinner.getValueFactory().valueProperty().bindBidirectional(item.quantity.asObject());


                    qtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateCartCountAndTotal());

                    // Remove button
                    Button removeBtn = new Button("Remove");
                    removeBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white;");
                    removeBtn.setOnAction(e -> {
                        cartItems.remove(item.product.id);
                    });

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    container.getChildren().addAll(img, details, spacer, qtySpinner, removeBtn);

                    setGraphic(container);
                }
            }
        }

        public static void main(String[] args) {
            launch(args);
        }
    }



