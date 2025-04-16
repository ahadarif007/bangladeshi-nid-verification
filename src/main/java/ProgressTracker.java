import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks and displays progress of validation process
 */
public class ProgressTracker {
    private final ScheduledExecutorService scheduler;
    private final AtomicLong processedRowsCounter;
    private final long totalRows;
    private final int updateIntervalSeconds;

    /**
     * Create a new progress tracker
     *
     * @param processedRowsCounter Counter for processed rows
     * @param totalRows Total number of rows to process
     * @param updateIntervalSeconds How often to update progress (seconds)
     */
    public ProgressTracker(AtomicLong processedRowsCounter, long totalRows, int updateIntervalSeconds) {
        this.processedRowsCounter = processedRowsCounter;
        this.totalRows = totalRows;
        this.updateIntervalSeconds = updateIntervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Start tracking progress
     */
    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            displayProgress();
        }, updateIntervalSeconds, updateIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Display current progress
     */
    private void displayProgress() {
        long processed = processedRowsCounter.get();
        if (processed > 0) {
            System.out.printf("Progress: %.2f%% (%,d/%,d records processed)%n",
                    (double) processed / totalRows * 100, processed, totalRows);
        }
    }

    /**
     * Stop tracking progress
     */
    public void stop() {
        scheduler.shutdownNow();
        // Show final progress
        displayProgress();
    }
}