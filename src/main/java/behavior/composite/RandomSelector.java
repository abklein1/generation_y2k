package behavior.composite;

import behavior.BehaviorContext;
import behavior.BehaviorNode;
import behavior.BehaviorStatus;
import utility.GameRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A selector that tries children in random order until one succeeds.
 * The order is randomized once when the node is initialized.
 */
public class RandomSelector extends CompositeNode {
    
    private List<BehaviorNode> shuffledChildren;
    
    /**
     * Creates a random selector node with a name.
     *
     * @param name the name of this node
     */
    public RandomSelector(String name) {
        super(name);
        this.shuffledChildren = new ArrayList<>();
    }
    
    /**
     * Creates a random selector node with a default name.
     */
    public RandomSelector() {
        super("RandomSelector");
        this.shuffledChildren = new ArrayList<>();
    }
    
    @Override
    public void init(BehaviorContext context) {
        super.init(context);
        // Create a shuffled copy of children
        shuffledChildren = new ArrayList<>(children);
        shuffleList(shuffledChildren);
    }
    
    /**
     * Shuffles a list using the game's random generator.
     *
     * @param list the list to shuffle
     */
    private void shuffleList(List<BehaviorNode> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = (int) (GameRandom.nextDouble(1.0) * (i + 1));
            Collections.swap(list, i, j);
        }
    }
    
    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ensureInitialized(context);
        
        // If no children, fail immediately
        if (shuffledChildren.isEmpty()) {
            return BehaviorStatus.FAILURE;
        }
        
        // Continue from where we left off
        while (currentChildIndex < shuffledChildren.size()) {
            BehaviorNode child = shuffledChildren.get(currentChildIndex);
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
    
    @Override
    public void reset() {
        super.reset();
        shuffledChildren.clear();
    }
}
