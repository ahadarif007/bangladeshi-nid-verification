import java.util.concurrent.ExecutionException;
import java.io.IOException;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Main class for the NID Validator application.
 * Processes CSV files containing National ID and Smart Card information.
 */
public class NIDValidator {
    // Default file paths
    private static final String DEFAULT_INPUT_FILE = "data/nid_input.csv";
    private static final String DEFAULT_OUTPUT_FILE = "data/nid_output.csv";

    // Default processing parameters
    private static final int DEFAULT_BATCH_SIZE = 100_000;
    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        // Parse command line arguments
        CommandLineArgs cliArgs = CommandLineUtils.parseArguments(args);

        // Set up configuration
        String inputFile = cliArgs.getInputFile(DEFAULT_INPUT_FILE);
        String outputFile = cliArgs.getOutputFile(DEFAULT_OUTPUT_FILE);
        int batchSize = cliArgs.getBatchSize(DEFAULT_BATCH_SIZE);
        int numThreads = cliArgs.getThreadCount(DEFAULT_THREAD_COUNT);

        System.out.println("Using input file: " + inputFile);
        System.out.println("Using output file: " + outputFile);

        try {
            // Execute the validation process
            long startTime = System.currentTimeMillis();

            // Create the validator service and run validation
            ValidationService validationService = new ValidationService(
                    inputFile,
                    outputFile,
                    batchSize,
                    numThreads,
                    true); // Enable progress tracking

            validationService.validate();

            // Log execution time
            long endTime = System.currentTimeMillis();
            double elapsedSeconds = (endTime - startTime) / 1000.0;

            System.out.println("Validation completed successfully in " + elapsedSeconds + " seconds.");
            System.out.println("Results saved to " + outputFile);
        } catch (IOException e) {
            System.err.println("File I/O error: " + e.getMessage());
            e.printStackTrace();
        } catch (CsvValidationException e) {
            System.err.println("CSV format error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Processing interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("Execution error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}