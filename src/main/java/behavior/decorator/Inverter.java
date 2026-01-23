package behavior.decorator;

import behavior.BehaviorContext;
import behavior.BehaviorNode;
import behavior.BehaviorStatus;

/**
 * Inverts the result of its child node.
 * SUCCESS becomes FAILURE and vice versa.
 * RUNNING remains RUNNING.
 */
public class Inverter extends DecoratorNode {
    
    /**
     * Creates an inverter with a child.
     *
     * @param child the child node
     */
    public Inverter(BehaviorNode child) {
        super("Inverter", child);
    }
    
    /**
     * Creates an inverter with a name and child.
     *
     * @param name the name of this node
     * @param child the child node
     */
    public Inverter(String name, BehaviorNode child) {
        super(name, child);
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        if (child == null) {
            return BehaviorStatus.FAILURE;
        }
        
        BehaviorStatus status = child.tick(context);
        
        switch (status) {
            case SUCCESS:
                return BehaviorStatus.FAILURE;
            case FAILURE:
                return BehaviorStatus.SUCCESS;
            case RUNNING:
            default:
                return BehaviorStatus.RUNNING;
        }
    }
}
