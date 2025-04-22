import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Loads geolocation reference data for NID validation from CSV files
 */
public class GeoDataLoader {
    // Default data file paths
    private static final String DEFAULT_DATA_DIR = "geodata/";
    private static final String DEFAULT_RMO_FILE = "code-rmo.csv";
    private static final String DEFAULT_DISTRICT_FILE = "districts.csv";
    private static final String DEFAULT_DIVISION_FILE = "divisions.csv";
    private static final String DEFAULT_UNION_FILE = "unions.csv";
    private static final String DEFAULT_UPAZILA_FILE = "upazilas.csv";

    private String dataDirectory;

    /**
     * Create a new data loader with default data directory
     */
    public GeoDataLoader() {
        this(DEFAULT_DATA_DIR);
    }

    /**
     * Create a new data loader with custom data directory
     *
     * @param dataDirectory The directory containing the data files
     */
    public GeoDataLoader(String dataDirectory) {
        this.dataDirectory = dataDirectory;

        // Ensure directory path ends with a slash
        if (!this.dataDirectory.endsWith("/")) {
            this.dataDirectory += "/";
        }
    }

    /**
     * Load all reference data and return it as a collection of maps
     *
     * @return ReferenceData object containing all loaded maps
     * @throws IOException If there's an error reading the files
     * @throws CsvValidationException If there's an error parsing the CSV data
     */
    public ReferenceData loadAll() throws IOException, CsvValidationException {
        HashMap<String, Object> districtMap = loadDistrictMap();
        HashMap<String, Object> rmoMap = loadRMOMap();
        HashMap<String, Object> upazilaMap = loadUpazilaMap();
        HashMap<String, Object> unionOrWardMap = loadUnionMap();

        return new ReferenceData(districtMap, rmoMap, upazilaMap, unionOrWardMap);
    }

    /**
     * Load district data from CSV
     *
     * @return HashMap with district codes as keys
     * @throws IOException If there's an error reading the file
     * @throws CsvValidationException If there's an error parsing the CSV data
     */
    public HashMap<String, Object> loadDistrictMap() throws IOException, CsvValidationException {
        HashMap<String, Object> districtMap = new HashMap<>();
        String filePath = dataDirectory + DEFAULT_DISTRICT_FILE;

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip header
            String[] header = reader.readNext();
            if (header == null) {
                throw new IOException("Empty district file or missing header");
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 3) {
                    String code = line[0].trim().replaceAll("^\"|\"$", ""); // Remove quotes if present

                    // Ensure code is 2 digits (pad with leading zero if needed)
                    if (code.length() == 1) {
                        code = "0" + code;
                    }

                    DistrictInfo district = new DistrictInfo(
                            code,
                            line[1].trim().replaceAll("^\"|\"$", ""), // division_code
                            line[2].trim().replaceAll("^\"|\"$", ""), // name_en
                            line.length > 3 ? line[3].trim().replaceAll("^\"|\"$", "") : "" // name_bn
                    );
                    districtMap.put(code, district);
                }
            }
        }

        return districtMap;
    }

    /**
     * Load RMO data from CSV
     *
     * @return HashMap with RMO codes as keys
     * @throws IOException If there's an error reading the file
     * @throws CsvValidationException If there's an error parsing the CSV data
     */
    public HashMap<String, Object> loadRMOMap() throws IOException, CsvValidationException {
        HashMap<String, Object> rmoMap = new HashMap<>();
        String filePath = dataDirectory + DEFAULT_RMO_FILE;

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip header
            String[] header = reader.readNext();
            if (header == null) {
                throw new IOException("Empty RMO file or missing header");
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 2) {
                    String code = line[0].trim();
                    RMOInfo rmo = new RMOInfo(
                            code,
                            line[1].trim(), // Area Type
                            line.length > 2 ? line[2].trim() : "" // Source
                    );
                    rmoMap.put(code, rmo);
                }
            }
        }

        return rmoMap;
    }

    /**
     * Load upazila data from CSV
     *
     * @return HashMap with upazila codes as keys
     * @throws IOException If there's an error reading the file
     * @throws CsvValidationException If there's an error parsing the CSV data
     */
    public HashMap<String, Object> loadUpazilaMap() throws IOException, CsvValidationException {
        HashMap<String, Object> upazilaMap = new HashMap<>();
        String filePath = dataDirectory + DEFAULT_UPAZILA_FILE;

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip header
            String[] header = reader.readNext();
            if (header == null) {
                throw new IOException("Empty upazila file or missing header");
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 3) {
                    String code = line[0].trim().replaceAll("^\"|\"$", ""); // Remove quotes if present

                    // Store both 2-digit and 3-digit versions of the code
                    // This handles different NID formats that may use either
                    String twoDigitCode = code;
                    String threeDigitCode = code;

                    // Ensure proper formatting of codes
                    if (code.length() == 1) {
                        twoDigitCode = "0" + code;
                        threeDigitCode = "00" + code;
                    } else if (code.length() == 2) {
                        threeDigitCode = "0" + code;
                    }

                    UpazilaInfo upazila = new UpazilaInfo(
                            code,
                            line[1].trim().replaceAll("^\"|\"$", ""), // district_code
                            line[2].trim().replaceAll("^\"|\"$", ""), // name_en
                            line.length > 3 ? line[3].trim().replaceAll("^\"|\"$", "") : "" // name_bn
                    );

                    // Add both 2-digit and 3-digit versions to the map
                    upazilaMap.put(twoDigitCode, upazila);
                    upazilaMap.put(threeDigitCode, upazila);
                }
            }
        }

        return upazilaMap;
    }

    /**
     * Load union data from CSV
     *
     * @return HashMap with union codes as keys
     * @throws IOException If there's an error reading the file
     * @throws CsvValidationException If there's an error parsing the CSV data
     */
    public HashMap<String, Object> loadUnionMap() throws IOException, CsvValidationException {
        HashMap<String, Object> unionMap = new HashMap<>();
        String filePath = dataDirectory + DEFAULT_UNION_FILE;

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Skip header
            String[] header = reader.readNext();
            if (header == null) {
                throw new IOException("Empty union file or missing header");
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 3) {
                    String code = line[0].trim().replaceAll("^\"|\"$", ""); // Remove quotes if present

                    // Store both 2-digit and 3-digit versions of the code
                    // This handles different NID formats that may use either
                    String twoDigitCode = code;
                    String threeDigitCode = code;

                    // Ensure proper formatting of codes
                    if (code.length() == 1) {
                        twoDigitCode = "0" + code;
                        threeDigitCode = "00" + code;
                    } else if (code.length() == 2) {
                        threeDigitCode = "0" + code;
                    }

                    UnionInfo union = new UnionInfo(
                            code,
                            line[1].trim().replaceAll("^\"|\"$", ""), // upazilla_code
                            line[2].trim().replaceAll("^\"|\"$", ""), // name_en
                            line.length > 3 ? line[3].trim().replaceAll("^\"|\"$", "") : "" // name_bn
                    );

                    // Add both 2-digit and 3-digit versions to the map
                    unionMap.put(twoDigitCode, union);
                    unionMap.put(threeDigitCode, union);
                }
            }
        }

        return unionMap;
    }

    /**
     * Class to hold all reference data
     */
    public static class ReferenceData {
        private final HashMap<String, Object> districtMap;
        private final HashMap<String, Object> rmoMap;
        private final HashMap<String, Object> upazilaMap;
        private final HashMap<String, Object> unionOrWardMap;

        public ReferenceData(
                HashMap<String, Object> districtMap,
                HashMap<String, Object> rmoMap,
                HashMap<String, Object> upazilaMap,
                HashMap<String, Object> unionOrWardMap) {
            this.districtMap = districtMap;
            this.rmoMap = rmoMap;
            this.upazilaMap = upazilaMap;
            this.unionOrWardMap = unionOrWardMap;
        }

        public HashMap<String, Object> getDistrictMap() {
            return districtMap;
        }

        public HashMap<String, Object> getRmoMap() {
            return rmoMap;
        }

        public HashMap<String, Object> getUpazilaMap() {
            return upazilaMap;
        }

        public HashMap<String, Object> getUnionOrWardMap() {
            return unionOrWardMap;
        }
    }

    /**
     * Class to store district information
     */
    public static class DistrictInfo {
        private final String code;
        private final String divisionCode;
        private final String nameEn;
        private final String nameBn;

        public DistrictInfo(String code, String divisionCode, String nameEn, String nameBn) {
            this.code = code;
            this.divisionCode = divisionCode;
            this.nameEn = nameEn;
            this.nameBn = nameBn;
        }

        @Override
        public String toString() {
            return nameEn + " (" + nameBn + ")";
        }
    }

    /**
     * Class to store RMO information
     */
    public static class RMOInfo {
        private final String code;
        private final String areaType;
        private final String source;

        public RMOInfo(String code, String areaType, String source) {
            this.code = code;
            this.areaType = areaType;
            this.source = source;
        }

        @Override
        public String toString() {
            return areaType;
        }
    }

    /**
     * Class to store upazila information
     */
    public static class UpazilaInfo {
        private final String code;
        private final String districtCode;
        private final String nameEn;
        private final String nameBn;

        public UpazilaInfo(String code, String districtCode, String nameEn, String nameBn) {
            this.code = code;
            this.districtCode = districtCode;
            this.nameEn = nameEn;
            this.nameBn = nameBn;
        }

        @Override
        public String toString() {
            return nameEn + " (" + nameBn + ")";
        }
    }

    /**
     * Class to store union information
     */
    public static class UnionInfo {
        private final String code;
        private final String upazilaCode;
        private final String nameEn;
        private final String nameBn;

        public UnionInfo(String code, String upazilaCode, String nameEn, String nameBn) {
            this.code = code;
            this.upazilaCode = upazilaCode;
            this.nameEn = nameEn;
            this.nameBn = nameBn;
        }

        @Override
        public String toString() {
            return nameEn + " (" + nameBn + ")";
        }
    }
}