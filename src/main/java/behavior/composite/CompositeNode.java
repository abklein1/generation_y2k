package behavior.composite;

import behavior.BehaviorContext;
import behavior.BehaviorNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for composite nodes that contain multiple children.
 */
public abstract class CompositeNode extends BehaviorNode {
    
    protected final List<BehaviorNode> children;
    protected int currentChildIndex;
    
    /**
     * Creates a composite node with a name.
     *
     * @param name the name of this node
     */
    public CompositeNode(String name) {
        super(name);
        this.children = new ArrayList<>();
        this.currentChildIndex = 0;
    }
    
    /**
     * Creates a composite node with a default name.
     */
    public CompositeNode() {
        super();
        this.children = new ArrayList<>();
        this.currentChildIndex = 0;
    }
    
    /**
     * Adds a child node.
     *
     * @param child the child to add
     * @return this node for chaining
     */
    public CompositeNode addChild(BehaviorNode child) {
        children.add(child);
        return this;
    }
    
    /**
     * Adds multiple child nodes.
     *
     * @param nodes the children to add
     * @return this node for chaining
     */
    public CompositeNode addChildren(BehaviorNode... nodes) {
        for (BehaviorNode node : nodes) {
            children.add(node);
        }
        return this;
    }
    
    /**
     * Removes a child node.
     *
     * @param child the child to remove
     * @return true if removed
     */
    public boolean removeChild(BehaviorNode child) {
        return children.remove(child);
    }
    
    /**
     * Gets all children.
     *
     * @return the list of children
     */
    public List<BehaviorNode> getChildren() {
        return children;
    }
    
    /**
     * Gets the number of children.
     *
     * @return the child count
     */
    public int getChildCount() {
        return children.size();
    }
    
    /**
     * Gets a child by index.
     *
     * @param index the child index
     * @return the child node
     */
    public BehaviorNode getChild(int index) {
        return children.get(index);
    }
    
    @Override
    public void init(BehaviorContext context) {
        super.init(context);
        currentChildIndex = 0;
    }
    
    @Override
    public void reset() {
        super.reset();
        currentChildIndex = 0;
        for (BehaviorNode child : children) {
            child.reset();
        }
    }
}
