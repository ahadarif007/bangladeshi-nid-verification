import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;

/**
 * Processes a batch of CSV lines in parallel
 */
public class CsvBatchProcessor implements Callable<List<String[]>> {
    private final List<String> lines;
    private final int batchId;
    private final IdValidator validator;
    private final ValidationStats stats;

    /**
     * Create a new batch processor
     *
     * @param lines The CSV lines to process
     * @param batchId The ID of this batch
     * @param validator The validator to use
     * @param stats The statistics collector
     */
    public CsvBatchProcessor(List<String> lines, int batchId, IdValidator validator, ValidationStats stats) {
        this.lines = lines;
        this.batchId = batchId;
        this.validator = validator;
        this.stats = stats;
    }

    @Override
    public List<String[]> call() throws Exception {
        List<String[]> results = new ArrayList<>(lines.size());
        CSVParser parser = new CSVParserBuilder().build();

        int batchValidNIDs = 0;
        int batchValidSmartCards = 0;

        // Process each line in the batch
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            try {
                // Parse the CSV line
                String[] row = parser.parseLine(line);

                // For the first row, add the batch ID
                if (i == 0) {
                    String[] rowWithId = new String[row.length + 1];
                    rowWithId[0] = String.valueOf(batchId);
                    System.arraycopy(row, 0, rowWithId, 1, row.length);
                    row = rowWithId;
                }

                // Ensure the row has enough columns for validation results
                String[] newRow;
                // Create new row with space for validation results
                newRow = new String[row.length + 2];
                System.arraycopy(row, 0, newRow, 0, row.length);
                row = newRow;

                int adjustedIndex = (i == 0) ? 1 : 0; // Adjust for batch ID in first row

                // Get values for validation
                String nidValue = "";
                String smartCardValue = "";
                String dobValue = "";

                if (row.length > CsvConstants.NID_INDEX + adjustedIndex) {
                    nidValue = row[CsvConstants.NID_INDEX + adjustedIndex].trim();
                }

                if (row.length > CsvConstants.SMART_CARD_INDEX + adjustedIndex) {
                    smartCardValue = row[CsvConstants.SMART_CARD_INDEX + adjustedIndex].trim();
                }

                if (row.length > CsvConstants.DOB_INDEX + adjustedIndex) {
                    dobValue = row[CsvConstants.DOB_INDEX + adjustedIndex].trim();
                }

                // Validate NID
                boolean isNidValid = validator.validateNID(nidValue, dobValue);
                if (isNidValid) batchValidNIDs++;

                // Validate Smart Card
                boolean isSmartCardValid = validator.validateSmartCard(smartCardValue);
                if (isSmartCardValid) batchValidSmartCards++;

                // Update the validation status columns
                row[CsvConstants.HAS_VALID_NID_INDEX + adjustedIndex] = String.valueOf(isNidValid).toUpperCase();
                row[CsvConstants.HAS_VALID_SMART_CARD_INDEX + adjustedIndex] = String.valueOf(isSmartCardValid).toUpperCase();

                // Add to results
                results.add(row);
            } catch (Exception e) {
                // Handle parsing errors for individual rows
                System.err.println("Error processing row: " + e.getMessage());
                // Add a dummy row with error information
                String[] errorRow = new String[12];
                errorRow[0] = (i == 0) ? String.valueOf(batchId) : "";
                errorRow[1] = "ERROR";
                errorRow[11] = "Parse error: " + e.getMessage();
                results.add(errorRow);
            }
        }

        // Update statistics
        stats.addValidNIDs(batchValidNIDs);
        stats.addValidSmartCards(batchValidSmartCards);
        stats.addProcessedRows(lines.size());

        return results;
    }
}