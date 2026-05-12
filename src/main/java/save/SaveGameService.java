package save;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveGameService {

    private SaveGameService() {
    }

    public static Path getDefaultSaveDirectory() {
        return Paths.get(System.getProperty("user.home"), "generation_y2k", "saves");
    }

    public static void save(SaveGameData data, Path savePath) throws IOException {
        Path parent = savePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(savePath))) {
            out.writeObject(data);
        }
    }

    public static SaveGameData load(Path savePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(savePath))) {
            Object loaded = in.readObject();
            if (!(loaded instanceof SaveGameData saveData)) {
                throw new IOException("Selected file is not a save game.");
            }
            if (saveData.getFormatVersion() != SaveGameData.FORMAT_VERSION) {
                throw new IOException("Unsupported save format version: " + saveData.getFormatVersion());
            }
            return saveData;
        }
    }
}
