package behavior.leaf;

import behavior.BehaviorContext;
import behavior.BehaviorStatus;

/**
 * Base class for condition nodes that test a predicate.
 * Conditions return SUCCESS if the condition is true, FAILURE otherwise.
 * Conditions are instantaneous and never return RUNNING.
 */
public abstract class ConditionNode extends LeafNode {
    
    /**
     * Creates a condition node with a name.
     *
     * @param name the name of this condition
     */
    public ConditionNode(String name) {
        super(name);
    }
    
    /**
     * Creates a condition node with a default name.
     */
    public ConditionNode() {
        super();
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        return check(context) ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
    
    /**
     * Checks if the condition is true.
     *
     * @param context the behavior context
     * @return true if the condition is met
     */
    public abstract boolean check(BehaviorContext context);
}
