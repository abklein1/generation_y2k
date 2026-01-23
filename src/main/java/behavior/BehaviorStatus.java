package behavior;

/**
 * Represents the possible return statuses of a behavior tree node.
 */
public enum BehaviorStatus {
    /**
     * The node completed successfully.
     */
    SUCCESS,
    
    /**
     * The node failed to complete its task.
     */
    FAILURE,
    
    /**
     * The node is still running and needs more ticks to complete.
     */
    RUNNING
}
