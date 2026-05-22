package utility;

import entity.Items.EquipmentSlot;
import entity.Items.Piercing;
import entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Piercing generation and descriptions")
class PiercingDescriptionTest {

    @Test
    @DisplayName("Should pair a one-sided gauge with a matching opposite-ear gauge")
    void testGaugePairingReconcilesMismatchedEars() {
        GameRandom.reset();
        GameRandom.initialize(1L);

        Student student = new Student();
        student.getStudentHead().equip(new Piercing("hoops", "white gold", null,
                EquipmentSlot.LEFT_EAR, "small"));
        student.getStudentHead().equip(new Piercing("gauges", "titanium", null,
                EquipmentSlot.RIGHT_EAR, "small"));
        student.getStudentHead().equip(new Piercing("hoops", "gold", null,
                EquipmentSlot.RIGHT_EAR, "small"));

        StudentPopGenerator.ensureGaugePairs(student);

        assertTrue(student.getStudentHead().getEquippedList(EquipmentSlot.LEFT_EAR)
                .stream().anyMatch(item -> item.getName().contains("gauge")));
        assertTrue(student.getStudentHead().getEquippedList(EquipmentSlot.RIGHT_EAR)
                .stream().anyMatch(item -> item.getName().contains("gauge")));
        assertEquals(2, student.studentStatistics.getEarPiercingLeftCount());
        assertEquals(2, student.studentStatistics.getEarPiercingRightCount());
    }

    @Test
    @DisplayName("Should singularize a single gauge in mixed-ear descriptions")
    void testMixedPiercingDescriptionSingularizesSingleGauge() throws Exception {
        Student student = new Student();
        student.getStudentHead().equip(new Piercing("hoops", "white gold", null,
                EquipmentSlot.LEFT_EAR, "small"));
        student.getStudentHead().equip(new Piercing("gauges", "titanium", null,
                EquipmentSlot.RIGHT_EAR, "small"));
        student.getStudentHead().equip(new Piercing("hoops", "gold", null,
                EquipmentSlot.RIGHT_EAR, "small"));

        String description = buildHeadPiercingDescription(student);

        assertTrue(description.contains("a small, titanium gauge"));
        assertFalse(description.contains("a small, titanium gauges"));
    }

    private String buildHeadPiercingDescription(Student student) throws Exception {
        Method method = Inspector.class.getDeclaredMethod(
                "buildHeadPiercingDescription", Student.class);
        method.setAccessible(true);
        return (String) method.invoke(null, student);
    }
}
