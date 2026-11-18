package com.foodie.admin.data;

import com.foodie.admin.model.Customer;
import com.foodie.admin.model.MenuItem;
import com.foodie.admin.model.Order;
import com.foodie.admin.model.Rider;
import com.foodie.admin.model.RiderDocument;
import com.foodie.admin.model.Seller;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory dummy data for the admin dashboard.
 * Swap this for Spring Data JPA repositories when you connect a real database.
 */
@Component
public class DummyData {

    private static final String PORTRAIT = "https://randomuser.me/api/portraits/";

    public final Map<String, Customer> customers = new LinkedHashMap<>();
    public final Map<String, Seller> sellers = new LinkedHashMap<>();
    public final Map<String, Rider> riders = new LinkedHashMap<>();
    public final List<Order> orders = new ArrayList<>();

    public DummyData() {
        seed();
    }

    private static String food(int pexelsId) {
        return "https://images.pexels.com/photos/" + pexelsId + "/pexels-photo-" + pexelsId
                + ".jpeg?auto=compress&cs=tinysrgb&w=600";
    }

    private static String face(String gender, int n) {
        return PORTRAIT + gender + "/" + n + ".jpg";
    }

    private void seed() {
        // ------------------------------------------------------------------ menus
        List<List<MenuItem>> menus = List.of(
                List.of(
                        new MenuItem("Chicken Dum Biryani", "Main Course", 289, false, food(1640777)),
                        new MenuItem("Paneer Tikka Biryani", "Main Course", 249, true, food(1410235)),
                        new MenuItem("Mirchi Ka Salan", "Sides", 99, true, food(1099680)),
                        new MenuItem("Double Ka Meetha", "Dessert", 89, true, food(376464))),
                List.of(
                        new MenuItem("Classic Smash Burger", "Burgers", 199, false, food(70497)),
                        new MenuItem("Crispy Veggie Burger", "Burgers", 169, true, food(1639557)),
                        new MenuItem("Peri Peri Fries", "Sides", 119, true, food(1583884)),
                        new MenuItem("Chocolate Shake", "Beverages", 149, true, food(103566))),
                List.of(
                        new MenuItem("Margherita Pizza", "Pizza", 249, true, food(315755)),
                        new MenuItem("Pepperoni Feast", "Pizza", 399, false, food(1146760)),
                        new MenuItem("Garlic Bread", "Sides", 129, true, food(1279330)),
                        new MenuItem("Tiramisu", "Dessert", 179, true, food(291528))),
                List.of(
                        new MenuItem("Masala Dosa", "Tiffin", 99, true, food(5560763)),
                        new MenuItem("Ghee Podi Idli", "Tiffin", 89, true, food(5560763)),
                        new MenuItem("Medu Vada", "Tiffin", 69, true, food(1640777)),
                        new MenuItem("Filter Coffee", "Beverages", 49, true, food(312418))),
                List.of(
                        new MenuItem("Butter Chicken", "Main Course", 329, false, food(1099680)),
                        new MenuItem("Garlic Naan", "Breads", 59, true, food(1117862)),
                        new MenuItem("Tandoori Platter", "Starters", 449, false, food(1410235)),
                        new MenuItem("Dal Makhani", "Main Course", 219, true, food(1640777))),
                List.of(
                        new MenuItem("Red Velvet Cake", "Cakes", 599, true, food(291528)),
                        new MenuItem("Chocolate Brownie", "Dessert", 129, true, food(376464)),
                        new MenuItem("Blueberry Muffin", "Bakery", 99, true, food(205961)),
                        new MenuItem("Cheesecake Slice", "Dessert", 179, true, food(1126359))));

        // ------------------------------------------------------------------ people
        String[][] c = {
                // name, email, phone, avatar, area, address, joined, status, lifetime orders before the seeded ones
                {"Aarav Sharma", "aarav.sharma@gmail.com", "+91 98480 12345", face("men", 32), "MVP Colony", "Flat 402, Sai Residency, MVP Colony", "14 Mar 2025", "ACTIVE", "38"},
                {"Priya Reddy", "priya.reddy@gmail.com", "+91 98765 43210", face("women", 44), "Dwaraka Nagar", "12-4-18, Dwaraka Nagar Main Road", "02 Jun 2025", "ACTIVE", "52"},
                {"Rohit Verma", "rohit.verma@outlook.com", "+91 99890 11223", face("men", 45), "Siripuram", "Door 7-1-3, Siripuram Junction", "21 Aug 2025", "BLOCKED", "9"},
                {"Ananya Iyer", "ananya.iyer@gmail.com", "+91 90300 55671", face("women", 68), "Madhurawada", "Plot 88, Sri Sai Nagar, Madhurawada", "09 Jan 2026", "ACTIVE", "27"},
                {"Karthik Naidu", "karthik.naidu@yahoo.com", "+91 97012 88340", face("men", 76), "Gajuwaka", "5-22/B, Gajuwaka Main Road", "30 Jan 2026", "ACTIVE", "15"},
                {"Sneha Patel", "sneha.patel@gmail.com", "+91 98499 76120", face("women", 12), "Beach Road", "Apt 12C, Sea Breeze Towers, Beach Road", "17 Mar 2026", "ACTIVE", "44"},
                {"Vikram Singh", "vikram.singh@gmail.com", "+91 94400 23098", face("men", 18), "Seethammadhara", "21-5-9, Seethammadhara North Extn", "05 May 2026", "ACTIVE", "6"},
                {"Meera Nair", "meera.nair@gmail.com", "+91 88850 41276", face("women", 90), "Rushikonda", "Villa 14, Hill View Enclave, Rushikonda", "28 Jul 2026", "ACTIVE", "11"}
        };

        String[][] s = {
                // name, owner, ownerAvatar, cover, cuisine, phone, email, address, fssai, rating, status, open, joined, lifetime orders
                {"Spice Route Biryani House", "Mohammed Rafi", face("men", 22), food(262978), "Hyderabadi • North Indian", "+91 98850 22114", "rafi@spiceroute.in", "Shop 3, Jagadamba Centre, Jagadamba Junction", "10124017000231", "4.6", "ACTIVE", "true", "11 Jan 2025", "410"},
                {"The Burger Barn", "Neha Gupta", face("women", 33), food(70497), "Burgers • Fast Food", "+91 99121 70830", "hello@burgerbarn.in", "Ground Floor, Vizag Central Mall, Dwaraka Nagar", "10124017000582", "4.4", "ACTIVE", "true", "03 Apr 2025", "365"},
                {"Pizza Napoli", "Giovanni D'Souza", face("men", 64), food(315755), "Italian • Pizza", "+91 98490 66321", "orders@pizzanapoli.in", "Beach Road, Opp. Kali Mata Temple", "10124017000947", "4.7", "ACTIVE", "false", "19 Jun 2025", "298"},
                {"Dosa Corner", "Lakshmi Narayana", face("men", 40), food(5560763), "South Indian • Tiffins", "+91 94910 55208", "dosacorner@gmail.com", "Siripuram Circle, Near Hindu Public School", "10124017001133", "4.8", "ACTIVE", "true", "25 Sep 2025", "521"},
                {"Tandoori Tales", "Harpreet Kaur", face("women", 55), food(1099680), "North Indian • Tandoor", "+91 99080 14477", "contact@tandooritales.in", "Plot 21, Madhurawada Main Road", "10124017001420", "4.3", "ACTIVE", "true", "08 Dec 2025", "187"},
                {"Sweet Tooth Bakery", "Divya Menon", face("women", 17), food(291528), "Bakery • Desserts", "+91 90140 33812", "divya@sweettooth.in", "MVP Colony, Sector 9 Market", "10124017001755", "0.0", "PENDING", "false", "12 Nov 2026", "0"}
        };

        String[][] r = {
                // name, avatar, phone, vehicle, vehicle no, licence, area, status, rating, joined, lifetime deliveries
                {"Suresh Kumar", face("men", 52), "+91 98481 90021", "Bike", "AP 31 BX 4821", "AP3120210045821", "MVP Colony", "ONLINE", "4.8", "18 Feb 2025", "612"},
                {"Imran Khan", face("men", 61), "+91 99630 41872", "Bike", "AP 31 CZ 1190", "AP3120190031190", "Dwaraka Nagar", "ON_DELIVERY", "4.6", "07 May 2025", "488"},
                {"Lakshmi Devi", face("women", 26), "+91 90320 18455", "Scooter", "AP 39 AB 7745", "AP3920220017745", "Siripuram", "OFFLINE", "4.9", "22 Jul 2025", "354"},
                {"Ravi Teja", face("men", 85), "+91 97039 62210", "Bike", "AP 31 DK 3308", "AP3120200023308", "Gajuwaka", "ONLINE", "4.5", "14 Oct 2025", "271"},
                {"Mohan Rao", face("men", 29), "+91 88860 70913", "Bike", "AP 31 EH 9021", "AP3120230049021", "Madhurawada", "PENDING", "0.0", "13 Nov 2026", "0"},
                {"Deepak Yadav", face("men", 71), "+91 94930 28806", "Bike", "AP 31 FQ 5567", "AP3120180025567", "Beach Road", "SUSPENDED", "3.1", "03 Mar 2025", "143"}
        };

        // ------------------------------------------------------------------ orders
        String[] statusPattern = {"DELIVERED", "DELIVERED", "ON_THE_WAY", "DELIVERED", "PREPARING",
                "DELIVERED", "CANCELLED", "DELIVERED", "AWAITING_RIDER"};
        String[] payments = {"UPI", "CARD", "CASH ON DELIVERY"};
        int[] activeRiders = {0, 1, 2, 3};

        for (int i = 0; i < 40; i++) {
            int ci = (i * 3 + 1) % c.length;
            int si = (i * 3 + i / 5) % 5;                 // sellers 0-4 take orders, #5 is still pending approval
            String status = statusPattern[i % statusPattern.length];
            boolean hasRider = status.equals("DELIVERED") || status.equals("ON_THE_WAY");
            int ri = (i % 9 == 0) ? 5 : activeRiders[(i * 7 + 2) % activeRiders.length];

            List<MenuItem> menu = menus.get(si);
            MenuItem first = menu.get(i % 4);
            MenuItem second = menu.get((i + 1) % 4);
            int qty = (i % 3) + 1;
            double amount = first.price() * qty + second.price() + 40;   // + ₹40 delivery fee
            String items = qty + "× " + first.name() + ", 1× " + second.name();

            int day = 16 - (i / 4 + 1);
            int hour = 11 + (i * 7) % 11;
            int minute = (i * 13) % 60;
            String placedAt = String.format("%02d Nov 2026, %02d:%02d", day, hour, minute);

            orders.add(new Order(
                    "ORD-" + (4800 + 40 - i),
                    "C" + (ci + 1), c[ci][0],
                    "S" + (si + 1), s[si][0],
                    hasRider ? "R" + (ri + 1) : null,
                    hasRider ? r[ri][0] : null,
                    items, amount, status, payments[i % payments.length], placedAt));
        }

        // ------------------------------------------------------------------ customers
        for (int i = 0; i < c.length; i++) {
            String id = "C" + (i + 1);
            int mine = 0;
            double spent = 0;
            for (Order o : orders) {
                if (o.customerId().equals(id)) {
                    mine++;
                    if (o.status().equals("DELIVERED")) spent += o.amount();
                }
            }
            int lifetime = Integer.parseInt(c[i][8]);
            customers.put(id, new Customer(id, c[i][0], c[i][1], c[i][2], c[i][3],
                    "Visakhapatnam", c[i][5] + ", " + c[i][4], c[i][6], c[i][7],
                    mine + lifetime, spent + lifetime * 340.0));
        }

        // ------------------------------------------------------------------ sellers
        for (int i = 0; i < s.length; i++) {
            String id = "S" + (i + 1);
            int mine = 0;
            double revenue = 0;
            for (Order o : orders) {
                if (o.sellerId().equals(id)) {
                    mine++;
                    if (o.status().equals("DELIVERED")) revenue += o.amount();
                }
            }
            int lifetime = Integer.parseInt(s[i][13]);
            sellers.put(id, new Seller(id, s[i][0], s[i][1], s[i][2], s[i][3], s[i][4], s[i][5], s[i][6],
                    s[i][7], "Visakhapatnam", s[i][8], Double.parseDouble(s[i][9]), s[i][10],
                    Boolean.parseBoolean(s[i][11]), s[i][12], mine + lifetime,
                    revenue + lifetime * 310.0, menus.get(i)));
        }

        // ------------------------------------------------------------------ riders
        for (int i = 0; i < r.length; i++) {
            String id = "R" + (i + 1);
            int mine = 0;
            for (Order o : orders) {
                if (id.equals(o.riderId()) && o.status().equals("DELIVERED")) mine++;
            }
            int lifetime = Integer.parseInt(r[i][10]);
            String status = r[i][7];
            List<RiderDocument> docs = status.equals("PENDING")
                    ? List.of(new RiderDocument("Driving Licence", "VERIFIED"),
                              new RiderDocument("Aadhaar Card", "PENDING"),
                              new RiderDocument("Vehicle RC", "PENDING"),
                              new RiderDocument("Selfie Verification", "PENDING"))
                    : List.of(new RiderDocument("Driving Licence", "VERIFIED"),
                              new RiderDocument("Aadhaar Card", "VERIFIED"),
                              new RiderDocument("Vehicle RC", "VERIFIED"),
                              new RiderDocument("Selfie Verification", "VERIFIED"));
            String email = r[i][0].toLowerCase().replace(" ", ".") + "@gmail.com";
            riders.put(id, new Rider(id, r[i][0], r[i][1], r[i][2], email, r[i][3], r[i][4], r[i][5],
                    "Visakhapatnam • " + r[i][6], status, Double.parseDouble(r[i][8]),
                    mine + lifetime, (mine + lifetime) * 52.0, r[i][9], docs));
        }
    }
}
