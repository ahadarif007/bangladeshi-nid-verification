/**
 * Class to hold parsed command line arguments
 */
public class CommandLineArgs {
    private String inputFile;
    private String outputFile;
    private Integer batchSize;
    private Integer threadCount;

    public CommandLineArgs() {
        // Default constructor
    }

    /**
     * Get the input file path or default if not set
     */
    public String getInputFile(String defaultValue) {
        return inputFile != null ? inputFile : defaultValue;
    }

    /**
     * Get the output file path or default if not set
     */
    public String getOutputFile(String defaultValue) {
        return outputFile != null ? outputFile : defaultValue;
    }

    /**
     * Get the batch size or default if not set
     */
    public int getBatchSize(int defaultValue) {
        return batchSize != null ? batchSize : defaultValue;
    }

    /**
     * Get the thread count or default if not set
     */
    public int getThreadCount(int defaultValue) {
        return threadCount != null ? threadCount : defaultValue;
    }

    // Setters
    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}