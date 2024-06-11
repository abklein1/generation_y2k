package entity;

import java.util.List;

public class ClassDetail {
    public String subject;
    public boolean honors;
    public boolean onLevel;
    public boolean required;
    public List<String> prerequisite;
    public List<String> alternatives;
    public List<Integer> gradeLevel;

    public ClassDetail(String subject, boolean honors, boolean onLevel, boolean required, List<String> prerequisite, List<String> alternatives, List<Integer> gradeLevel) {
        this.subject = subject;
        this.honors = honors;
        this.onLevel = onLevel;
        this.required = required;
        this.prerequisite = prerequisite;
        this.alternatives = alternatives;
        this.gradeLevel = gradeLevel;
    }
}