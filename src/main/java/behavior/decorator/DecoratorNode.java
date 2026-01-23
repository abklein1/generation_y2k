package behavior.decorator;

import behavior.BehaviorContext;
import behavior.BehaviorNode;

/**
 * Base class for decorator nodes that wrap a single child.
 * Decorators modify the behavior or result of their child node.
 */
public abstract class DecoratorNode extends BehaviorNode {
    
    protected BehaviorNode child;
    
    /**
     * Creates a decorator node with a name and child.
     *
     * @param name the name of this node
     * @param child the child node to wrap
     */
    public DecoratorNode(String name, BehaviorNode child) {
        super(name);
        this.child = child;
    }
    
    /**
     * Creates a decorator node with just a child.
     *
     * @param child the child node to wrap
     */
    public DecoratorNode(BehaviorNode child) {
        super();
        this.child = child;
    }
    
    /**
     * Creates a decorator node without a child.
     * The child must be set later.
     *
     * @param name the name of this node
     */
    public DecoratorNode(String name) {
        super(name);
        this.child = null;
    }
    
    /**
     * Gets the child node.
     *
     * @return the child node
     */
    public BehaviorNode getChild() {
        return child;
    }
    
    /**
     * Sets the child node.
     *
     * @param child the child node
     */
    public void setChild(BehaviorNode child) {
        this.child = child;
    }
    
    @Override
    public void init(BehaviorContext context) {
        super.init(context);
    }
    
    @Override
    public void reset() {
        super.reset();
        if (child != null) {
            child.reset();
        }
    }
}
