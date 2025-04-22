import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Singleton class to manage NID validation reference data
 */
public class ReferenceDataManager {
    private static ReferenceDataManager instance;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private HashMap<String, Object> districtMap;
    private HashMap<String, Object> rmoMap;
    private HashMap<String, Object> upazilaMap;
    private HashMap<String, Object> unionOrWardMap;

    private boolean dataLoaded = false;

    // Private constructor for singleton pattern
    private ReferenceDataManager() {
        districtMap = new HashMap<>();
        rmoMap = new HashMap<>();
        upazilaMap = new HashMap<>();
        unionOrWardMap = new HashMap<>();
    }

    /**
     * Get the singleton instance
     */
    public static synchronized ReferenceDataManager getInstance() {
        if (instance == null) {
            instance = new ReferenceDataManager();
        }
        return instance;
    }

    /**
     * Load reference data from default location
     *
     * @return True if data loaded successfully
     */
    public boolean loadData() {
        return loadData(new GeoDataLoader());
    }

    /**
     * Load reference data from specified directory
     *
     * @param dataDirectory Directory containing CSV files
     * @return True if data loaded successfully
     */
    public boolean loadData(String dataDirectory) {
        return loadData(new GeoDataLoader(dataDirectory));
    }

    /**
     * Load reference data using the provided loader
     *
     * @param loader The loader to use
     * @return True if data loaded successfully
     */
    public boolean loadData(GeoDataLoader loader) {
        try {
            // Acquire write lock for thread safety
            lock.writeLock().lock();

            GeoDataLoader.ReferenceData refData = loader.loadAll();

            this.districtMap = refData.getDistrictMap();
            this.rmoMap = refData.getRmoMap();
            this.upazilaMap = refData.getUpazilaMap();
            this.unionOrWardMap = refData.getUnionOrWardMap();

            this.dataLoaded = true;
            return true;

        } catch (IOException | CsvValidationException e) {
            System.err.println("Error loading reference data: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get the district map
     */
    public HashMap<String, Object> getDistrictMap() {
        lock.readLock().lock();
        try {
            return new HashMap<>(districtMap);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the RMO map
     */
    public HashMap<String, Object> getRmoMap() {
        lock.readLock().lock();
        try {
            return new HashMap<>(rmoMap);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the upazila map
     */
    public HashMap<String, Object> getUpazilaMap() {
        lock.readLock().lock();
        try {
            return new HashMap<>(upazilaMap);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the union/ward map
     */
    public HashMap<String, Object> getUnionOrWardMap() {
        lock.readLock().lock();
        try {
            return new HashMap<>(unionOrWardMap);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Check if reference data has been loaded
     */
    public boolean isDataLoaded() {
        lock.readLock().lock();
        try {
            return dataLoaded;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get summary statistics about the loaded data
     */
    public String getDataSummary() {
        lock.readLock().lock();
        try {
            if (!dataLoaded) {
                return "Reference data not loaded";
            }

            return String.format(
                    "Reference Data Summary:%n" +
                            "Districts: %d%n" +
                            "RMO Types: %d%n" +
                            "Upazilas: %d%n" +
                            "Unions/Wards: %d",
                    districtMap.size(),
                    rmoMap.size(),
                    upazilaMap.size() / 2, // Divide by 2 because we store both 2-digit and 3-digit codes
                    unionOrWardMap.size() / 2 // Divide by 2 because we store both 2-digit and 3-digit codes
            );

        } finally {
            lock.readLock().unlock();
        }
    }
}