package behavior;

/**
 * Abstract base class for all behavior tree nodes.
 * Each node can be initialized, ticked (executed), and reset.
 */
public abstract class BehaviorNode {
    
    protected String name;
    protected boolean initialized;
    
    /**
     * Creates a behavior node with a name.
     *
     * @param name the name of this node
     */
    public BehaviorNode(String name) {
        this.name = name;
        this.initialized = false;
    }
    
    /**
     * Creates a behavior node with a default name.
     */
    public BehaviorNode() {
        this.name = getClass().getSimpleName();
        this.initialized = false;
    }
    
    /**
     * Initializes the node before execution.
     * Called once before the first tick.
     *
     * @param context the behavior context
     */
    public void init(BehaviorContext context) {
        initialized = true;
    }
    
    /**
     * Executes one tick of this node's behavior.
     *
     * @param context the behavior context
     * @return the status after this tick
     */
    public abstract BehaviorStatus tick(BehaviorContext context);
    
    /**
     * Resets this node to its initial state.
     * Called when the behavior tree is reset or when this node needs to restart.
     */
    public void reset() {
        initialized = false;
    }
    
    /**
     * Gets the name of this node.
     *
     * @return the node name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of this node.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Checks if this node has been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Ensures the node is initialized before ticking.
     * Call this at the start of tick() implementations.
     *
     * @param context the behavior context
     */
    protected void ensureInitialized(BehaviorContext context) {
        if (!initialized) {
            init(context);
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
}
