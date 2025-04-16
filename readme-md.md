# NID Validator

A Java application that validates National ID (NID) and Smart Card numbers using the Luhn checksum algorithm. The program reads from a CSV file, validates the numbers, and updates the validation status fields.

## Features

- Validates Bangladeshi National ID numbers (NID) using the Luhn algorithm
- Validates Smart Card numbers using the Luhn algorithm
- Processes CSV files efficiently
- Updates validation status columns in the output CSV
- Provides a summary of validation results

## Requirements

- Java 11 or higher
- Maven for dependency management and building

## Dependencies

- OpenCSV (5.7.1): For CSV file processing
- Apache Commons Validator (1.7): For Luhn algorithm implementation

## Building the Application

```bash
mvn clean package
```

This will generate a JAR file with all dependencies included in the `target` directory.

## Running the Application

```bash
 java -jar target/nid-validator-1.0-SNAPSHOT-jar-with-dependencies.jar data/input.csv data/output.csv
```

### Example:

```bash
java -jar target/nid-validator-1.0-SNAPSHOT-jar-with-dependencies.jar input_data.csv validated_output.csv
```

## CSV Format

The application expects a CSV file with the following columns:

```
SL, erp_member_nomember_name, nid_smart_card_id, office_code, office_name, project_code, project_name, hasValidNID, hasValidSmartCard
```

Example row:
```
1, 51552998, Ms Eti Moni, 174668582375, Betagi279, BCUP, FALSE, TRUE
```

## Validation Logic

The validation uses the Luhn algorithm (also known as the "modulus 10" algorithm), which is commonly used to validate identification numbers such as credit card numbers, IMEI numbers, and National ID numbers.

For Bangladesh NIDs, the application checks if the number has a valid length (10, 13, or 17 digits) and then applies the Luhn checksum verification.

## Output

The program will:

1. Create a new CSV file with the same data but updated validation status
2. Print a summary of the validation results to the console
