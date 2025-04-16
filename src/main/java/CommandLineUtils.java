/**
 * Utility class for handling command line arguments
 */
public class CommandLineUtils {
    /**
     * Parse command line arguments into a structured object
     *
     * @param args The command line arguments
     * @return A CommandLineArgs object
     */
    public static CommandLineArgs parseArguments(String[] args) {
        CommandLineArgs cliArgs = new CommandLineArgs();

        // Parse input file path (arg 0)
        if (args.length >= 1) {
            cliArgs.setInputFile(args[0]);
        }

        // Parse output file path (arg 1)
        if (args.length >= 2) {
            cliArgs.setOutputFile(args[1]);
        }

        // Parse batch size (arg 2)
        if (args.length >= 3) {
            try {
                int batchSize = Integer.parseInt(args[2]);
                cliArgs.setBatchSize(batchSize);
            } catch (NumberFormatException e) {
                System.err.println("Invalid batch size: " + args[2] + ". Using default.");
            }
        }

        // Parse thread count (arg 3)
        if (args.length >= 4) {
            try {
                int threadCount = Integer.parseInt(args[3]);
                cliArgs.setThreadCount(threadCount);
            } catch (NumberFormatException e) {
                System.err.println("Invalid thread count: " + args[3] + ". Using default.");
            }
        }

        return cliArgs;
    }

    /**
     * Print usage instructions to console
     */
    public static void printUsage() {
        System.out.println("Usage: java NIDValidator [input-csv-file] [output-csv-file] [batch-size] [num-threads]");
        System.out.println("  input-csv-file: Path to input CSV file");
        System.out.println("  output-csv-file: Path to output CSV file");
        System.out.println("  batch-size: Optional. Number of records to process in each batch");
        System.out.println("  num-threads: Optional. Number of parallel threads to use");
    }
}