package behavior.leaf;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;

/**
 * Base class for action nodes that perform work over time.
 * Actions can take multiple ticks to complete and may return RUNNING.
 */
public abstract class ActionNode extends LeafNode {
    
    protected int ticksElapsed;
    protected int durationTicks;
    
    /**
     * Creates an action node with a name.
     *
     * @param name the name of this action
     */
    public ActionNode(String name) {
        super(name);
        this.ticksElapsed = 0;
        this.durationTicks = 1;
    }
    
    /**
     * Creates an action node with a name and duration.
     *
     * @param name the name of this action
     * @param durationTicks how many ticks this action takes
     */
    public ActionNode(String name, int durationTicks) {
        super(name);
        this.ticksElapsed = 0;
        this.durationTicks = Math.max(1, durationTicks);
    }
    
    /**
     * Creates an action node with a default name.
     */
    public ActionNode() {
        super();
        this.ticksElapsed = 0;
        this.durationTicks = 1;
    }
    
    @Override
    public void init(BehaviorContext context) {
        super.init(context);
        ticksElapsed = 0;
        onStart(context);
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        // Check if we can execute
        if (!canExecute(context)) {
            return BehaviorStatus.FAILURE;
        }
        
        // Execute the action
        BehaviorStatus status = execute(context);
        ticksElapsed++;
        
        // If we're done (success or failure), call onEnd
        if (status != BehaviorStatus.RUNNING) {
            onEnd(context, status);
        }
        
        return status;
    }
    
    @Override
    public void reset() {
        super.reset();
        ticksElapsed = 0;
    }
    
    /**
     * Checks if the action can be executed.
     * Override to add preconditions.
     *
     * @param context the behavior context
     * @return true if the action can execute
     */
    public boolean canExecute(BehaviorContext context) {
        return true;
    }
    
    /**
     * Executes one tick of the action.
     *
     * @param context the behavior context
     * @return the status after this tick
     */
    public abstract BehaviorStatus execute(BehaviorContext context);
    
    /**
     * Called when the action starts.
     * Override to perform setup.
     *
     * @param context the behavior context
     */
    protected void onStart(BehaviorContext context) {
        // Default: do nothing
    }
    
    /**
     * Called when the action ends.
     * Override to perform cleanup.
     *
     * @param context the behavior context
     * @param finalStatus the final status (SUCCESS or FAILURE)
     */
    protected void onEnd(BehaviorContext context, BehaviorStatus finalStatus) {
        // Default: do nothing
    }
    
    /**
     * Gets the number of ticks elapsed.
     *
     * @return ticks elapsed
     */
    public int getTicksElapsed() {
        return ticksElapsed;
    }
    
    /**
     * Gets the expected duration in ticks.
     *
     * @return duration in ticks
     */
    public int getDurationTicks() {
        return durationTicks;
    }
    
    /**
     * Sets the duration in ticks.
     *
     * @param durationTicks the new duration
     */
    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }
    
    /**
     * Checks if this action is complete based on ticks.
     *
     * @return true if ticksElapsed >= durationTicks
     */
    protected boolean isComplete() {
        return ticksElapsed >= durationTicks;
    }
}
