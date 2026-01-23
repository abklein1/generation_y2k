package behavior.decorator;

import behavior.BehaviorContext;
import behavior.BehaviorNode;
import behavior.BehaviorStatus;

/**
 * Always returns SUCCESS after its child completes, regardless of the child's result.
 * Useful for optional tasks that shouldn't fail the parent sequence.
 * RUNNING is passed through unchanged.
 */
public class Succeeder extends DecoratorNode {
    
    /**
     * Creates a succeeder with a child.
     *
     * @param child the child node
     */
    public Succeeder(BehaviorNode child) {
        super("Succeeder", child);
    }
    
    /**
     * Creates a succeeder with a name and child.
     *
     * @param name the name of this node
     * @param child the child node
     */
    public Succeeder(String name, BehaviorNode child) {
        super(name, child);
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        if (child == null) {
            return BehaviorStatus.SUCCESS;
        }
        
        BehaviorStatus status = child.tick(context);
        
        if (status == BehaviorStatus.RUNNING) {
            return BehaviorStatus.RUNNING;
        }
        
        // Always return success when child completes
        return BehaviorStatus.SUCCESS;
    }
}
