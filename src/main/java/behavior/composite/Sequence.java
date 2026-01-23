package behavior.composite;

import behavior.BehaviorContext;
import behavior.BehaviorNode;
import behavior.BehaviorStatus;

/**
 * A composite node that runs children in sequence.
 * Succeeds only if ALL children succeed.
 * Fails immediately if any child fails.
 * Returns RUNNING if a child is still running.
 */
public class Sequence extends CompositeNode {
    
    /**
     * Creates a sequence node with a name.
     *
     * @param name the name of this node
     */
    public Sequence(String name) {
        super(name);
    }
    
    /**
     * Creates a sequence node with a default name.
     */
    public Sequence() {
        super("Sequence");
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        // If no children, succeed immediately
        if (children.isEmpty()) {
            return BehaviorStatus.SUCCESS;
        }
        
        // Continue from where we left off
        while (currentChildIndex < children.size()) {
            BehaviorNode child = children.get(currentChildIndex);
            BehaviorStatus status = child.tick(context);
            
            switch (status) {
                case FAILURE:
                    // A child failed, the sequence fails
                    return BehaviorStatus.FAILURE;
                    
                case RUNNING:
                    // Child is still running, wait for next tick
                    return BehaviorStatus.RUNNING;
                    
                case SUCCESS:
                    // Child succeeded, move to next child
                    child.reset();
                    currentChildIndex++;
                    break;
            }
        }
        
        // All children succeeded
        return BehaviorStatus.SUCCESS;
    }
}
