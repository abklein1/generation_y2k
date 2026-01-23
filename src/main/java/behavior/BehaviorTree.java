package behavior;

/**
 * Represents a complete behavior tree with a root node.
 * Manages the execution of the tree through ticks.
 */
public class BehaviorTree {
    
    private final BehaviorNode root;
    private final String name;
    private BehaviorStatus lastStatus;
    private int tickCount;
    
    /**
     * Creates a behavior tree with a root node.
     *
     * @param name the name of this tree
     * @param root the root node
     */
    public BehaviorTree(String name, BehaviorNode root) {
        this.name = name;
        this.root = root;
        this.lastStatus = null;
        this.tickCount = 0;
    }
    
    /**
     * Creates a behavior tree with just a root node.
     *
     * @param root the root node
     */
    public BehaviorTree(BehaviorNode root) {
        this("BehaviorTree", root);
    }
    
    /**
     * Executes one tick of the behavior tree.
     *
     * @param context the behavior context
     * @return the status after this tick
     */
    public BehaviorStatus tick(BehaviorContext context) {
        if (root == null) {
            return BehaviorStatus.FAILURE;
        }
        
        tickCount++;
        lastStatus = root.tick(context);
        
        // If the tree completed (success or failure), reset for next run
        if (lastStatus != BehaviorStatus.RUNNING) {
            root.reset();
        }
        
        return lastStatus;
    }
    
    /**
     * Resets the entire behavior tree.
     */
    public void reset() {
        if (root != null) {
            root.reset();
        }
        lastStatus = null;
        tickCount = 0;
    }
    
    /**
     * Gets the root node of this tree.
     *
     * @return the root node
     */
    public BehaviorNode getRoot() {
        return root;
    }
    
    /**
     * Gets the name of this tree.
     *
     * @return the tree name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the status from the last tick.
     *
     * @return the last status, or null if never ticked
     */
    public BehaviorStatus getLastStatus() {
        return lastStatus;
    }
    
    /**
     * Gets the total number of ticks executed.
     *
     * @return the tick count
     */
    public int getTickCount() {
        return tickCount;
    }
    
    @Override
    public String toString() {
        return name + " [root=" + (root != null ? root.getName() : "null") + 
               ", lastStatus=" + lastStatus + ", ticks=" + tickCount + "]";
    }
}
