import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.Instant;

public class App {
    public static void main(String[] args) {
        // reduce SLF4J / Jetty / Spark info noise (only show errors)
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss");

        try {
            // start the web server (Server will open the browser)
            Server.main(args);
        } catch (Throwable t) {
            System.err.println("Internal error: " + t.getMessage());
            t.printStackTrace(); // print to console
            // also append to error.log for debugging
            try (PrintWriter pw = new PrintWriter(new FileWriter("error.log", true))) {
                pw.println("---- " + Instant.now().toString() + " ----");
                t.printStackTrace(pw);
            } catch (IOException ioe) {
                System.err.println("Failed to write error.log: " + ioe.getMessage());
            }
            System.exit(1);
        }
    }
}