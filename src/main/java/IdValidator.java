import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

/**
 * Validator class for NID and Smart Card validation
 */
public class IdValidator {
    private final LuhnCheckDigit luhnValidator;
    private final ReferenceDataManager referenceDataManager;

    /**
     * Create a new validator using the singleton reference data manager
     */
    public IdValidator() {
        this.luhnValidator = new LuhnCheckDigit();
        this.referenceDataManager = ReferenceDataManager.getInstance();

        // Ensure reference data is loaded
        if (!referenceDataManager.isDataLoaded()) {
            referenceDataManager.loadData();
        }
    }

    /**
     * Create a new validator with a custom reference data manager
     *
     * @param referenceDataManager The reference data manager to use
     */
    public IdValidator(ReferenceDataManager referenceDataManager) {
        this.luhnValidator = new LuhnCheckDigit();
        this.referenceDataManager = referenceDataManager;
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

        // Apply different validation logic based on NID length
        if (nidDigitsOnly.length() == 10) {
            // For 10 digits, apply Luhn algorithm for checksum validation
            return validateWithLuhn(nidDigitsOnly);
        } else if (nidDigitsOnly.length() == 13 || nidDigitsOnly.length() == 17) {
            // For 13 and 17-digit NIDs, apply Bangladesh-specific validation
            return validateBangladeshNID(nidDigitsOnly, dobValue);
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
     * Validate a Bangladesh NID by checking format and location codes
     */
    private boolean validateBangladeshNID(String nid, String dobValue) {
        try {
            String birthYear = null;

            // Extract birth year from DOB
            if (dobValue != null && !dobValue.isEmpty()) {
                try {
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

                    // Extract just the year
                    birthYear = String.valueOf(dob.getYear());
                } catch (Exception e) {
                    // If there's any error parsing the date, birth year remains null
                }
            }

            // Get reference data from manager
            HashMap<String, Object> districtMap = referenceDataManager.getDistrictMap();
            HashMap<String, Object> rmoMap = referenceDataManager.getRmoMap();
            HashMap<String, Object> upazilaMap = referenceDataManager.getUpazilaMap();
            HashMap<String, Object> unionOrWardMap = referenceDataManager.getUnionOrWardMap();

            return isValidNID(nid, birthYear, districtMap, rmoMap, upazilaMap, unionOrWardMap);
        } catch (Exception e) {
            // If any exception occurs during validation, consider it invalid
            return false;
        }
    }

    /**
     * Main NID validation logic for Bangladesh NIDs
     */
    private boolean isValidNID(
            String nid,
            String birthYear,
            HashMap<String, Object> districtMap,
            HashMap<String, Object> rmoMap,
            HashMap<String, Object> upazilaMap,
            HashMap<String, Object> unionOrWardMap
    ) {
        if (nid == null || birthYear == null) {
            return false;
        }

        if (!(nid.length() == 13 || nid.length() == 17)) {
            return false;
        }

        // If 17 digits, check that it starts with birth year
        if (nid.length() == 17) {
            if (!nid.startsWith(birthYear)) {
                return false;
            }
            nid = nid.substring(4); // strip birth year to get 13-digit core
        }

        try {
            // Make sure it's numeric
            Long.parseLong(nid);

            String districtCode = nid.substring(0, 2);
            String rmoCode = nid.substring(2, 3);

            // Try 2-digit upazila + 2-digit union/ward
            String upazilaCode = nid.substring(3, 5);
            String unionOrWardCode = nid.substring(5, 7);
            String serialNumber = nid.substring(7);

            if (!districtMap.containsKey(districtCode)) return false;
            if (!rmoMap.containsKey(rmoCode)) return false;

            // 2+2
            if (upazilaMap.containsKey(upazilaCode) && unionOrWardMap.containsKey(unionOrWardCode)) {
                return true;
            }

            // Try 3+2
            upazilaCode = nid.substring(3, 6);
            unionOrWardCode = nid.substring(6, 8);
            if (upazilaMap.containsKey(upazilaCode) && unionOrWardMap.containsKey(unionOrWardCode)) {
                return true;
            }

            // Try 2+3
            upazilaCode = nid.substring(3, 5);
            unionOrWardCode = nid.substring(5, 8);
            if (upazilaMap.containsKey(upazilaCode) && unionOrWardMap.containsKey(unionOrWardCode)) {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

        return false;
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