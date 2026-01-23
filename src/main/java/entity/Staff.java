package entity;

import behavior.BehaviorContext;
import behavior.BehaviorTree;
import entity.Body.TeacherArms;
import entity.Body.TeacherLegs;
import entity.Body.TeacherUpperT;
import utility.TeacherFactory;
import utility.TeacherName;
import utility.TeacherStatistics;

import java.io.Serializable;

public class Staff implements Serializable {

    private final TeacherUpperT teacherUpperT;
    private final TeacherLegs teacherLegs;
    private final TeacherArms teacherArms;
    private final ShoulderBag shoulderBag;
    public TeacherName teacherName;
    public TeacherStatistics teacherStatistics;

    TeacherFactory teacherFactory = new TeacherFactory();
    
    // Simulation components
    private EntityState entityState;
    private transient BehaviorTree behaviorTree;
    private transient BehaviorContext behaviorContext;

    public Staff() {
        teacherName = teacherFactory.createName();
        teacherUpperT = teacherFactory.createUpperTorso();
        teacherLegs = teacherFactory.createLegs();
        teacherArms = teacherFactory.createArms();
        shoulderBag = teacherFactory.createCarry();
        teacherStatistics = teacherFactory.setStats();
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
        return teacherName.getFirstName() + " " + teacherName.getLastName();
    }

}
