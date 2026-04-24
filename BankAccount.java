import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BankAccount {

    private String       accountNumber;
    private String       accountHolder;
    private String       pin;
    private double       balance;
    private List<String> transactions;

    // ── Constructor for NEW account (first run) ──────────────────
    public BankAccount(String accNo, String holder, String pin, double balance) {
        this.accountNumber = accNo;
        this.accountHolder = holder;
        this.pin           = pin;
        this.balance       = balance;
        this.transactions  = new ArrayList<>();
        // Log the opening transaction
        addTransaction("Account opened | Balance: Rs." + fmt(balance));
    }

    // ── Constructor for LOADING from database (existing account) ─
    // Does NOT add an opening transaction — it's already saved in DB
    public BankAccount(String accNo, String holder, String pin,
                       double balance, List<String> savedTransactions) {
        this.accountNumber = accNo;
        this.accountHolder = holder;
        this.pin           = pin;
        this.balance       = balance;
        this.transactions  = new ArrayList<>(savedTransactions);
    }

    // ── PIN Validation ───────────────────────────────────────────
    public boolean validatePin(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    // ── Deposit ──────────────────────────────────────────────────
    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance += amount;
        addTransaction("DEPOSIT    +Rs." + fmt(amount)
                     + "   | New Balance: Rs." + fmt(balance));
        return true;
    }

    // ── Withdraw ─────────────────────────────────────────────────
    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        addTransaction("WITHDRAW   -Rs." + fmt(amount)
                     + "   | New Balance: Rs." + fmt(balance));
        return true;
    }

    // ── Getters ──────────────────────────────────────────────────
    public double       getBalance()       { return balance; }
    public String       getAccountNumber() { return accountNumber; }
    public String       getAccountHolder() { return accountHolder; }
    public String       getPin()           { return pin; }
    public List<String> getTransactions()  { return transactions; }

    // ── Private Helpers ──────────────────────────────────────────
    private void addTransaction(String detail) {
        String time = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        transactions.add("[" + time + "]  " + detail);
    }

    private String fmt(double val) {
        return String.format("%,.2f", val);
    }
}
