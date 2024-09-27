package utility;

import entity.ClassDetail;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
                    for (Object classKey2 : classDetails.keySet()) {
                        String className2 = (String) classKey2;
                        JSONObject classDetails2 = (JSONObject) classDetails.get(className2);
                        boolean honors = (boolean) classDetails2.get("Honors");
                        boolean onLevel = (boolean) classDetails2.get("OnLevel");
                        boolean required = (boolean) classDetails2.get("Required");

                        JSONArray prereqArray = (JSONArray) classDetails2.get("Prerequisite");
                        List<String> prerequisite = new ArrayList<>();
                        if (!(prereqArray == null)) {
                            for (Object prereq : prereqArray) {
                                prerequisite.add((String) prereq);
                            }
                        }

                        JSONArray altArray = (JSONArray) classDetails2.get("Alternatives");
                        List<String> alternatives = new ArrayList<>();
                        if (!(altArray == null)) {
                            for (Object alt : altArray) {
                                alternatives.add((String) alt);
                            }
                        }

                        JSONArray gradeArray = (JSONArray) classDetails2.get("Grade Level");
                        List<Integer> gradeLevel = new ArrayList<>();
                        if (!(gradeArray == null)) {
                            for (Object grade : gradeArray) {
                                gradeLevel.add(((Long) grade).intValue());
                            }
                        }
                        ClassDetail detail = new ClassDetail(className, honors, onLevel, required, prerequisite, alternatives, gradeLevel);
                        classDetailsMap.put(className2, detail);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        return classDetailsMap;
    }
}