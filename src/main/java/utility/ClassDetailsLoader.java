package utility;

import entity.ClassDetail;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class ClassDetailsLoader {

    public static HashMap<String, ClassDetail> loadClassDetails(String filePath) {
        HashMap<String, ClassDetail> classDetailsMap = new HashMap<>();

        try {
            Object object = new JSONParser().parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject) object;

            for (Object key : jsonObject.keySet()) {
                String subject = (String) key;
                JSONObject subjectClasses = (JSONObject) jsonObject.get(subject);

                for (Object classKey : subjectClasses.keySet()) {
                    String className = (String) classKey;
                    JSONObject classDetails = (JSONObject) subjectClasses.get(className);

                    boolean honors = (boolean) classDetails.get("Honors");
                    boolean onLevel = (boolean) classDetails.get("OnLevel");
                    boolean required = (boolean) classDetails.get("Required");

                    JSONArray prereqArray = (JSONArray) classDetails.get("Prerequisite");
                    List<String> prerequisite = new ArrayList<>();
                    for (Object prereq : prereqArray) {
                        prerequisite.add((String) prereq);
                    }

                    JSONArray altArray = (JSONArray) classDetails.get("Alternatives");
                    List<String> alternatives = new ArrayList<>();
                    for (Object alt : altArray) {
                        alternatives.add((String) alt);
                    }

                    JSONArray gradeArray = (JSONArray) classDetails.get("GradeLevel");
                    List<Integer> gradeLevel = new ArrayList<>();
                    for (Object grade : gradeArray) {
                        gradeLevel.add(((Long) grade).intValue());
                    }

                    ClassDetail detail = new ClassDetail(honors, onLevel, required, prerequisite, alternatives, gradeLevel);
                    classDetailsMap.put(className, detail);
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        return classDetailsMap;
    }
}