import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe class to track validation statistics
 */
public class ValidationStats {
    private final AtomicLong validNIDsCounter = new AtomicLong(0);
    private final AtomicLong validSmartCardsCounter = new AtomicLong(0);
    private final AtomicLong processedRowsCounter = new AtomicLong(0);

    /**
     * Add to the count of valid NIDs
     */
    public void addValidNIDs(long count) {
        validNIDsCounter.addAndGet(count);
    }

    /**
     * Add to the count of valid Smart Cards
     */
    public void addValidSmartCards(long count) {
        validSmartCardsCounter.addAndGet(count);
    }

    /**
     * Add to the count of processed rows
     */
    public void addProcessedRows(long count) {
        processedRowsCounter.addAndGet(count);
    }

    /**
     * Get the count of valid NIDs
     */
    public long getValidNIDsCount() {
        return validNIDsCounter.get();
    }

    /**
     * Get the count of valid Smart Cards
     */
    public long getValidSmartCardsCount() {
        return validSmartCardsCounter.get();
    }

    /**
     * Get the count of processed rows
     */
    public long getProcessedRowsCount() {
        return processedRowsCounter.get();
    }

    /**
     * Get the percentage of valid NIDs
     */
    public double getValidNIDsPercentage() {
        long total = processedRowsCounter.get();
        return total > 0 ? (validNIDsCounter.get() * 100.0 / total) : 0;
    }

    /**
     * Get the percentage of valid Smart Cards
     */
    public double getValidSmartCardsPercentage() {
        long total = processedRowsCounter.get();
        return total > 0 ? (validSmartCardsCounter.get() * 100.0 / total) : 0;
    }

    /**
     * Generate a summary of the validation statistics
     */
    @Override
    public String toString() {
        long totalRows = processedRowsCounter.get();
        long validNIDs = validNIDsCounter.get();
        long validSmartCards = validSmartCardsCounter.get();

        return String.format(
                "Summary:%n" +
                        "Total records processed: %,d%n" +
                        "Valid NIDs: %,d (%.2f%%)%n" +
                        "Valid Smart Cards: %,d (%.2f%%)",
                totalRows,
                validNIDs,
                getValidNIDsPercentage(),
                validSmartCards,
                getValidSmartCardsPercentage()
        );
    }
}