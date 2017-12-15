package gr.istl.collaborativechat.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Antonis
 */
public class FirebaseDBManager {

    private final static String CREDENTIALS_PATH = "path-to-service-account-credentials-json-file";
    private final static String DB_URL = "your-firebase-database-url";

    private FirebaseDatabase database = null;

    private static FirebaseDBManager instance = null;

    public FirebaseDBManager() {
        init();
    }

    private void init() {
        try {
            FileInputStream serviceAccount = new FileInputStream(CREDENTIALS_PATH);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl(DB_URL).build();
            FirebaseApp defaultApp = FirebaseApp.initializeApp(options);
            database = FirebaseDatabase.getInstance(defaultApp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Credentials File not found.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO error.");
            System.exit(1);
        }
    }

    public static FirebaseDBManager getInstance() {
        if (instance == null) {
            instance = new FirebaseDBManager();
        }

        return instance;
    }

    public DatabaseReference getDBRef(String path) {
        return database.getReference(path);
    }

}
