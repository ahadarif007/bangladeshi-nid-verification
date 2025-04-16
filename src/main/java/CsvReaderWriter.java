import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Handles batch reading and ordered writing of CSV files
 */
public class CsvReaderWriter {
    private final String inputFile;
    private final String outputFile;
    private final int batchSize;

    /**
     * Create a new CSV reader/writer
     *
     * @param inputFile Path to the input CSV file
     * @param outputFile Path to the output CSV file
     * @param batchSize Number of records to process in each batch
     */
    public CsvReaderWriter(String inputFile, String outputFile, int batchSize) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.batchSize = batchSize;
    }

    /**
     * Count the total number of lines in the CSV file
     *
     * @return The total number of lines, excluding the header
     * @throws IOException If there's an error reading the file
     */
    public long countLines() throws IOException {
        long count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            while (reader.readLine() != null) count++;
        }
        // Subtract 1 for the header
        return count > 0 ? count - 1 : 0;
    }

    /**
     * Read the header row from the CSV file
     *
     * @return The header row
     * @throws IOException If there's an error reading the file
     * @throws CsvValidationException If there's an error parsing the CSV
     */
    public String[] readHeader() throws IOException, CsvValidationException {
        try (CSVReader headerReader = new CSVReader(new FileReader(inputFile))) {
            return headerReader.readNext();
        }
    }

    /**
     * Create and write the header to the output file with validation columns
     *
     * @param header The original header row
     * @throws IOException If there's an error writing the file
     */
    public void writeHeader(String[] header) throws IOException {
        // Create a new header with validation columns if they don't exist
        String[] newHeader;
        if (header.length <= CsvConstants.PROJECT_NAME_INDEX + 1) {
            newHeader = new String[header.length + 2];
            System.arraycopy(header, 0, newHeader, 0, header.length);
            newHeader[CsvConstants.HAS_VALID_NID_INDEX] = CsvConstants.HAS_VALID_NID_HEADER;
            newHeader[CsvConstants.HAS_VALID_SMART_CARD_INDEX] = CsvConstants.HAS_VALID_SMART_CARD_HEADER;
        } else {
            newHeader = header;
        }

        // Write the header to the output file
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
            writer.writeNext(newHeader);
        }
    }

    /**
     * Process the file in batches using the provided executor
     *
     * @param executor The executor service to use
     * @param validator The validator to use
     * @param stats The statistics collector
     * @return A blocking queue of futures with the batch results
     * @throws IOException If there's an error reading the file
     * @throws InterruptedException If the batch submission is interrupted
     */
    public BlockingQueue<Future<List<String[]>>> processBatches(
            ExecutorService executor,
            IdValidator validator,
            ValidationStats stats,
            int queueSize) throws IOException, InterruptedException {

        BlockingQueue<Future<List<String[]>>> resultQueue = new LinkedBlockingQueue<>(queueSize);

        // Process the file in batches
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            // Skip header
            br.readLine();

            List<String> batch = new ArrayList<>(batchSize);
            String line;
            int batchId = 0;

            while ((line = br.readLine()) != null) {
                batch.add(line);

                if (batch.size() >= batchSize) {
                    // Submit the batch for processing
                    submitBatch(batch, batchId++, executor, resultQueue, validator, stats);
                    batch = new ArrayList<>(batchSize);
                }
            }

            // Process the final batch if it's not empty
            if (!batch.isEmpty()) {
                submitBatch(batch, batchId, executor, resultQueue, validator, stats);
            }
        } catch (InterruptedException e) {
            // Propagate the interruption and clean up
            Thread.currentThread().interrupt();
            throw e;
        }

        return resultQueue;
    }

    /**
     * Submit a batch for processing
     */
    private void submitBatch(
            List<String> batch,
            int batchId,
            ExecutorService executor,
            BlockingQueue<Future<List<String[]>>> resultQueue,
            IdValidator validator,
            ValidationStats stats) throws InterruptedException {

        // Create a processor for this batch
        CsvBatchProcessor processor = new CsvBatchProcessor(batch, batchId, validator, stats);

        // Submit the batch for processing
        Future<List<String[]>> future = executor.submit(processor);

        // Add the future to the queue
        resultQueue.put(future);
    }

    /**
     * Start a thread that writes results to the output file in batch order
     *
     * @param resultQueue The queue of batch result futures
     * @return The writer thread
     */
    public Thread startResultWriter(BlockingQueue<Future<List<String[]>>> resultQueue) {
        Thread resultWriterThread = new Thread(() -> {
            try {
                int nextBatchToWrite = 0;
                Map<Integer, List<String[]>> completedBatches = new HashMap<>();

                while (true) {
                    try {
                        Future<List<String[]>> future = resultQueue.poll(100, TimeUnit.MILLISECONDS);

                        if (future == null) {
                            // Check if we're done processing
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }
                            continue;
                        }

                        List<String[]> batchResults = future.get();
                        int batchId = Integer.parseInt(batchResults.get(0)[0]);

                        // Remove the batch ID from the first row
                        batchResults.set(0, Arrays.copyOfRange(batchResults.get(0), 1, batchResults.get(0).length));

                        // Store batch for ordered writing
                        completedBatches.put(batchId, batchResults);

                        // Write batches in order
                        while (completedBatches.containsKey(nextBatchToWrite)) {
                            List<String[]> toWrite = completedBatches.remove(nextBatchToWrite);
                            try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile, true))) {
                                writer.writeAll(toWrite);
                            }
                            nextBatchToWrite++;
                        }
                    } catch (Exception e) {
                        if (!(e instanceof InterruptedException)) {
                            System.err.println("Error in result writer thread: " + e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                // Write any remaining batches before exiting
                List<Integer> remainingBatchIds = new ArrayList<>(completedBatches.keySet());
                Collections.sort(remainingBatchIds);

                for (Integer batchId : remainingBatchIds) {
                    List<String[]> toWrite = completedBatches.get(batchId);
                    try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile, true))) {
                        writer.writeAll(toWrite);
                    } catch (IOException e) {
                        System.err.println("Error writing final batches: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Fatal error in result writer thread: " + e.getMessage());
                e.printStackTrace();
            }
        });

        resultWriterThread.start();
        return resultWriterThread;
    }
}