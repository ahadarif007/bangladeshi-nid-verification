import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Main service for coordinating the validation process
 */
public class ValidationService {
    private final String inputFile;
    private final String outputFile;
    private final int batchSize;
    private final int numThreads;
    private final boolean enableProgressTracking;
    private final int queueSize = 10;

    /**
     * Create a new validation service
     */
    public ValidationService(String inputFile, String outputFile, int batchSize, int numThreads, boolean enableProgressTracking) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.batchSize = batchSize;
        this.numThreads = numThreads;
        this.enableProgressTracking = enableProgressTracking;
    }

    /**
     * Execute the validation process
     */
    public void validate() throws IOException, CsvValidationException, InterruptedException, ExecutionException {
        System.out.println("Starting validation with " + numThreads + " threads and batch size of " + batchSize);

        // Create components
        CsvReaderWriter csvReaderWriter = new CsvReaderWriter(inputFile, outputFile, batchSize);
        IdValidator validator = new IdValidator();
        ValidationStats stats = new ValidationStats();
        AtomicLong processedRowsCounter = new AtomicLong(0);

        // Set up thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Set up progress tracking
        ProgressTracker progressTracker = null;
        if (enableProgressTracking) {
            // Count total lines for progress tracking
            long totalRows = csvReaderWriter.countLines();
            System.out.println("Total records to process: " + totalRows);

            // Create and start progress tracker
            progressTracker = new ProgressTracker(processedRowsCounter, totalRows, 2);
            progressTracker.start();
        }

        try {
            // Read and write header
            String[] header = csvReaderWriter.readHeader();
            if (header == null) {
                throw new IOException("Empty CSV file or missing header");
            }
            csvReaderWriter.writeHeader(header);

            // Process data in batches
            BlockingQueue<Future<List<String[]>>> resultQueue = csvReaderWriter.processBatches(
                    executorService, validator, stats, queueSize);

            // Start result writer
            Thread resultWriterThread = csvReaderWriter.startResultWriter(resultQueue);

            // Shutdown executor service and wait for completion
            executorService.shutdown();
            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                System.err.println("Processing did not complete within the timeout period.");
                executorService.shutdownNow();
            }

            // Signal writer thread to complete and wait for it
            resultWriterThread.interrupt();
            resultWriterThread.join();

            // Print validation summary
            System.out.println(stats.toString());

        } finally {
            // Ensure resources are closed properly
            executorService.shutdownNow();
            if (progressTracker != null) {
                progressTracker.stop();
            }
        }
    }
}