/**
 * Constants for CSV column indices and headers
 */
public class CsvConstants {
    // Column indices based on the actual CSV structure
    public static final int ERP_MEM_NO_INDEX = 0;
    public static final int MEMBER_NAME_INDEX = 1;
    public static final int DOB_INDEX = 2;
    public static final int NID_INDEX = 3;
    public static final int SMART_CARD_INDEX = 4;
    public static final int MEMBERSHIP_DATE_INDEX = 5;
    public static final int OFFICE_CODE_INDEX = 6;
    public static final int OFFICE_NAME_INDEX = 7;
    public static final int PROJECT_CODE_INDEX = 8;
    public static final int PROJECT_NAME_INDEX = 9;

    // Additional validation result columns
    public static final int HAS_VALID_NID_INDEX = 10;
    public static final int HAS_VALID_SMART_CARD_INDEX = 11;

    // Column header names
    public static final String HAS_VALID_NID_HEADER = "Has Valid NID";
    public static final String HAS_VALID_SMART_CARD_HEADER = "Has Valid Smart Card";
}