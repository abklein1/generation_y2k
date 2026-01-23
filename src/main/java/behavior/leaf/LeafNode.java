package behavior.leaf;

import behavior.BehaviorNode;

/**
 * Base class for leaf nodes (nodes with no children).
 * Leaf nodes perform actual work like checking conditions or executing actions.
 */
public abstract class LeafNode extends BehaviorNode {
    
    /**
     * Creates a leaf node with a name.
     *
     * @param name the name of this node
     */
    public LeafNode(String name) {
        super(name);
    }
    
    /**
     * Creates a leaf node with a default name.
     */
    public LeafNode() {
        super();
    }
}
