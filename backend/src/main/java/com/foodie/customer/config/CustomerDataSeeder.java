package com.foodie.customer.config;

import com.foodie.customer.model.MenuItem;
import com.foodie.customer.model.Restaurant;

import com.foodie.customer.repo.Repos.MenuItemRepo;
import com.foodie.customer.repo.Repos.RestaurantRepo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class CustomerDataSeeder implements CommandLineRunner {
    private final RestaurantRepo restaurants;
    private final MenuItemRepo menu;
    

    public CustomerDataSeeder(RestaurantRepo restaurants, MenuItemRepo menu) {
    this.restaurants = restaurants;
    this.menu = menu;
}

    @Override
    public void run(String... args) {
        if (restaurants.count() > 0) return;

        Restaurant biryani = r("Spice Route Biryani", "Biryani, North Indian, Mughlai", "🍛", 4.4, 30, 450, false, "MVP Colony", 17.7425, 83.3317, "20% OFF UPTO ₹50");
        i(biryani, "Biryani", "Chicken Dum Biryani", "Long-grain basmati slow cooked with marinated chicken and whole spices.", 289, false, true, "🍗");
        i(biryani, "Biryani", "Mutton Dum Biryani", "Tender mutton pieces layered with saffron rice.", 389, false, true, "🍖");
        i(biryani, "Biryani", "Paneer Tikka Biryani", "Charred paneer tikka layered with fragrant rice.", 259, true, false, "🧀");
        i(biryani, "Biryani", "Egg Biryani", "Masala eggs over aromatic dum rice.", 219, false, false, "🥚");
        i(biryani, "Starters", "Chicken 65", "Crisp fried chicken tossed with curry leaves and chillies.", 229, false, true, "🍗");
        i(biryani, "Starters", "Veg Manchurian", "Crispy vegetable dumplings in a spicy sauce.", 179, true, false, "🥦");
        i(biryani, "Breads & Sides", "Butter Naan", "Soft tandoor-baked naan brushed with butter.", 45, true, false, "🫓");
        i(biryani, "Desserts", "Double Ka Meetha", "Hyderabadi bread pudding in saffron milk.", 99, true, false, "🍮");

        Restaurant tiffins = r("Vizag Tiffins", "South Indian, Breakfast, Tiffins", "🥞", 4.5, 22, 200, true, "Dwaraka Nagar", 17.7292, 83.3081, "ITEMS AT ₹59");
        i(tiffins, "Dosas", "Masala Dosa", "Crisp dosa filled with spiced potato masala.", 89, true, true, "🥞");
        i(tiffins, "Dosas", "Ghee Podi Dosa", "Dosa roasted in ghee and dusted with gunpowder.", 99, true, true, "🥞");
        i(tiffins, "Dosas", "Onion Rava Dosa", "Lacy semolina dosa with onions and green chillies.", 95, true, false, "🧅");
        i(tiffins, "Tiffins", "Idli (3 pcs)", "Steamed rice cakes with sambar and chutneys.", 59, true, true, "⚪");
        i(tiffins, "Tiffins", "Medu Vada (2 pcs)", "Crisp lentil doughnuts served with sambar.", 69, true, false, "🍩");
        i(tiffins, "Tiffins", "Pongal", "Ghee-rich rice and moong dal with pepper and cashews.", 79, true, false, "🍚");
        i(tiffins, "Beverages", "Filter Coffee", "Strong South Indian decoction coffee.", 39, true, false, "☕");

        Restaurant pizza = r("Pizza Piazza", "Pizza, Italian, Pasta", "🍕", 4.2, 35, 500, false, "Siripuram", 17.7227, 83.3159, "₹100 OFF ABOVE ₹499");
        i(pizza, "Pizzas", "Margherita", "Tomato, mozzarella and fresh basil.", 249, true, true, "🍕");
        i(pizza, "Pizzas", "Farmhouse", "Capsicum, onion, mushroom and sweet corn.", 349, true, false, "🍕");
        i(pizza, "Pizzas", "Pepperoni Feast", "Loaded with pepperoni and double cheese.", 429, false, true, "🍕");
        i(pizza, "Pizzas", "BBQ Chicken", "Smoky BBQ chicken with red onion.", 399, false, false, "🍗");
        i(pizza, "Pasta", "Penne Arrabbiata", "Penne in a spicy tomato and garlic sauce.", 229, true, false, "🍝");
        i(pizza, "Pasta", "Chicken Alfredo", "Creamy white sauce pasta with grilled chicken.", 289, false, false, "🍝");
        i(pizza, "Sides", "Garlic Bread with Cheese", "Toasted garlic bread topped with melted cheese.", 139, true, true, "🥖");
        i(pizza, "Sides", "Choco Lava Cake", "Warm chocolate cake with a molten centre.", 99, true, false, "🍫");

        Restaurant wok = r("Dragon Wok", "Chinese, Asian, Thai", "🥡", 4.1, 32, 400, false, "Asilmetta", 17.7186, 83.3015, "FLAT ₹50 OFF");
        i(wok, "Noodles & Rice", "Hakka Noodles (Veg)", "Wok-tossed noodles with crunchy vegetables.", 169, true, true, "🍜");
        i(wok, "Noodles & Rice", "Chicken Fried Rice", "Egg and chicken fried rice with spring onion.", 199, false, true, "🍚");
        i(wok, "Noodles & Rice", "Schezwan Noodles", "Fiery noodles in house Schezwan sauce.", 189, true, false, "🌶️");
        i(wok, "Starters", "Chilli Chicken", "Crispy chicken in a soy and green chilli glaze.", 239, false, true, "🍗");
        i(wok, "Starters", "Crispy Corn", "Golden corn kernels tossed with pepper and spring onion.", 159, true, false, "🌽");
        i(wok, "Soups", "Hot & Sour Soup", "Peppery soup with vegetables and tofu.", 119, true, false, "🍲");
        i(wok, "Soups", "Chicken Manchow Soup", "Thick soup with crispy noodles on top.", 139, false, false, "🍲");

        Restaurant green = r("Green Bowl", "Healthy, Salads, Pure Veg", "🥗", 4.6, 28, 350, true, "Beach Road", 17.7126, 83.3203, "FLAT ₹75 OFF");
        i(green, "Bowls", "Quinoa Power Bowl", "Quinoa, roasted veggies, chickpeas and tahini.", 249, true, true, "🥗");
        i(green, "Bowls", "Paneer Burrito Bowl", "Spiced paneer, beans, rice and salsa.", 259, true, false, "🌯");
        i(green, "Salads", "Greek Salad", "Cucumber, olives, feta and oregano dressing.", 199, true, false, "🥒");
        i(green, "Salads", "Sprouts Chaat Salad", "Crunchy sprouts with lemon and chaat masala.", 149, true, false, "🌱");
        i(green, "Juices", "Fresh Orange Juice", "Cold-pressed, no added sugar.", 99, true, false, "🍊");
        i(green, "Juices", "Green Detox Smoothie", "Spinach, apple, cucumber and mint.", 129, true, true, "🥤");

        Restaurant burger = r("Burger Barn", "Burgers, Fast Food, Fries", "🍔", 4.0, 25, 300, false, "Maharani Peta", 17.7203, 83.2998, "ITEMS AT ₹99");
        i(burger, "Burgers", "Classic Veg Burger", "Crispy veg patty, lettuce and house sauce.", 119, true, true, "🍔");
        i(burger, "Burgers", "Crispy Chicken Burger", "Buttermilk fried chicken with mayo.", 159, false, true, "🍔");
        i(burger, "Burgers", "Double Cheese Smash", "Two smashed patties with melted cheese.", 219, false, false, "🧀");
        i(burger, "Sides", "Peri Peri Fries", "Skin-on fries tossed in peri peri seasoning.", 99, true, false, "🍟");
        i(burger, "Sides", "Chicken Nuggets (6 pcs)", "Golden nuggets with dip.", 129, false, false, "🍗");
        i(burger, "Beverages", "Cold Coffee", "Thick, creamy and chilled.", 89, true, false, "🥤");

        Restaurant bakery = r("Sweet Tooth Bakery", "Desserts, Bakery, Ice Cream", "🍰", 4.7, 20, 250, true, "Jagadamba Junction", 17.7118, 83.3011, "BUY 1 GET 1 ON SHAKES");
        i(bakery, "Cakes", "Chocolate Truffle Pastry", "Rich chocolate ganache layered sponge.", 89, true, true, "🍰");
        i(bakery, "Cakes", "Red Velvet Jar Cake", "Cream-cheese frosting with red velvet crumbs.", 129, true, false, "🍰");
        i(bakery, "Cakes", "Black Forest Cake (500g)", "Classic cherries, cream and chocolate shavings.", 449, true, false, "🎂");
        i(bakery, "Ice Cream & Shakes", "Oreo Shake", "Thick shake with cookie crumbs.", 139, true, true, "🥤");
        i(bakery, "Ice Cream & Shakes", "Butterscotch Sundae", "Vanilla scoops with butterscotch and nuts.", 119, true, false, "🍨");
        i(bakery, "Bakes", "Almond Croissant", "Flaky butter croissant with almond filling.", 109, true, false, "🥐");

        Restaurant kebab = r("Kebab Junction", "Kebabs, North Indian, Tandoor", "🍢", 4.3, 38, 550, false, "Daba Gardens", 17.7266, 83.3028, "20% OFF UPTO ₹100");
        i(kebab, "Kebabs", "Chicken Seekh Kebab", "Minced chicken kebabs grilled on skewers.", 249, false, true, "🍢");
        i(kebab, "Kebabs", "Paneer Malai Tikka", "Creamy marinated paneer from the tandoor.", 239, true, true, "🧀");
        i(kebab, "Kebabs", "Tandoori Chicken (Half)", "Yoghurt-marinated chicken with smoky char.", 289, false, false, "🍗");
        i(kebab, "Mains", "Butter Chicken", "Tomato and cream gravy with tandoori chicken.", 329, false, true, "🍛");
        i(kebab, "Mains", "Dal Makhani", "Slow-simmered black lentils finished with butter.", 229, true, false, "🍲");
        i(kebab, "Breads & Sides", "Garlic Naan", "Tandoor naan with garlic and coriander.", 59, true, false, "🫓");
        i(kebab, "Breads & Sides", "Jeera Rice", "Basmati rice tempered with cumin.", 129, true, false, "🍚");

        Restaurant coastal = r("Coastal Curry House", "Seafood, Andhra, Meals", "🐟", 4.5, 40, 600, false, "Rushikonda", 17.7822, 83.3844, "FREE DELIVERY");
        i(coastal, "Seafood", "Apollo Fish Fry", "Boneless fish tossed with curry leaves and spices.", 299, false, true, "🐟");
        i(coastal, "Seafood", "Prawn Pulao", "Fragrant rice cooked with spiced prawns.", 329, false, true, "🦐");
        i(coastal, "Seafood", "Pomfret Fry", "Whole pomfret marinated in coastal masala.", 389, false, false, "🐟");
        i(coastal, "Meals", "Andhra Veg Meals", "Rice, sambar, rasam, curries, pickle and curd.", 189, true, true, "🍱");
        i(coastal, "Meals", "Andhra Chicken Meals", "Spicy Andhra chicken curry with a full thali.", 259, false, false, "🍱");
        i(coastal, "Sides", "Gongura Pachadi", "Tangy sorrel leaf chutney.", 49, true, false, "🌿");

        Restaurant chai = r("Chai & Chaat Corner", "Street Food, Beverages, Snacks", "🍵", 4.2, 18, 150, true, "Seethammadhara", 17.7432, 83.3120, "ITEMS AT ₹49");
        i(chai, "Chaat", "Pani Puri (6 pcs)", "Crisp puris with spicy mint water.", 49, true, true, "🥟");
        i(chai, "Chaat", "Samosa Chaat", "Crushed samosa with chutneys, curd and sev.", 69, true, false, "🥟");
        i(chai, "Chaat", "Pav Bhaji", "Buttery mashed vegetable curry with toasted pav.", 119, true, true, "🍞");
        i(chai, "Beverages", "Masala Chai", "Ginger and cardamom milk tea.", 25, true, true, "🍵");
        i(chai, "Beverages", "Mango Lassi", "Chilled yoghurt drink with Alphonso mango.", 79, true, false, "🥭");

       
    }

    private Restaurant r(String name, String cuisines, String emoji, double rating, int mins, int cost, boolean pureVeg,
                         String area, double lat, double lon, String offer) {
        Restaurant x = new Restaurant();
        x.setName(name);
        x.setCuisines(cuisines);
        x.setEmoji(emoji);
        x.setRating(rating);
        x.setDeliveryMinutes(mins);
        x.setCostForTwo(cost);
        x.setPureVeg(pureVeg);
        x.setArea(area);
        x.setLat(lat);
        x.setLon(lon);
        x.setOffer(offer);
        return restaurants.save(x);
    }

    private void i(Restaurant r, String category, String name, String description, int price, boolean veg,
                   boolean bestseller, String emoji) {
        MenuItem m = new MenuItem();
        m.setRestaurantId(r.getId());
        m.setCategory(category);
        m.setName(name);
        m.setDescription(description);
        m.setPrice(price);
        m.setVeg(veg);
        m.setBestseller(bestseller);
        m.setEmoji(emoji);
        menu.save(m);
    }

    
}
