package com.jgm.mybudgetapp.utils;

import com.jgm.mybudgetapp.R;
import com.jgm.mybudgetapp.objects.Icon;

import java.util.ArrayList;

public class IconUtils {

    public IconUtils() {}

    private static final Icon ic_default = new Icon(0, R.drawable.ic_category_fill, "Category");

    private static final Icon ic_cash = new Icon(1, R.drawable.ic_cash_payments_fill, "Cash");
    private static final Icon ic_wallet = new Icon(2, R.drawable.ic_wallet_fill, "Wallet");
    private static final Icon ic_credit_card = new Icon(3, R.drawable.ic_credit_card, "Credit Card");
    private static final Icon ic_savings = new Icon(4, R.drawable.ic_savings_fill, "Savings");
    private static final Icon ic_trending_up = new Icon(5, R.drawable.ic_trending_up_fill, "Trending up");

    private static final Icon ic_home = new Icon(6, R.drawable.ic_home_fill, "Home");
    private static final Icon ic_construction = new Icon(7, R.drawable.ic_construction_fill, "Construction");
    private static final Icon ic_cleaning = new Icon(8, R.drawable.ic_cleaning_services_fill, "Cleaning services");
    private static final Icon ic_shopping_cart = new Icon(9, R.drawable.ic_shopping_cart_fill, "Shopping Cart");
    private static final Icon ic_pharmacy = new Icon(10, R.drawable.ic_local_pharmacy_fill, "Pharmacy");

    private static final Icon ic_work = new Icon(11, R.drawable.ic_business_center_fill, "Work");
    private static final Icon ic_gavel = new Icon(12, R.drawable.ic_gavel_fill, "Gavel");
    private static final Icon ic_bank = new Icon(13, R.drawable.ic_app_account_balance_400, "Bank");
    private static final Icon ic_library = new Icon(14, R.drawable.ic_local_library_fill, "Library");
    private static final Icon ic_school = new Icon(15, R.drawable.ic_school_fill, "School");

    private static final Icon ic_devices = new Icon(16, R.drawable.ic_devices_fill, "Devices");
    private static final Icon ic_checkroom = new Icon(17, R.drawable.ic_checkroom_fill, "Checkroom");
    private static final Icon ic_shopping_bag = new Icon(18, R.drawable.ic_shopping_bag_fill, "Shopping bag");
    private static final Icon ic_heart = new Icon(19, R.drawable.ic_favorite_fill, "Heart");
    private static final Icon ic_celebration = new Icon(20, R.drawable.ic_celebration_fill, "Celebration");

    private static final Icon ic_pet = new Icon(21, R.drawable.ic_pets_fill, "Pet");
    private static final Icon ic_child = new Icon(22, R.drawable.ic_child_care_fill, "Child");
    private static final Icon ic_toy = new Icon(23, R.drawable.ic_toys_fill, "Toy");
    private static final Icon ic_videogame = new Icon(24, R.drawable.ic_stadia_controller_fill, "Videogame");
    private static final Icon ic_attractions = new Icon(25, R.drawable.ic_attractions_fill, "Attractions");

    private static final Icon ic_car = new Icon(26, R.drawable.ic_directions_car_fill, "Car");
    private static final Icon ic_motor_sports = new Icon(27, R.drawable.ic_sports_motorsports_fill, "Motor bike");
    private static final Icon ic_pedal_bike = new Icon(28, R.drawable.ic_pedal_bike_fill, "Pedal bike");
    private static final Icon ic_commute = new Icon(29, R.drawable.ic_commute_fill, "Commute");
    private static final Icon ic_gas_station = new Icon(30, R.drawable.ic_local_gas_station_fill, "Gas station");

    private static final Icon ic_gym = new Icon(31, R.drawable.ic_fitness_center_fill, "Gym");
    private static final Icon ic_stethoscope = new Icon(32, R.drawable.ic_stethoscope_fill, "Stethoscope");
    private static final Icon ic_dentistry = new Icon(33, R.drawable.ic_dentistry_fill, "Dentistry");
    private static final Icon ic_ecg_heart = new Icon(34, R.drawable.ic_ecg_heart_fill, "Ecg heart");
    private static final Icon ic_eyeglasses = new Icon(35, R.drawable.ic_eyeglasses_fill, "Eyeglasses");

    private static final Icon ic_restaurant = new Icon(36, R.drawable.ic_restaurant_fill, "Restaurant");
    private static final Icon ic_bakery = new Icon(37, R.drawable.ic_bakery_dining_fill, "Bakery");
    private static final Icon ic_takeout = new Icon(38, R.drawable.ic_takeout_dining_fill, "Takeout");
    private static final Icon ic_fruit = new Icon(39, R.drawable.ic_nutrition_fill, "Fruit");
    private static final Icon ic_pizza = new Icon(40, R.drawable.ic_local_pizza_fill, "Pizza");

    private static final Icon ic_hamburger = new Icon(41, R.drawable.ic_lunch_dining_fill, "Hamburger");
    private static final Icon ic_beer = new Icon(42, R.drawable.ic_sports_bar_fill, "Beer");
    private static final Icon ic_sports = new Icon(43, R.drawable.ic_sports_fill, "Sports");
    private static final Icon ic_local_activity = new Icon(44, R.drawable.ic_local_activity_fill, "Local activity");
    private static final Icon ic_wine = new Icon(45, R.drawable.ic_wine_bar_fill, "Wine");

    private static final Icon ic_nightlife = new Icon(46, R.drawable.ic_nightlife_fill, "Nightlife");
    private static final Icon ic_theaters = new Icon(47, R.drawable.ic_theaters_fill, "Theaters");
    private static final Icon ic_music = new Icon(48, R.drawable.ic_music_note_fill, "Music");
    private static final Icon ic_casino = new Icon(49, R.drawable.ic_casino_fill, "Casino");
    private static final Icon ic_smoking = new Icon(50, R.drawable.ic_smoking_rooms_fill, "Smoking");

    private static final Icon ic_airplane = new Icon(51, R.drawable.ic_flight_fill, "Airplane");
    private static final Icon ic_luggage = new Icon(52, R.drawable.ic_luggage_fill, "Luggage");
    private static final Icon ic_map = new Icon(53, R.drawable.ic_map_fill, "Map");
    private static final Icon ic_camera = new Icon(54, R.drawable.ic_photo_camera_fill, "Camera");
    private static final Icon ic_spa = new Icon(55, R.drawable.ic_spa_fill, "Spa");

    private static final Icon ic_florist = new Icon(56, R.drawable.ic_local_florist_fill, "Florist");
    private static final Icon ic_grass = new Icon(57, R.drawable.ic_grass_fill, "Grass");
    private static final Icon ic_plant = new Icon(58, R.drawable.ic_potted_plant_fill, "Potted plant");
    private static final Icon ic_grill = new Icon(59, R.drawable.ic_outdoor_grill_fill, "Outdoor grill");
    private static final Icon ic_pool = new Icon(60, R.drawable.ic_pool_fill, "Swimming pool");

    private static final Icon ic_beach = new Icon(61, R.drawable.ic_beach_access_fill, "Beach");
    private static final Icon ic_park= new Icon(62, R.drawable.ic_park_fill, "Park");
    private static final Icon ic_cabin = new Icon(63, R.drawable.ic_cabin_fill, "Cabin");
    private static final Icon ic_camping = new Icon(64, R.drawable.ic_camping_fill, "Camping");
    private static final Icon ic_phishing = new Icon(65, R.drawable.ic_phishing_fill, "Phishing");
    private static final Icon ic_emergency = new Icon(66, R.drawable.ic_emergency, "Emergency");

    // Accounts
    private static final Icon ic_app_cash = new Icon(67, R.drawable.ic_app_cash, "Cash");
    private static final Icon ic_app_checking = new Icon(68, R.drawable.ic_app_account_balance, "Checking");
    private static final Icon ic_app_savings = new Icon(69, R.drawable.ic_app_savings, "Savings");
    private static final Icon ic_app_credit_card = new Icon(70, R.drawable.ic_app_credit_card, "Credit Card");

    private static final Icon ic_app_accumulated = new Icon(71, R.drawable.ic_app_autorenew, "Accumulated");

    public static ArrayList<Icon> getIconList() {
        ArrayList<Icon> icons = new ArrayList<>();

        icons.add(ic_cash);
        icons.add(ic_wallet);
        icons.add(ic_credit_card);
        icons.add(ic_savings);
        icons.add(ic_trending_up);

        icons.add(ic_home);
        icons.add(ic_construction);
        icons.add(ic_cleaning);
        icons.add(ic_shopping_cart);
        icons.add(ic_pharmacy);

        icons.add(ic_work);
        icons.add(ic_gavel);
        icons.add(ic_bank);
        icons.add(ic_library);
        icons.add(ic_school);

        icons.add(ic_devices);
        icons.add(ic_checkroom);
        icons.add(ic_shopping_bag);
        icons.add(ic_heart);
        icons.add(ic_celebration);

        icons.add(ic_pet);
        icons.add(ic_child);
        icons.add(ic_toy);
        icons.add(ic_videogame);
        icons.add(ic_attractions);

        icons.add(ic_car);
        icons.add(ic_motor_sports);
        icons.add(ic_pedal_bike);
        icons.add(ic_commute);
        icons.add(ic_gas_station);

        icons.add(ic_gym);
        icons.add(ic_stethoscope);
        icons.add(ic_dentistry);
        icons.add(ic_ecg_heart);
        icons.add(ic_eyeglasses);

        icons.add(ic_restaurant);
        icons.add(ic_bakery);
        icons.add(ic_takeout);
        icons.add(ic_fruit);
        icons.add(ic_pizza);

        icons.add(ic_hamburger);
        icons.add(ic_beer);
        icons.add(ic_sports);
        icons.add(ic_local_activity);
        icons.add(ic_wine);

        icons.add(ic_nightlife);
        icons.add(ic_theaters);
        icons.add(ic_music);
        icons.add(ic_casino);
        icons.add(ic_smoking);

        icons.add(ic_airplane);
        icons.add(ic_luggage);
        icons.add(ic_map);
        icons.add(ic_camera);
        icons.add(ic_spa);

        icons.add(ic_florist);
        icons.add(ic_grass);
        icons.add(ic_plant);
        icons.add(ic_grill);
        icons.add(ic_pool);

        icons.add(ic_beach);
        icons.add(ic_park);
        icons.add(ic_cabin);
        icons.add(ic_camping);
        icons.add(ic_phishing);

        icons.add(ic_emergency);
        icons.add(ic_default);

        return icons;
    }

    public static Icon getIcon(int id) {
        Icon icon;

        switch (id) {
            case 1: icon = ic_cash; break;
            case 2: icon = ic_wallet; break;
            case 3: icon = ic_credit_card; break;
            case 4: icon = ic_savings; break;
            case 5: icon = ic_trending_up; break;
            case 6: icon = ic_home; break;
            case 7: icon = ic_construction; break;
            case 8: icon = ic_cleaning; break;
            case 9: icon = ic_shopping_cart; break;
            case 10: icon = ic_pharmacy; break;
            case 11: icon = ic_work; break;
            case 12: icon = ic_gavel; break;
            case 13: icon = ic_bank; break;
            case 14: icon = ic_library; break;
            case 15: icon = ic_school; break;
            case 16: icon = ic_devices; break;
            case 17: icon = ic_checkroom; break;
            case 18: icon = ic_shopping_bag; break;
            case 19: icon = ic_heart; break;
            case 20: icon = ic_celebration; break;
            case 21: icon = ic_pet; break;
            case 22: icon = ic_child; break;
            case 23: icon = ic_toy; break;
            case 24: icon = ic_videogame; break;
            case 25: icon = ic_attractions; break;
            case 26: icon = ic_car; break;
            case 27: icon = ic_motor_sports; break;
            case 28: icon = ic_pedal_bike; break;
            case 29: icon = ic_commute; break;
            case 30: icon = ic_gas_station; break;
            case 31: icon = ic_gym; break;
            case 32: icon = ic_stethoscope; break;
            case 33: icon = ic_dentistry; break;
            case 34: icon = ic_ecg_heart; break;
            case 35: icon = ic_eyeglasses; break;
            case 36: icon = ic_restaurant; break;
            case 37: icon = ic_bakery; break;
            case 38: icon = ic_takeout; break;
            case 39: icon = ic_fruit; break;
            case 40: icon = ic_pizza; break;
            case 41: icon = ic_hamburger; break;
            case 42: icon = ic_beer; break;
            case 43: icon = ic_sports; break;
            case 44: icon = ic_local_activity; break;
            case 45: icon = ic_wine; break;
            case 46: icon = ic_nightlife; break;
            case 47: icon = ic_theaters; break;
            case 48: icon = ic_music; break;
            case 49: icon = ic_casino; break;
            case 50: icon = ic_smoking; break;
            case 51: icon = ic_airplane; break;
            case 52: icon = ic_luggage; break;
            case 53: icon = ic_map; break;
            case 54: icon = ic_camera; break;
            case 55: icon = ic_spa; break;
            case 56: icon = ic_florist; break;
            case 57: icon = ic_grass; break;
            case 58: icon = ic_plant; break;
            case 59: icon = ic_grill; break;
            case 60: icon = ic_pool; break;
            case 61: icon = ic_beach; break;
            case 62: icon = ic_park; break;
            case 63: icon = ic_cabin; break;
            case 64: icon = ic_camping; break;
            case 65: icon = ic_phishing; break;
            case 66: icon = ic_emergency; break;
            case 67: icon = ic_app_cash; break;
            case 68: icon = ic_app_checking; break;
            case 69: icon = ic_app_savings; break;
            case 70: icon = ic_app_credit_card; break;
            case 71: icon = ic_app_accumulated; break;
            default: icon = ic_default;
        }

        return icon;
    }

}
