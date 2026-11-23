package com.foodie.admin.data;

import com.foodie.admin.model.FoodOrder;
import com.foodie.admin.model.FoodOrderItem;
import com.foodie.admin.model.Rider;
import com.foodie.admin.repository.FoodOrderRepository;
import com.foodie.admin.repository.RiderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_SERVICE_PIN_CODES = "531162,531163";
    private final FoodOrderRepository orders;
    private final RiderRepository riders;

    public DataSeeder(FoodOrderRepository orders, RiderRepository riders) {
        this.orders = orders;
        this.riders = riders;
    }

    @Override
    public void run(String... args) {
        normalizeRiderStatuses();
        ensureRiderServicePinCodes();
        ensureDefaultRiderRatings();
        normalizeOrderStatuses();
        seedDemoOrders();
        ensureOrderOtpCodes();
    }

    private void normalizeRiderStatuses() {
        List<Rider> updates = riders.findAll().stream()
                .filter(rider -> !"ONLINE".equals(rider.getStatus())
                        && !"OFFLINE".equals(rider.getStatus())
                        && !"ON_DELIVERY".equals(rider.getStatus()))
                .toList();
        updates.forEach(rider -> rider.setStatus("OFFLINE"));
        if (!updates.isEmpty()) riders.saveAll(updates);
    }

    private void ensureRiderServicePinCodes() {
        List<Rider> updates = riders.findAll().stream()
                .filter(rider -> rider.getServicePinCodes() == null || rider.getServicePinCodes().isBlank())
                .toList();
        updates.forEach(rider -> rider.setServicePinCodes(DEFAULT_SERVICE_PIN_CODES));
        if (!updates.isEmpty()) riders.saveAll(updates);
    }

    private void ensureDefaultRiderRatings() {
        List<Rider> updates = riders.findAll().stream()
                .filter(rider -> rider.getRating() <= 0.0)
                .toList();
        updates.forEach(rider -> rider.setRating(Rider.DEFAULT_RATING));
        if (!updates.isEmpty()) riders.saveAll(updates);
    }

    private void normalizeOrderStatuses() {
        Map<String, String> legacyStatuses = Map.of(
                "AWAITING_SELLER", "PLACED",
                "PREPARING", "ACCEPTED",
                "READY_FOR_PICKUP", "READY",
                "AWAITING_RIDER", "READY",
                "ON_THE_WAY", "DISPATCHED",
                "CANCELLED", "REJECTED");
        List<FoodOrder> updates = orders.findAll().stream()
                .filter(order -> order.getStatus() != null && legacyStatuses.containsKey(order.getStatus()))
                .toList();
        updates.forEach(order -> order.setStatus(legacyStatuses.get(order.getStatus())));
        if (!updates.isEmpty()) orders.saveAll(updates);
    }

    private void seedDemoOrders() {
        Instant now = Instant.now();
        seedDemoOrder("DEMO-1001", "Ananya Rao", "+919876543210",
                "REST-01", "Bheemunipatnam Breakfast House", "12 Beach Road, Bheemunipatnam, Visakhapatnam 531163",
                "Flat 4B, Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8902, 83.4521, 17.8883, 83.4508, 340.0, "UPI",
                List.of(new FoodOrderItem("Masala Dosa", 180.0, 1),
                        new FoodOrderItem("Filter Coffee", 80.0, 2)),
                now.minusSeconds(12 * 60L), now.plusSeconds(28 * 60L), "demo-track-1001");
        seedDemoOrder("DEMO-1002", "Rahul Menon", "+919876543211",
                "REST-02", "Bheemili Tiffin Room", "221 Main Road, Bheemunipatnam, Visakhapatnam 531162",
                "No. 18, Lighthouse Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8950, 83.4498, 17.8919, 83.4510, 285.0, "CARD",
                List.of(new FoodOrderItem("Paneer Roll", 285.0, 1)),
                now.minusSeconds(6 * 60L), now.plusSeconds(34 * 60L), "demo-track-1002");
        seedDemoOrder("DEMO-1003", "Meera Iyer", "+919876543212",
                "REST-03", "Bheemunipatnam Biryani Corner", "44 Beach Road, Bheemunipatnam, Visakhapatnam 531163",
                "Flat 12, Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8866, 83.4475, 17.8924, 83.4544, 330.0, "UPI",
                List.of(new FoodOrderItem("Vada Pav", 90.0, 1),
                        new FoodOrderItem("Mango Lassi", 120.0, 2)),
                now.minusSeconds(9 * 60L), now.plusSeconds(31 * 60L), "demo-track-1003");
        seedDemoOrder("DEMO-1004", "Arjun Nair", "+919876543213",
                "REST-04", "Bheemunipatnam Spice Kitchen", "8 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531162",
                "22 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8990, 83.4570, 17.8951, 83.4552, 320.0, "CARD",
                List.of(new FoodOrderItem("Chicken Biryani", 280.0, 1),
                        new FoodOrderItem("Raita", 40.0, 1)),
                now.minusSeconds(15 * 60L), now.plusSeconds(25 * 60L), "demo-track-1004");
        seedDemoOrder("DEMO-1005", "Kavya Reddy", "+919876543214",
                "REST-05", "Bheemili Burger Yard", "77 Main Road, Bheemunipatnam, Visakhapatnam 531163",
                "9 Fishermen Colony Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8837, 83.4454, 17.8893, 83.4531, 560.0, "UPI",
                List.of(new FoodOrderItem("Classic Burger", 220.0, 2),
                        new FoodOrderItem("Peri Peri Fries", 120.0, 1)),
                now.minusSeconds(4 * 60L), now.plusSeconds(36 * 60L), "demo-track-1005");
        seedDemoOrder("DEMO-1006", "Vikram Shah", "+919876543215",
                "REST-06", "Green Leaf Bheemunipatnam", "16 Beach Road, Bheemunipatnam, Visakhapatnam 531162",
                "41 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8972, 83.4485, 17.8907, 83.4499, 520.0, "CASH",
                List.of(new FoodOrderItem("South Indian Thali", 260.0, 2)),
                now.minusSeconds(18 * 60L), now.plusSeconds(22 * 60L), "demo-track-1006");
        seedDemoOrder("DEMO-1007", "Nisha Thomas", "+919876543216",
                "REST-07", "Bheemunipatnam Dosa District", "102 Beach Road, Bheemunipatnam, Visakhapatnam 531163",
                "5 Bheemili Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8819, 83.4517, 17.8877, 83.4464, 250.0, "UPI",
                List.of(new FoodOrderItem("Set Dosa", 160.0, 1),
                        new FoodOrderItem("Filter Coffee", 90.0, 1)),
                now.minusSeconds(11 * 60L), now.plusSeconds(29 * 60L), "demo-track-1007");
        seedDemoOrder("DEMO-1008", "Aditya Kulkarni", "+919876543217",
                "REST-08", "Bheemili Oven Pizza", "35 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531162",
                "14 Main Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8935, 83.4544, 17.8842, 83.4391, 630.0, "CARD",
                List.of(new FoodOrderItem("Farmhouse Pizza", 450.0, 1),
                        new FoodOrderItem("Garlic Bread", 180.0, 1)),
                now.minusSeconds(7 * 60L), now.plusSeconds(33 * 60L), "demo-track-1008");
        seedDemoOrder("DEMO-1009", "Pooja Das", "+919876543218",
                "REST-09", "Bheemunipatnam Morning Tiffin", "19 Lighthouse Road, Bheemunipatnam, Visakhapatnam 531163",
                "3 Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8921, 83.4415, 17.8983, 83.4530, 310.0, "CASH",
                List.of(new FoodOrderItem("Idli", 120.0, 1),
                        new FoodOrderItem("Medu Vada", 100.0, 1),
                        new FoodOrderItem("Filter Coffee", 90.0, 1)),
                now.minusSeconds(21 * 60L), now.plusSeconds(19 * 60L), "demo-track-1009");
        seedDemoOrder("DEMO-1010", "Rohan Shetty", "+919876543219",
                "REST-10", "Bheemunipatnam Noodle Box", "60 Beach Road, Bheemunipatnam, Visakhapatnam 531162",
                "27 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8874, 83.4580, 17.8916, 83.4435, 400.0, "UPI",
                List.of(new FoodOrderItem("Hakka Noodles", 240.0, 1),
                        new FoodOrderItem("Veg Spring Roll", 160.0, 1)),
                now.minusSeconds(13 * 60L), now.plusSeconds(27 * 60L), "demo-track-1010");
        seedDemoOrder("DEMO-1011", "Ishita Rao", "+919876543220",
                "REST-11", "Bheemunipatnam Curry House", "14 Main Road, Bheemunipatnam, Visakhapatnam 531163",
                "8 Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8947, 83.4478, 17.8828, 83.4471, 390.0, "UPI",
                List.of(new FoodOrderItem("Paneer Butter Masala", 260.0, 1),
                        new FoodOrderItem("Butter Naan", 65.0, 2)),
                now.minusSeconds(5 * 60L), now.plusSeconds(35 * 60L), "demo-track-1011");
        seedDemoOrder("DEMO-1012", "Kiran Joseph", "+919876543221",
                "REST-12", "Bheemili Masala Dosa", "3 Beach Road, Bheemunipatnam, Visakhapatnam 531162",
                "21 Lighthouse Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8899, 83.4448, 17.8963, 83.4506, 245.0, "CARD",
                List.of(new FoodOrderItem("Mysore Masala Dosa", 150.0, 1),
                        new FoodOrderItem("Badam Milk", 95.0, 1)),
                now.minusSeconds(8 * 60L), now.plusSeconds(32 * 60L), "demo-track-1012");
        seedDemoOrder("DEMO-1013", "Sana Khan", "+919876543222",
                "REST-13", "Bheemunipatnam Bowl Co.", "29 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531163",
                "6 Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8789, 83.4564, 17.8978, 83.4429, 420.0, "CASH",
                List.of(new FoodOrderItem("Veg Buddha Bowl", 280.0, 1),
                        new FoodOrderItem("Mango Smoothie", 140.0, 1)),
                now.minusSeconds(10 * 60L), now.plusSeconds(30 * 60L), "demo-track-1013");
        seedDemoOrder("DEMO-1014", "Dev Patel", "+919876543223",
                "REST-14", "Bheemunipatnam Street Kitchen", "55 Main Road, Bheemunipatnam, Visakhapatnam 531162",
                "17 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8998, 83.4460, 17.8856, 83.4538, 350.0, "UPI",
                List.of(new FoodOrderItem("Chicken Kathi Roll", 230.0, 1),
                        new FoodOrderItem("Masala Lemonade", 120.0, 1)),
                now.minusSeconds(3 * 60L), now.plusSeconds(37 * 60L), "demo-track-1014");
        seedDemoOrder("DEMO-1015", "Tara Nair", "+919876543224",
                "REST-15", "Bheemunipatnam Breakfast Club", "10 Beach Road, Bheemunipatnam, Visakhapatnam 531163",
                "32 Main Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8849, 83.4503, 17.8930, 83.4574, 275.0, "CARD",
                List.of(new FoodOrderItem("Rava Idli", 165.0, 1),
                        new FoodOrderItem("Filter Coffee", 55.0, 2)),
                now.minusSeconds(14 * 60L), now.plusSeconds(26 * 60L), "demo-track-1015");
        seedDemoOrder("DEMO-1016", "Maya Reddy", "+919876543225",
                "REST-16", "Bheemili Coastal Kitchen", "7 Beach Road, Bheemunipatnam, Visakhapatnam 531162",
                "14 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8912, 83.4522, 17.8879, 83.4488, 315.0, "UPI",
                List.of(new FoodOrderItem("Andhra Veg Meals", 225.0, 1),
                        new FoodOrderItem("Sweet Pongal", 90.0, 1)),
                now.minusSeconds(7 * 60L), now.plusSeconds(33 * 60L), "demo-track-1016");
        seedDemoOrder("DEMO-1017", "Ravi Kumar", "+919876543226",
                "REST-17", "Bheemunipatnam Seaside Cafe", "18 Lighthouse Road, Bheemunipatnam, Visakhapatnam 531163",
                "5 Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8861, 83.4492, 17.8927, 83.4550, 260.0, "CARD",
                List.of(new FoodOrderItem("Paneer Fried Rice", 190.0, 1),
                        new FoodOrderItem("Lime Soda", 70.0, 1)),
                now.minusSeconds(9 * 60L), now.plusSeconds(31 * 60L), "demo-track-1017");
        seedDemoOrder("DEMO-1018", "Leela Varma", "+919876543227",
                "REST-18", "Bheemili Garden Restaurant", "3 Main Road, Bheemunipatnam, Visakhapatnam 530048",
                "22 Beach Road, Bheemunipatnam, Visakhapatnam 530048", "530048",
                17.8964, 83.4519, 17.8892, 83.4465, 385.0, "UPI",
                List.of(new FoodOrderItem("Mushroom Biryani", 295.0, 1),
                        new FoodOrderItem("Curd Rice", 90.0, 1)),
                now.minusSeconds(6 * 60L), now.plusSeconds(34 * 60L), "demo-track-1018");
        seedDemoOrder("DEMO-1019", "Naveen Das", "+919876543228",
                "REST-19", "Bheemunipatnam Spice Corner", "31 Kothapalem Road, Bheemunipatnam, Visakhapatnam 530048",
                "9 Lighthouse Road, Bheemunipatnam, Visakhapatnam 530048", "530048",
                17.8981, 83.4556, 17.8848, 83.4513, 410.0, "CASH",
                List.of(new FoodOrderItem("Chicken 65", 230.0, 1),
                        new FoodOrderItem("Chicken Pulao", 180.0, 1)),
                now.minusSeconds(11 * 60L), now.plusSeconds(29 * 60L), "demo-track-1019");
        seedDemoOrder("DEMO-1020", "Siri Mohan", "+919876543229",
                "REST-20", "Bheemunipatnam Family Kitchen", "6 Beach Road, Bheemunipatnam, Visakhapatnam 530045",
                "16 Main Road, Bheemunipatnam, Visakhapatnam 530045", "530045",
                17.8836, 83.4543, 17.8958, 83.4491, 335.0, "CARD",
                List.of(new FoodOrderItem("Veg Pulao", 215.0, 1),
                        new FoodOrderItem("Gobi Manchurian", 120.0, 1)),
                now.minusSeconds(4 * 60L), now.plusSeconds(36 * 60L), "demo-track-1020");
        seedDemoOrder("DEMO-1021", "Karthik Rao", "+919876543230",
                "REST-21", "Bheemili Tandoori House", "42 Beach Road, Bheemunipatnam, Visakhapatnam 530045",
                "11 Kothapalem Road, Bheemunipatnam, Visakhapatnam 530045", "530045",
                17.8938, 83.4447, 17.8817, 83.4571, 475.0, "UPI",
                List.of(new FoodOrderItem("Tandoori Chicken", 360.0, 1),
                        new FoodOrderItem("Rumali Roti", 115.0, 1)),
                now.minusSeconds(13 * 60L), now.plusSeconds(27 * 60L), "demo-track-1021");
        seedDemoOrder("DEMO-1022", "Anu Prasad", "+919876543231",
                "REST-22", "Bheemunipatnam Dosa Point", "25 Lighthouse Road, Bheemunipatnam, Visakhapatnam 531162",
                "4 Main Road, Bheemunipatnam, Visakhapatnam 531162", "531162",
                17.8975, 83.4468, 17.8851, 83.4496, 205.0, "CASH",
                List.of(new FoodOrderItem("Onion Rava Dosa", 145.0, 1),
                        new FoodOrderItem("Filter Coffee", 60.0, 1)),
                now.minusSeconds(8 * 60L), now.plusSeconds(32 * 60L), "demo-track-1022");
        seedDemoOrder("DEMO-1023", "Vivek Sahu", "+919876543232",
                "REST-23", "Bheemunipatnam Bowl House", "9 Kothapalem Road, Bheemunipatnam, Visakhapatnam 531163",
                "28 Beach Road, Bheemunipatnam, Visakhapatnam 531163", "531163",
                17.8896, 83.4572, 17.8942, 83.4438, 295.0, "UPI",
                List.of(new FoodOrderItem("Paneer Rice Bowl", 225.0, 1),
                        new FoodOrderItem("Buttermilk", 70.0, 1)),
                now.minusSeconds(5 * 60L), now.plusSeconds(35 * 60L), "demo-track-1023");
    }

    private void ensureOrderOtpCodes() {
        List<FoodOrder> updates = orders.findAll().stream()
                .filter(FoodOrder::ensureOtpCodes)
                .toList();
        if (!updates.isEmpty()) orders.saveAll(updates);
    }

    private void seedDemoOrder(String id, String customerName, String customerPhone, String sellerId,
                               String sellerName, String sellerAddress, String customerAddress,
                               String customerPinCode, double customerLatitude, double customerLongitude, double sellerLatitude,
                               double sellerLongitude, double amount, String payment,
                               List<FoodOrderItem> items, Instant orderedAt, Instant expectedDeliveryAt,
                               String trackingToken) {
        FoodOrder existing = orders.findById(id).orElse(null);
        if (existing != null) {
            existing.updateDemoLocation(sellerName, sellerAddress, customerAddress, customerPinCode,
                    customerLatitude, customerLongitude, sellerLatitude, sellerLongitude);
            orders.save(existing);
            return;
        }
        orders.save(FoodOrder.readyOrder(id, customerName, customerPhone, sellerId, sellerName,
                sellerAddress, customerAddress, customerPinCode, customerLatitude, customerLongitude, sellerLatitude,
                sellerLongitude, amount, payment, items, orderedAt, expectedDeliveryAt,
                trackingToken, true));
    }
}
