import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * DatabaseManager — saves and loads account data using local files.
 *
 * Storage structure:
 *   atm_database/
 *     accounts.txt          ← account list (accNo|holder|pin|balance)
 *     transactions_123456.txt ← one transaction file per account
 *     transactions_789012.txt
 *     transactions_345678.txt
 */
public class DatabaseManager {

    // Folder where all data files are stored
    private static final String DB_FOLDER    = "atm_database";
    private static final String ACCOUNTS_FILE = DB_FOLDER + "/accounts.txt";
    private static final String SEPARATOR     = "\\|";  // pipe separator in files
    private static final String PIPE          = "|";

    // ════════════════════════════════════════════════════════════
    //  INITIALIZE — create folder + seed demo accounts if new
    // ════════════════════════════════════════════════════════════
    public static void initialize() {
        // Create the database folder if it doesn't exist
        File folder = new File(DB_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
            System.out.println("[DB] Created database folder: " + DB_FOLDER);
        }

        // If accounts file doesn't exist, create it with 3 demo accounts
        File accFile = new File(ACCOUNTS_FILE);
        if (!accFile.exists()) {
            System.out.println("[DB] First run detected — creating demo accounts...");
            createDemoAccounts();
        } else {
            System.out.println("[DB] Database loaded from: " + ACCOUNTS_FILE);
        }
    }

    // Creates 3 demo accounts on first run
    private static void createDemoAccounts() {
        List<String[]> demos = new ArrayList<>();
        demos.add(new String[]{"123456", "Rahul Sharma", "1111", "50000.00"});
        demos.add(new String[]{"789012", "Priya Verma",  "2222", "75000.00"});
        demos.add(new String[]{"345678", "Amit Kumar",   "3333", "30000.00"});

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ACCOUNTS_FILE))) {
            for (String[] acc : demos) {
                // Write: accountNumber|holder|pin|balance
                bw.write(acc[0] + PIPE + acc[1] + PIPE + acc[2] + PIPE + acc[3]);
                bw.newLine();
            }
            System.out.println("[DB] Demo accounts created successfully.");
        } catch (IOException e) {
            System.err.println("[DB] ERROR creating accounts file: " + e.getMessage());
        }

        // Create empty transaction files for each demo account
        for (String[] acc : demos) {
            String txnFile = txnFilePath(acc[0]);
            try {
                new File(txnFile).createNewFile();

                // Write first transaction entry for each account
                String time = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(txnFile))) {
                    bw.write("[" + time + "]  Account opened | Balance: Rs." +
                             String.format("%,.2f", Double.parseDouble(acc[3])));
                    bw.newLine();
                }
            } catch (IOException e) {
                System.err.println("[DB] ERROR creating transaction file: " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  LOAD ALL ACCOUNTS
    // ════════════════════════════════════════════════════════════
    public static Map<String, BankAccount> loadAllAccounts() {
        Map<String, BankAccount> accounts = new HashMap<>();
        File accFile = new File(ACCOUNTS_FILE);
        if (!accFile.exists()) return accounts;

        try (BufferedReader br = new BufferedReader(new FileReader(accFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Parse: accountNumber|holder|pin|balance
                String[] parts = line.split(SEPARATOR);
                if (parts.length < 4) continue;

                String accNo   = parts[0].trim();
                String holder  = parts[1].trim();
                String pin     = parts[2].trim();
                double balance = Double.parseDouble(parts[3].trim());

                // Load this account's transaction history
                List<String> txns = loadTransactions(accNo);

                // Create BankAccount object (without re-logging opening transaction)
                BankAccount account = new BankAccount(accNo, holder, pin, balance, txns);
                accounts.put(accNo, account);
            }
            System.out.println("[DB] Loaded " + accounts.size() + " accounts from database.");
        } catch (IOException e) {
            System.err.println("[DB] ERROR reading accounts: " + e.getMessage());
        }
        return accounts;
    }

    // ════════════════════════════════════════════════════════════
    //  SAVE ACCOUNT (called after every deposit/withdraw)
    // ════════════════════════════════════════════════════════════
    public static void saveAccount(BankAccount account) {
        // Step 1: Read all existing accounts from file
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEPARATOR);
                if (parts.length >= 1 && parts[0].trim().equals(account.getAccountNumber())) {
                    // Replace this line with updated balance
                    lines.add(account.getAccountNumber() + PIPE
                            + account.getAccountHolder()  + PIPE
                            + account.getPin()             + PIPE
                            + String.format("%.2f", account.getBalance()));
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("[DB] ERROR reading accounts for save: " + e.getMessage());
            return;
        }

        // Step 2: Write all accounts back (with the updated one)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ACCOUNTS_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[DB] ERROR saving account: " + e.getMessage());
        }

        // Step 3: Save updated transactions
        saveTransactions(account);

        if (found) {
            System.out.println("[DB] Saved account: " + account.getAccountNumber()
                             + " | Balance: Rs." + String.format("%,.2f", account.getBalance()));
        }
    }

    // ════════════════════════════════════════════════════════════
    //  LOAD TRANSACTIONS for one account
    // ════════════════════════════════════════════════════════════
    public static List<String> loadTransactions(String accountNumber) {
        List<String> txns = new ArrayList<>();
        File file = new File(txnFilePath(accountNumber));
        if (!file.exists()) return txns;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    txns.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("[DB] ERROR reading transactions: " + e.getMessage());
        }
        return txns;
    }

    // ════════════════════════════════════════════════════════════
    //  SAVE TRANSACTIONS for one account
    // ════════════════════════════════════════════════════════════
    public static void saveTransactions(BankAccount account) {
        String filePath = txnFilePath(account.getAccountNumber());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String txn : account.getTransactions()) {
                bw.write(txn);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[DB] ERROR saving transactions: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    //  CHECK if database already has accounts (not first run)
    // ════════════════════════════════════════════════════════════
    public static boolean databaseExists() {
        return new File(ACCOUNTS_FILE).exists();
    }

    // Returns path for a transaction file
    private static String txnFilePath(String accountNumber) {
        return DB_FOLDER + "/transactions_" + accountNumber + ".txt";
    }
}
