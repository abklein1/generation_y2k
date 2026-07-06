package utility;

import entity.Items.ClothingItem;
import entity.Items.Outfit;
import entity.Items.Wardrobe;
import entity.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static constants.SimConstants.*;
import static utility.Randomizer.setRandom;

/**
 * Dresses students each morning from their pre-generated {@link Wardrobe}.
 *
 * <p>During the first school week every student wears each of their
 * pre-generated outfits once, preferring (among the not-yet-worn ones)
 * the outfit whose total warmth best matches the day's weather. Once
 * every outfit has been worn, daily outfits are recombined from the
 * pool of owned {@link ClothingItem}s: several random candidates are
 * assembled from a clique recipe and the one whose warmth is closest to
 * the day's ideal is worn.</p>
 *
 * <p>Students without a wardrobe (cliques whose clothing inventory is
 * still unpopulated) keep whatever outfit they already have.</p>
 */
public final class DailyOutfitAssigner {

    private DailyOutfitAssigner() {
    }

    /**
     * Maps the day's forecast to the total outfit warmth that keeps a
     * student comfortable. Uses the average of the day's high and low.
     *
     * @param tempHighF the day's high in Fahrenheit (TMAX)
     * @param tempLowF  the day's low in Fahrenheit (TMIN)
     * @return the ideal total outfit warmth for the day
     */
    public static int idealWarmthForTemps(int tempHighF, int tempLowF) {
        return idealWarmthForTemp((tempHighF + tempLowF) / 2);
    }

    /**
     * Maps a single ambient temperature to the total outfit warmth that
     * keeps a student comfortable in it. Used for indoor comfort, where
     * the room's HVAC temperature replaces the outdoor daily average.
     *
     * @param tempF the ambient temperature in Fahrenheit
     * @return the ideal total outfit warmth for that temperature
     */
    public static int idealWarmthForTemp(int tempF) {
        if (tempF < CLOTHING_TEMP_COLD_MAX_F) {
            return CLOTHING_IDEAL_WARMTH_COLD;
        }
        if (tempF < CLOTHING_TEMP_COOL_MAX_F) {
            return CLOTHING_IDEAL_WARMTH_COOL;
        }
        if (tempF < CLOTHING_TEMP_MILD_MAX_F) {
            return CLOTHING_IDEAL_WARMTH_MILD;
        }
        if (tempF < CLOTHING_TEMP_WARM_MAX_F) {
            return CLOTHING_IDEAL_WARMTH_WARM;
        }
        return CLOTHING_IDEAL_WARMTH_HOT;
    }

    /**
     * Assigns the day's outfit to every student based on the forecast.
     *
     * @param students  the student population
     * @param tempHighF the day's high in Fahrenheit
     * @param tempLowF  the day's low in Fahrenheit
     */
    public static void assignDailyOutfits(Map<Integer, Student> students,
                                          int tempHighF, int tempLowF) {
        if (students == null) {
            return;
        }
        int idealWarmth = idealWarmthForTemps(tempHighF, tempLowF);
        for (Student student : students.values()) {
            assignOutfitForDay(student, idealWarmth);
        }
    }

    /**
     * Assigns one student's outfit for the day. While unworn
     * pre-generated outfits remain, the best-fitting one is worn next;
     * afterwards a fresh outfit is recombined from owned pieces.
     *
     * @param student     the student to dress
     * @param idealWarmth the target total outfit warmth for the day
     */
    public static void assignOutfitForDay(Student student, int idealWarmth) {
        if (student == null || student.studentStatistics == null) {
            return;
        }
        Wardrobe wardrobe = student.studentStatistics.getWardrobe();
        if (wardrobe == null || wardrobe.isEmpty()) {
            return;
        }

        if (wardrobe.hasUnwornOutfits()) {
            Outfit next = wardrobe.wearBestFit(idealWarmth);
            student.studentStatistics.setCurrentOutfit(next);
            return;
        }

        Outfit recombined = recombineOutfit(student, wardrobe, idealWarmth);
        if (recombined != null && !recombined.isEmpty()) {
            student.studentStatistics.setCurrentOutfit(recombined);
        }
    }

    /**
     * Builds several candidate outfits from the wardrobe's item pool and
     * returns the one whose total warmth is closest to the ideal. This
     * keeps daily looks varied while biasing them toward the weather.
     */
    private static Outfit recombineOutfit(Student student, Wardrobe wardrobe,
                                          int idealWarmth) {
        Outfit best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < OUTFIT_RECOMBINATION_CANDIDATES; i++) {
            Outfit candidate = buildCandidate(student, wardrobe);
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            int distance = Math.abs(candidate.getTotalWarmth() - idealWarmth);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    /**
     * Assembles one random outfit from owned pieces using a recipe the
     * wardrobe's item pool can satisfy. Mirrors the layer rules used at
     * generation: required layers always filled, optional layers rolled
     * with the same chances, accessory counts capped by the recipe.
     */
    private static Outfit buildCandidate(Student student, Wardrobe wardrobe) {
        OutfitTypeLoader.OutfitTypeData recipe = pickRecipeForPool(
                student, wardrobe);
        if (recipe == null) {
            return null;
        }

        Outfit outfit = new Outfit(recipe.getName());
        for (String layer : recipe.getRequiredLayers()) {
            addOwnedItemsForLayer(outfit, layer, recipe, wardrobe);
        }
        for (String layer : recipe.getOptionalLayers()) {
            int chance = "accessories".equals(layer)
                    ? CLIQUE_CLOTHING_OPTIONAL_ACCESSORY_CHANCE
                    : CLIQUE_CLOTHING_OPTIONAL_LAYER_CHANCE;
            if (setRandom(0, 99) < chance) {
                addOwnedItemsForLayer(outfit, layer, recipe, wardrobe);
            }
        }
        return outfit;
    }

    /**
     * Picks a recipe whose required layers the owned item pool can fill.
     * Prefers a weighted pick from the clique's {@code outfit_types}
     * list, falling back to any loaded recipe the pool satisfies.
     */
    private static OutfitTypeLoader.OutfitTypeData pickRecipeForPool(
            Student student, Wardrobe wardrobe) {
        String clique = student.studentStatistics.getMainClique();
        String gender = student.studentStatistics.getGender();

        List<CliqueClothingLoader.OutfitTypeRef> refs =
                CliqueClothingLoader.getOutfitTypeRefs(clique, gender);
        if (!refs.isEmpty()) {
            List<OutfitTypeLoader.OutfitTypeData> viable = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            int totalWeight = 0;
            for (CliqueClothingLoader.OutfitTypeRef ref : refs) {
                OutfitTypeLoader.OutfitTypeData recipe =
                        OutfitTypeLoader.getOutfitType(ref.getName());
                if (recipe != null && poolSatisfies(recipe, wardrobe)) {
                    viable.add(recipe);
                    weights.add(ref.getWeight());
                    totalWeight += ref.getWeight();
                }
            }
            if (!viable.isEmpty()) {
                int roll = setRandom(0, totalWeight - 1);
                for (int i = 0; i < viable.size(); i++) {
                    roll -= weights.get(i);
                    if (roll < 0) {
                        return viable.get(i);
                    }
                }
                return viable.get(viable.size() - 1);
            }
        }

        List<OutfitTypeLoader.OutfitTypeData> viable = new ArrayList<>();
        for (OutfitTypeLoader.OutfitTypeData recipe
                : OutfitTypeLoader.getAllOutfitTypes()) {
            if (poolSatisfies(recipe, wardrobe)) {
                viable.add(recipe);
            }
        }
        if (viable.isEmpty()) {
            return null;
        }
        return viable.get(setRandom(0, viable.size() - 1));
    }

    private static boolean poolSatisfies(
            OutfitTypeLoader.OutfitTypeData recipe, Wardrobe wardrobe) {
        for (String layer : recipe.getRequiredLayers()) {
            if (wardrobe.getItemsByLayer(layer).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds items for one layer to the candidate outfit, drawn only from
     * the wardrobe's owned pool. Non-accessory layers get a single
     * random item; accessories get 1 to the recipe's max, avoiding
     * duplicates.
     */
    private static void addOwnedItemsForLayer(Outfit outfit, String layer,
                                              OutfitTypeLoader.OutfitTypeData recipe,
                                              Wardrobe wardrobe) {
        List<ClothingItem> pool = wardrobe.getItemsByLayer(layer);
        if (pool.isEmpty()) {
            return;
        }
        if (!"accessories".equals(layer)) {
            outfit.addItem(pool.get(setRandom(0, pool.size() - 1)));
            return;
        }
        int max = Math.max(1, recipe.getMaxForLayer(layer));
        int count = Math.min(setRandom(1, max), pool.size());
        List<ClothingItem> candidates = new ArrayList<>(pool);
        for (int i = 0; i < count && !candidates.isEmpty(); i++) {
            int index = setRandom(0, candidates.size() - 1);
            outfit.addItem(candidates.remove(index));
        }
    }
}
