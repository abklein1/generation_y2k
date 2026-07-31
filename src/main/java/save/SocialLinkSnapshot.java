package save;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialLinkSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<EdgeSnapshot> edges = new ArrayList<>();
    private final Map<String, String> catalysts = new HashMap<>();
    // Directed romance records ("sourceId>targetId" -> RomanticStatus name).
    // Null on snapshots serialized before romance existed; accessors guard.
    private final Map<String, String> romance = new HashMap<>();

    public void addEdge(int sourceStudentId, int targetStudentId, double weight) {
        edges.add(new EdgeSnapshot(sourceStudentId, targetStudentId, weight));
    }

    public List<EdgeSnapshot> getEdges() {
        return new ArrayList<>(edges);
    }

    public void putCatalysts(Map<String, String> catalystRecords) {
        catalysts.clear();
        if (catalystRecords != null) {
            catalysts.putAll(catalystRecords);
        }
    }

    public Map<String, String> getCatalysts() {
        return new HashMap<>(catalysts);
    }

    public void putRomance(Map<String, String> romanceRecords) {
        romance.clear();
        if (romanceRecords != null) {
            romance.putAll(romanceRecords);
        }
    }

    public Map<String, String> getRomance() {
        // Field is null when deserializing a pre-romance save
        return romance == null ? new HashMap<>() : new HashMap<>(romance);
    }

    public static class EdgeSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int sourceStudentId;
        private final int targetStudentId;
        private final double weight;

        EdgeSnapshot(int sourceStudentId, int targetStudentId, double weight) {
            this.sourceStudentId = sourceStudentId;
            this.targetStudentId = targetStudentId;
            this.weight = weight;
        }

        public int getSourceStudentId() {
            return sourceStudentId;
        }

        public int getTargetStudentId() {
            return targetStudentId;
        }

        public double getWeight() {
            return weight;
        }
    }
}
