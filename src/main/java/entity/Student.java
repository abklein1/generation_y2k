package entity;

import behavior.BehaviorContext;
import behavior.BehaviorTree;
import entity.Body.StudentArms;
import entity.Body.StudentLegs;
import entity.Body.StudentUpperT;
import utility.StudentFactory;
import utility.StudentName;
import utility.StudentStatistics;

import java.io.Serializable;

public class Student implements Serializable {

    private final StudentUpperT studentUpperT;
    private final StudentLegs studentLegs;
    private final StudentArms studentArms;
    private final Backpack backpack;
    public StudentName studentName;
    public StudentStatistics studentStatistics;
    StudentFactory studentFactory = new StudentFactory();
    
    // Simulation components
    private EntityState entityState;
    private transient BehaviorTree behaviorTree;
    private transient BehaviorContext behaviorContext;

    public Student() {
        studentName = studentFactory.createName();
        studentUpperT = studentFactory.createUpperTorso();
        studentLegs = studentFactory.createLegs();
        studentArms = studentFactory.createArms();
        backpack = studentFactory.createCarry();
        studentStatistics = studentFactory.setStats();
        entityState = new EntityState();
    }
    
    /**
     * Gets the entity state for simulation tracking.
     *
     * @return the entity state
     */
    public EntityState getEntityState() {
        return entityState;
    }
    
    /**
     * Sets the entity state.
     *
     * @param entityState the entity state
     */
    public void setEntityState(EntityState entityState) {
        this.entityState = entityState;
    }
    
    /**
     * Gets the behavior tree for AI decision making.
     *
     * @return the behavior tree, or null if not set
     */
    public BehaviorTree getBehaviorTree() {
        return behaviorTree;
    }
    
    /**
     * Sets the behavior tree.
     *
     * @param behaviorTree the behavior tree
     */
    public void setBehaviorTree(BehaviorTree behaviorTree) {
        this.behaviorTree = behaviorTree;
    }
    
    /**
     * Gets the behavior context for tree execution.
     *
     * @return the behavior context
     */
    public BehaviorContext getBehaviorContext() {
        return behaviorContext;
    }
    
    /**
     * Sets the behavior context.
     *
     * @param behaviorContext the behavior context
     */
    public void setBehaviorContext(BehaviorContext behaviorContext) {
        this.behaviorContext = behaviorContext;
    }

    @Override
    public String toString() {
        return studentName.getFullName();
    }
}
