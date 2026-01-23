package simulation.action;

import behavior.BehaviorContext;
import entity.EntityState;

/**
 * Interface for actions that entities can perform in the simulation.
 */
public interface Action {
    
    /**
     * Gets the name of this action.
     *
     * @return the action name
     */
    String getName();
    
    /**
     * Gets the display name for UI purposes.
     *
     * @return the display name
     */
    String getDisplayName();
    
    /**
     * Gets how many ticks this action takes to complete.
     *
     * @return duration in ticks
     */
    int getDurationTicks();
    
    /**
     * Checks if this action can be executed given the current state.
     *
     * @param state the entity state
     * @param context the behavior context
     * @return true if the action can execute
     */
    boolean canExecute(EntityState state, BehaviorContext context);
    
    /**
     * Executes the action.
     *
     * @param state the entity state
     * @param context the behavior context
     * @return the result of the action
     */
    ActionResult execute(EntityState state, BehaviorContext context);
    
    /**
     * Gets the category of this action.
     *
     * @return the action category
     */
    ActionCategory getCategory();
    
    /**
     * Gets the success probability for this action given the context.
     * Used for UI display and risk assessment.
     *
     * @param context the behavior context
     * @return probability from 0.0 to 1.0
     */
    default double getSuccessProbability(BehaviorContext context) {
        return 1.0;
    }
    
    /**
     * Gets the risk level of this action.
     * Higher risk means more chance of negative consequences if caught.
     *
     * @return risk level from 0 (safe) to 100 (very risky)
     */
    default int getRiskLevel() {
        return 0;
    }
}
