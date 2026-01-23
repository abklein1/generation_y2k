package behavior.decorator;

import behavior.BehaviorContext;
import behavior.BehaviorNode;
import behavior.BehaviorStatus;

/**
 * Repeats its child node a specified number of times.
 * If repeatCount is -1, repeats indefinitely (until manually stopped).
 * Returns SUCCESS after completing all repetitions.
 * Returns FAILURE immediately if the child fails (unless failOnChildFail is false).
 */
public class Repeater extends DecoratorNode {
    
    private final int repeatCount;
    private int currentCount;
    private final boolean failOnChildFail;
    
    /**
     * Creates a repeater that repeats a specific number of times.
     *
     * @param child the child node
     * @param repeatCount the number of times to repeat (-1 for infinite)
     */
    public Repeater(BehaviorNode child, int repeatCount) {
        super("Repeater", child);
        this.repeatCount = repeatCount;
        this.currentCount = 0;
        this.failOnChildFail = true;
    }
    
    /**
     * Creates a repeater with full options.
     *
     * @param name the name of this node
     * @param child the child node
     * @param repeatCount the number of times to repeat (-1 for infinite)
     * @param failOnChildFail if true, stops and fails when child fails
     */
    public Repeater(String name, BehaviorNode child, int repeatCount, boolean failOnChildFail) {
        super(name, child);
        this.repeatCount = repeatCount;
        this.currentCount = 0;
        this.failOnChildFail = failOnChildFail;
    }
    
    @Override
    public void init(BehaviorContext context) {
        super.init(context);
        currentCount = 0;
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        if (child == null) {
            return BehaviorStatus.FAILURE;
        }
        
        // Check if we've completed all repetitions (for finite repeats)
        if (repeatCount >= 0 && currentCount >= repeatCount) {
            return BehaviorStatus.SUCCESS;
        }
        
        BehaviorStatus status = child.tick(context);
        
        switch (status) {
            case SUCCESS:
                // Child completed, increment count and continue
                child.reset();
                currentCount++;
                
                // Check if we've hit the limit
                if (repeatCount >= 0 && currentCount >= repeatCount) {
                    return BehaviorStatus.SUCCESS;
                }
                
                // Still have more repetitions, return running
                return BehaviorStatus.RUNNING;
                
            case FAILURE:
                if (failOnChildFail) {
                    return BehaviorStatus.FAILURE;
                }
                // Treat failure as success for counting purposes
                child.reset();
                currentCount++;
                
                if (repeatCount >= 0 && currentCount >= repeatCount) {
                    return BehaviorStatus.SUCCESS;
                }
                return BehaviorStatus.RUNNING;
                
            case RUNNING:
            default:
                return BehaviorStatus.RUNNING;
        }
    }
    
    @Override
    public void reset() {
        super.reset();
        currentCount = 0;
    }
    
    /**
     * Gets the current repetition count.
     *
     * @return the current count
     */
    public int getCurrentCount() {
        return currentCount;
    }
    
    /**
     * Gets the target repeat count.
     *
     * @return the repeat count (-1 for infinite)
     */
    public int getRepeatCount() {
        return repeatCount;
    }
}
