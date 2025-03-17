import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Warehouse class stores scanned goods and ensures no duplicate items are recorded.
 * Uses locking mechanism to ensure thread safety when modifying shared data.
 */
public class Warehouse {
    static List<Good> goods = new ArrayList<>(); // List of scanned goods
    static Set<String> ids = new HashSet<>(); // Set of unique good IDs to prevent duplicates

    static Lock lock = new ReentrantLock(); // Lock for synchronizing access to shared data

    /**
     * Generates a summary report showing the total number of unique goods scanned.
     * @return Summary message as a string.
     */
    public static String generateSummary() {
        lock.lock(); // Acquire lock before accessing shared data
        try {
            String threadName = Thread.currentThread().getName();
            return "[INFO]: Total items so far: " + goods.size() + " (Thread: " + threadName + ")";
        } finally {
            lock.unlock(); // Always release lock after reading data
        }
    }

    /**
     * Adds a list of scanned items to the warehouse while ensuring no duplicates are stored.
     * Uses a locking mechanism to prevent race conditions.
     * @param items List of scanned goods.
     */
    public static void addItems(List<Good> items) {
        lock.lock(); // Acquire lock before modifying shared data
        try {
            for (Good good : items) {
                String goodId = good.getItemId();
                boolean goodRegistered = ids.contains(goodId);

                if (!goodRegistered) { // If item is unique, store it
                    ids.add(good.getItemId());
                    goods.add(good);
                } else {
                    // Log duplicate detection
                    System.out.println("[Warning]: Duplicate goods found! ID: " + goodId);
                }
            }
        } finally {
            lock.unlock(); // Release lock to allow other threads access
        }
    }
}
