package behavior.decorator;

import behavior.BehaviorContext;
import behavior.BehaviorNode;
import behavior.BehaviorStatus;

/**
 * Repeats its child node until the child returns FAILURE.
 * Always returns SUCCESS when the child finally fails.
 * Useful for "do while" style loops.
 */
public class RepeatUntilFail extends DecoratorNode {
    
    private int executionCount;
    
    /**
     * Creates a repeat-until-fail node with a child.
     *
     * @param child the child node
     */
    public RepeatUntilFail(BehaviorNode child) {
        super("RepeatUntilFail", child);
        this.executionCount = 0;
    }
    
    /**
     * Creates a repeat-until-fail node with a name and child.
     *
     * @param name the name of this node
     * @param child the child node
     */
    public RepeatUntilFail(String name, BehaviorNode child) {
        super(name, child);
        this.executionCount = 0;
    }
    
    @Override
    public void init(BehaviorContext context) {
        super.init(context);
        executionCount = 0;
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        if (child == null) {
            return BehaviorStatus.SUCCESS;
        }
        
        BehaviorStatus status = child.tick(context);
        
        switch (status) {
            case FAILURE:
                // Child failed, we're done - return success
                return BehaviorStatus.SUCCESS;
                
            case SUCCESS:
                // Child succeeded, reset and continue next tick
                child.reset();
                executionCount++;
                return BehaviorStatus.RUNNING;
                
            case RUNNING:
            default:
                return BehaviorStatus.RUNNING;
        }
    }
    
    @Override
    public void reset() {
        super.reset();
        executionCount = 0;
    }
    
    /**
     * Gets the number of times the child has been executed.
     *
     * @return the execution count
     */
    public int getExecutionCount() {
        return executionCount;
    }
}
