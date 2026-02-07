package entity;

import java.io.Serializable;

/**
 * Represents the allostatic load meter for a person in the simulation.
 * Allostatic load is a measurement of accumulated wear on the body's systems
 * due to chronic stressors. It increases when secondary stats are drained
 * through activities and decreases during rest or non-stressful activities.
 *
 * <p>Type 2 allostatic load (social/environmental factors) is the focus here.
 * Each person has a fixed tolerance level derived from their Resilience and
 * Determination stats. Exceeding this tolerance puts the person in "overload",
 * which can have long-term consequences.</p>
 *
 * <p>The load operates on a scale of 0.0 to 100.0:
 * <ul>
 *   <li>0.0 = No accumulated stress</li>
 *   <li>50.0 = Moderate stress accumulation</li>
 *   <li>100.0 = Maximum possible load</li>
 * </ul>
 * </p>
 */
public class AllostaticLoad implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The current allostatic load value (0.0 to 100.0). */
    private double currentLoad;

    /**
     * The maximum load this person can tolerate before entering overload.
     * Derived from Resilience and Determination during initialization.
     * People with higher resilience/determination can handle more stress.
     */
    private double maxTolerance;

    /** Tracks how many consecutive days the person has been in overload. */
    private int consecutiveOverloadDays;

    /** Whether the person is currently in an overloaded state. */
    private boolean overloaded;

    /**
     * Creates a new AllostaticLoad with default values.
     * Load starts at 0 (no stress) with a default tolerance of 50.
     */
    public AllostaticLoad() {
        this.currentLoad = 0.0;
        this.maxTolerance = 50.0;
        this.consecutiveOverloadDays = 0;
        this.overloaded = false;
    }

    /**
     * Creates a new AllostaticLoad with a specified tolerance.
     *
     * @param maxTolerance the maximum load this person can handle before overload
     */
    public AllostaticLoad(double maxTolerance) {
        this.currentLoad = 0.0;
        this.maxTolerance = Math.max(10.0, Math.min(100.0, maxTolerance));
        this.consecutiveOverloadDays = 0;
        this.overloaded = false;
    }

    /**
     * Calculates and sets the max tolerance based on a person's primary stats.
     * Resilience is the primary driver, with Determination as secondary.
     * Higher resilience and determination allow a person to tolerate more stress.
     *
     * @param resilience  the person's current resilience stat
     * @param determination the person's determination stat
     */
    public void initTolerance(int resilience, int determination) {
        // Base tolerance of 40, scaled up by resilience (primary) and determination (secondary)
        // Resilience contributes ~60% and determination ~40% of the bonus
        double resilienceContribution = resilience * 0.30;
        double determinationContribution = determination * 0.15;
        double baseTolerance = 40.0 + resilienceContribution + determinationContribution;

        // Clamp between 25 and 95
        this.maxTolerance = Math.max(25.0, Math.min(95.0, baseTolerance));
    }

    /**
     * Gets the current allostatic load.
     *
     * @return the current load value (0.0 to 100.0)
     */
    public double getCurrentLoad() {
        return currentLoad;
    }

    /**
     * Gets the maximum tolerance before overload.
     *
     * @return the tolerance threshold
     */
    public double getMaxTolerance() {
        return maxTolerance;
    }

    /**
     * Gets the current load as a percentage of the max tolerance.
     * Values above 100% indicate the person is in overload.
     *
     * @return the load percentage (0.0 to potentially above 100.0)
     */
    public double getLoadPercentage() {
        if (maxTolerance <= 0) {
            return 100.0;
        }
        return (currentLoad / maxTolerance) * 100.0;
    }

    /**
     * Checks whether the person is currently in allostatic overload.
     * Overload occurs when the current load exceeds the max tolerance.
     *
     * @return true if the person is overloaded
     */
    public boolean isOverloaded() {
        return overloaded;
    }

    /**
     * Gets the number of consecutive days the person has been in overload.
     * This tracks chronic overload which could have lasting consequences.
     *
     * @return the number of consecutive overload days
     */
    public int getConsecutiveOverloadDays() {
        return consecutiveOverloadDays;
    }

    /**
     * Increases the allostatic load by the specified amount.
     * The load is capped at 100.0. If the load exceeds the tolerance
     * threshold, the person enters an overloaded state.
     *
     * @param amount the amount to increase (must be positive)
     */
    public void increaseLoad(double amount) {
        if (amount <= 0) {
            return;
        }
        this.currentLoad = Math.min(100.0, this.currentLoad + amount);
        updateOverloadStatus();
    }

    /**
     * Decreases the allostatic load by the specified amount.
     * The load will not go below 0.0. If the load drops back below
     * the tolerance threshold, the overloaded state is cleared.
     *
     * @param amount the amount to decrease (must be positive)
     */
    public void decreaseLoad(double amount) {
        if (amount <= 0) {
            return;
        }
        this.currentLoad = Math.max(0.0, this.currentLoad - amount);
        updateOverloadStatus();
    }

    /**
     * Called when a secondary stat is drained below its maximum.
     * The allostatic load increase is proportional to how much the stat
     * was drained relative to its maximum value.
     *
     * @param drainAmount the amount the secondary stat was reduced
     * @param statMax     the maximum value of the secondary stat
     * @param stressFactor a multiplier for how stressful this particular drain is
     *                     (1.0 = normal, higher = more stressful)
     */
    public void onSecondaryStatDrain(int drainAmount, int statMax, double stressFactor) {
        if (drainAmount <= 0 || statMax <= 0) {
            return;
        }
        // Load increase is proportional to the fraction of the stat that was drained
        // A person losing 10 out of 50 empathy is more significant than 10 out of 100
        double drainRatio = (double) drainAmount / statMax;
        double loadIncrease = drainRatio * stressFactor * 10.0;
        increaseLoad(loadIncrease);
    }

    /**
     * Applies sleep recovery to the allostatic load.
     * Sleep is the primary mechanism for reducing allostatic load.
     * The recovery amount depends on the base recovery rate and how
     * overloaded the person is (overloaded people recover less efficiently).
     *
     * @param baseRecoveryRate the base amount of load to recover during sleep
     */
    public void applySleepRecovery(double baseRecoveryRate) {
        double recoveryAmount = baseRecoveryRate;

        // If overloaded, recovery is less efficient (chronic stress reduces recovery)
        if (overloaded) {
            recoveryAmount *= 0.6;
        }

        decreaseLoad(recoveryAmount);
    }

    /**
     * Applies a small recovery from performing a non-stressful activity
     * such as talking to a friend, relaxing, or engaging in a hobby.
     *
     * @param recoveryAmount the amount of load to recover
     */
    public void applyRelaxationRecovery(double recoveryAmount) {
        decreaseLoad(recoveryAmount);
    }

    /**
     * Called at the end of each day to track consecutive overload days.
     * Should be called before sleep recovery is applied.
     */
    public void endOfDayCheck() {
        if (overloaded) {
            consecutiveOverloadDays++;
        } else {
            consecutiveOverloadDays = 0;
        }
    }

    /**
     * Updates the overload status based on current load vs tolerance.
     */
    private void updateOverloadStatus() {
        this.overloaded = this.currentLoad >= this.maxTolerance;
    }

    /**
     * Resets the allostatic load to zero. Used for testing or special events.
     */
    public void reset() {
        this.currentLoad = 0.0;
        this.overloaded = false;
        this.consecutiveOverloadDays = 0;
    }

    @Override
    public String toString() {
        return String.format("AllostaticLoad{load=%.1f/%.1f (%.0f%%), overloaded=%s, overloadDays=%d}",
                currentLoad, maxTolerance, getLoadPercentage(), overloaded, consecutiveOverloadDays);
    }
}
