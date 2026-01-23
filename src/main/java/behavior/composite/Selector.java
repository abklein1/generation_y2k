package behavior.composite;

import behavior.BehaviorContext;
import behavior.BehaviorNode;
import behavior.BehaviorStatus;

/**
 * A composite node that tries children in order until one succeeds.
 * Succeeds if ANY child succeeds.
 * Fails only if ALL children fail.
 * Returns RUNNING if a child is still running.
 */
public class Selector extends CompositeNode {
    
    /**
     * Creates a selector node with a name.
     *
     * @param name the name of this node
     */
    public Selector(String name) {
        super(name);
    }
    
    /**
     * Creates a selector node with a default name.
     */
    public Selector() {
        super("Selector");
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        // If no children, fail immediately
        if (children.isEmpty()) {
            return BehaviorStatus.FAILURE;
        }
        
        // Continue from where we left off
        while (currentChildIndex < children.size()) {
            BehaviorNode child = children.get(currentChildIndex);
            BehaviorStatus status = child.tick(context);
            
            switch (status) {
                case SUCCESS:
                    // A child succeeded, the selector succeeds
                    return BehaviorStatus.SUCCESS;
                    
                case RUNNING:
                    // Child is still running, wait for next tick
                    return BehaviorStatus.RUNNING;
                    
                case FAILURE:
                    // Child failed, try next child
                    child.reset();
                    currentChildIndex++;
                    break;
            }
        }
        
        // All children failed
        return BehaviorStatus.FAILURE;
    }
}
