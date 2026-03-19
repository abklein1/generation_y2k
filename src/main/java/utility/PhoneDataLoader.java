package utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads phone hardware data from phones.json and provides a catalogue of
 * available phone models grouped by price tier for income-based selection.
 */
public class PhoneDataLoader {

    private static final String PHONES_PATH = "src/main/java/Resources/phones.json";

    private static List<PhoneSpec> allPhones = null;
    private static List<PhoneSpec> budgetPhones = null;
    private static List<PhoneSpec> midRangePhones = null;
    private static List<PhoneSpec> premiumPhones = null;

    private static final int MID_RANGE_THRESHOLD = 200;
    private static final int PREMIUM_THRESHOLD = 350;

    /**
     * A single phone model entry loaded from the JSON catalogue.
     */
    public static class PhoneSpec implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String make;
        private final String model;
        private final int price;
        private final String size;
        private final int battery;
        private final boolean keyboard;
        private final boolean camera;
        private final boolean video;
        private final boolean wifi;
        private final boolean bluetooth;
        private final boolean sms;
        private final boolean im;
        private final boolean pda;
        private final boolean mp3;
        private final List<String> colors;

        public PhoneSpec(String make, String model, int price, String size, int battery,
                         boolean keyboard, boolean camera, boolean video, boolean wifi,
                         boolean bluetooth, boolean sms, boolean im, boolean pda,
                         boolean mp3, List<String> colors) {
            this.make = make;
            this.model = model;
            this.price = price;
            this.size = size;
            this.battery = battery;
            this.keyboard = keyboard;
            this.camera = camera;
            this.video = video;
            this.wifi = wifi;
            this.bluetooth = bluetooth;
            this.sms = sms;
            this.im = im;
            this.pda = pda;
            this.mp3 = mp3;
            this.colors = colors;
        }

        public String getMake() {
            return make;
        }

        public String getModel() {
            return model;
        }

        public int getPrice() {
            return price;
        }

        public String getSize() {
            return size;
        }

        public int getBattery() {
            return battery;
        }

        public boolean hasKeyboard() {
            return keyboard;
        }

        public boolean hasCamera() {
            return camera;
        }

        public boolean hasVideo() {
            return video;
        }

        public boolean hasWifi() {
            return wifi;
        }

        public boolean hasBluetooth() {
            return bluetooth;
        }

        public boolean hasSms() {
            return sms;
        }

        public boolean hasIm() {
            return im;
        }

        public boolean hasPda() {
            return pda;
        }

        public boolean hasMp3() {
            return mp3;
        }

        public List<String> getColors() {
            return colors;
        }

        /**
         * Picks a random color from this phone's available colors.
         */
        public String randomColor() {
            return colors.get(GameRandom.nextInt(0, colors.size() - 1));
        }
    }

    /**
     * Ensures the phone catalogue is loaded from disk.
     * Phones from years up to and including the simulation starting year are included.
     */
    public static void ensureLoaded() {
        if (allPhones != null) {
            return;
        }
        allPhones = new ArrayList<>();
        budgetPhones = new ArrayList<>();
        midRangePhones = new ArrayList<>();
        premiumPhones = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(
                    new FileReader(PHONES_PATH, StandardCharsets.UTF_8));

            for (Object yearKey : root.keySet()) {
                JSONObject makesObj = (JSONObject) root.get(yearKey);
                if (makesObj == null || makesObj.isEmpty()) {
                    continue;
                }
                for (Object makeKey : makesObj.keySet()) {
                    String makeName = (String) makeKey;
                    JSONObject modelsObj = (JSONObject) makesObj.get(makeKey);
                    for (Object modelKey : modelsObj.keySet()) {
                        String modelName = (String) modelKey;
                        JSONObject spec = (JSONObject) modelsObj.get(modelKey);
                        PhoneSpec ps = parseSpec(makeName, modelName, spec);
                        allPhones.add(ps);
                        if (ps.price < MID_RANGE_THRESHOLD) {
                            budgetPhones.add(ps);
                        } else if (ps.price < PREMIUM_THRESHOLD) {
                            midRangePhones.add(ps);
                        } else {
                            premiumPhones.add(ps);
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            GameLogger.logDebug("Error loading phone data: " + e.getMessage());
        }

        GameLogger.logGeneration("Loaded " + allPhones.size() + " phone models ("
                + budgetPhones.size() + " budget, "
                + midRangePhones.size() + " mid-range, "
                + premiumPhones.size() + " premium)");
    }

    private static PhoneSpec parseSpec(String make, String model, JSONObject spec) {
        int price = ((Number) spec.get("price")).intValue();
        String size = (String) spec.get("size");
        int battery = ((Number) spec.get("battery")).intValue();
        boolean keyboard = (Boolean) spec.get("keyboard");
        boolean camera = (Boolean) spec.get("camera");
        boolean video = (Boolean) spec.get("video");
        boolean wifi = (Boolean) spec.get("wifi");
        boolean bluetooth = (Boolean) spec.get("bluetooth");
        boolean smsFlag = (Boolean) spec.get("sms");
        boolean im = (Boolean) spec.get("IM");
        boolean pda = (Boolean) spec.get("pda");
        boolean mp3 = (Boolean) spec.get("mp3");

        List<String> colors = new ArrayList<>();
        JSONArray colorArr = (JSONArray) spec.get("colors");
        if (colorArr != null) {
            for (Object c : colorArr) {
                colors.add((String) c);
            }
        }
        return new PhoneSpec(make, model, price, size, battery,
                keyboard, camera, video, wifi, bluetooth, smsFlag, im, pda, mp3, colors);
    }

    /** All loaded phone specs. */
    public static List<PhoneSpec> getAllPhones() {
        ensureLoaded();
        return allPhones;
    }

    /** Phones with price below the mid-range threshold. */
    public static List<PhoneSpec> getBudgetPhones() {
        ensureLoaded();
        return budgetPhones;
    }

    /** Phones with price at or above mid-range but below premium. */
    public static List<PhoneSpec> getMidRangePhones() {
        ensureLoaded();
        return midRangePhones;
    }

    /** Phones at or above the premium threshold. */
    public static List<PhoneSpec> getPremiumPhones() {
        ensureLoaded();
        return premiumPhones;
    }

    /**
     * Selects a random phone spec weighted by income level.
     * Low income heavily favors budget phones; high income favors premium.
     *
     * @param incomeLevel "Low", "Middle", or "High"
     * @return a randomly selected PhoneSpec
     */
    public static PhoneSpec selectByIncome(String incomeLevel) {
        ensureLoaded();
        int roll = GameRandom.nextInt(0, 99);

        List<PhoneSpec> pool;
        switch (incomeLevel) {
            case "Low":
                if (roll < 70) {
                    pool = budgetPhones;
                } else if (roll < 95) {
                    pool = midRangePhones;
                } else {
                    pool = premiumPhones;
                }
                break;
            case "High":
                if (roll < 10) {
                    pool = budgetPhones;
                } else if (roll < 40) {
                    pool = midRangePhones;
                } else {
                    pool = premiumPhones;
                }
                break;
            default:
                if (roll < 30) {
                    pool = budgetPhones;
                } else if (roll < 80) {
                    pool = midRangePhones;
                } else {
                    pool = premiumPhones;
                }
                break;
        }

        if (pool.isEmpty()) {
            pool = allPhones;
        }
        return pool.get(GameRandom.nextInt(0, pool.size() - 1));
    }

    /**
     * Selects a random phone spec for staff (even distribution, slight premium lean).
     */
    public static PhoneSpec selectForStaff() {
        ensureLoaded();
        int roll = GameRandom.nextInt(0, 99);

        List<PhoneSpec> pool;
        if (roll < 20) {
            pool = budgetPhones;
        } else if (roll < 60) {
            pool = midRangePhones;
        } else {
            pool = premiumPhones;
        }

        if (pool.isEmpty()) {
            pool = allPhones;
        }
        return pool.get(GameRandom.nextInt(0, pool.size() - 1));
    }

    /** Resets cached data (for testing). */
    public static void reset() {
        allPhones = null;
        budgetPhones = null;
        midRangePhones = null;
        premiumPhones = null;
    }
}
