import java.util.List;
import java.util.concurrent.Callable;

/**
 * ScanTask represents a single scanning operation performed by a warehouse sensor.
 * Implements Callable<List<Good>> to return scanned items.
 */
public class ScanTask implements Callable<List<Good>> {
    int scannerId; // Unique scanner ID

    public ScanTask(int scannerId) {
        this.scannerId = scannerId;
    }

    /**
     * Executes the scanning process for the assigned scanner.
     * Retrieves scanned goods from ScannerClient.
     * @return List of scanned goods.
     */
    @Override
    public List<Good> call() throws Exception {
        String threadName = Thread.currentThread().getName();
        System.out.println("[INFO]: " + threadName + " started scanning (scannerID = " + scannerId + ").");

        List<Good> items = ScannerClient.scan(scannerId); // Simulate scanning process

        // Logging the completion of the scanning process
        System.out.println(
                "[INFO]: " + threadName + " finished scanning (scannerID = " + scannerId + "). Retrieved: " + items.size() + " goods"
        );

        return items;
    }
}
