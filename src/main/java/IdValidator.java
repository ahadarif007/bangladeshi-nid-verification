import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator class for NID and Smart Card validation
 */
public class IdValidator {
    private final LuhnCheckDigit luhnValidator;

    public IdValidator() {
        this.luhnValidator = new LuhnCheckDigit();
    }

    /**
     * Validate a National ID number based on specified rules
     *
     * @param nidValue The NID to validate
     * @param dobValue The Date of Birth to check against for 17-digit NIDs
     * @return True if the NID is valid according to the rules
     */
    public boolean validateNID(String nidValue, String dobValue) {
        if (nidValue == null || nidValue.isEmpty()) {
            return false;
        }

        // Remove any non-digit characters for validation
        String nidDigitsOnly = nidValue.replaceAll("[^0-9]", "");

        // Apply the validation logic:
        // 1. If 13 digits: Not checking validity (just return true for 13 digits)
        // 2. If 17 digits: Check first 6 digits against DOB (YYYYMM format)
        // 3. If 10 digits: Apply Luhn algorithm check

        if (nidDigitsOnly.length() == 13) {
            // For 13 digits, we're not checking validity (as specified in the requirements)
            return true;
        } else if (nidDigitsOnly.length() == 17) {
            // For 17 digits, check if first 6 digits match DOB in YYYYMM format
            return validate17DigitNID(nidDigitsOnly, dobValue);
        } else if (nidDigitsOnly.length() == 10) {
            // For 10 digits, apply Luhn algorithm for checksum validation
            return validateWithLuhn(nidDigitsOnly);
        }

        // For any other length, consider it invalid
        return false;
    }

    /**
     * Validate a Smart Card ID using the Luhn algorithm
     *
     * @param smartCardValue The Smart Card ID to validate
     * @return True if the Smart Card ID passes Luhn validation
     */
    public boolean validateSmartCard(String smartCardValue) {
        if (smartCardValue == null || smartCardValue.isEmpty()) {
            return false;
        }

        // Remove any non-digit characters
        String smartCardDigitsOnly = smartCardValue.replaceAll("[^0-9]", "");

        // Validate using Luhn algorithm if digits are present
        if (smartCardDigitsOnly.isEmpty()) {
            return false;
        }

        return validateWithLuhn(smartCardDigitsOnly);
    }

    /**
     * Validate a 17-digit NID by checking first 6 digits against DOB
     */
    private boolean validate17DigitNID(String nidDigitsOnly, String dobValue) {
        if (dobValue == null || dobValue.isEmpty()) {
            return false;
        }

        try {
            // Extract the first 6 digits from NID
            String firstSixDigits = nidDigitsOnly.substring(0, 6);

            // Parse the DOB - handle different possible formats
            String dobDatePart = dobValue.split(" ")[0]; // Remove time part if present
            LocalDate dob;
            try {
                // Try standard format first
                dob = LocalDate.parse(dobDatePart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                // Try alternative format
                dob = LocalDate.parse(dobDatePart, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            // Format DOB as YYYYMM
            String dobYearMonth = dob.format(DateTimeFormatter.ofPattern("yyyyMM"));

            // Check if first 6 digits match DOB
            return firstSixDigits.equals(dobYearMonth);
        } catch (Exception e) {
            // If there's any error parsing the date, assume the NID is invalid
            return false;
        }
    }

    /**
     * Validate a number using the Luhn algorithm
     */
    private boolean validateWithLuhn(String digits) {
        try {
            return luhnValidator.isValid(digits);
        } catch (Exception e) {
            // If there's an error in validation, assume it's invalid
            return false;
        }
    }
}