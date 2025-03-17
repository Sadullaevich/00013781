import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Main class responsible for:
 * - Running multiple sensor scan tasks concurrently.
 * - Generating periodic warehouse summaries every 5 seconds.
 * - Ensuring proper execution and shutdown of all scanning and reporting tasks.
 */
public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // ScheduledExecutorService to generate periodic summary reports every 5 seconds
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            String summary = Warehouse.generateSummary(); // Retrieve warehouse summary
            System.out.println(summary);
        }, 0, 5, TimeUnit.SECONDS); // Initial delay: 0s, repeats every 5s

        // Thread pool with a maximum of 3 concurrent scanner tasks
        ExecutorService fetchExecutors = Executors.newFixedThreadPool(3);
        List<Future<List<Good>>> futures = new ArrayList<>();

        int numberOfSensors = 10; // Total number of sensor scanners

        // Submitting scan tasks to the thread pool
        for (int i = 1; i < numberOfSensors; i++) {
            futures.add(fetchExecutors.submit(new ScanTask(i))); // Submitting scanning task for execution
        }

        // Processing each future result in a separate daemon thread
        for (Future<List<Good>> future : futures) {
            Thread thread = new Thread(() -> {
                List<Good> items = null;
                try {
                    items = future.get(); // Retrieve scanned data
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    System.out.println("[Error]: Main thread interrupted while fetching future results.");
                } catch (ExecutionException e) {
                    System.out.println("[Error]: A scanning task threw an exception: " + e.getCause());
                }
                Warehouse.addItems(items); // Store scanned goods in the warehouse
            });

            thread.setDaemon(true); // Mark the thread as a daemon to allow program exit if main completes
            thread.start();
        }

        // Initiate shutdown process for scanner executor
        fetchExecutors.shutdown();
        System.out.println("Shutdown scanning executor initiated.");

        // Wait for scanning tasks to complete before proceeding
        try {
            System.out.println("Scanning executor termination await initiated.");
            if (!fetchExecutors.awaitTermination(2, TimeUnit.MINUTES)) {
                fetchExecutors.shutdownNow(); // Force shutdown if tasks take too long
            }
        } catch (InterruptedException e) {
            fetchExecutors.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }

        // Shutdown reporting executor
        scheduler.shutdown();
        System.out.println("Shutdown reporting executor initiated.");

        // Wait for scheduler to terminate before exiting
        try {
            System.out.println("Reporting executor termination await initiated.");
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow(); // Force shutdown if still running
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Final warehouse summary report before shutdown
        System.out.println("[INFO]: " + Warehouse.generateSummary());
        System.out.println("All scanning tasks completed. Program ending.");
    }
}
