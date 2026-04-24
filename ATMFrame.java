import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ATMFrame extends JFrame {

    // ── Card Names ───────────────────────────────────────────────
    private static final String WELCOME   = "WELCOME";
    private static final String LOGIN     = "LOGIN";
    private static final String MENU      = "MENU";
    private static final String BALANCE   = "BALANCE";
    private static final String DEPOSIT   = "DEPOSIT";
    private static final String WITHDRAW  = "WITHDRAW";
    private static final String STATEMENT = "STATEMENT";

    // ── Color Theme ──────────────────────────────────────────────
    private static final Color BG     = new Color(15,  23,  42);
    private static final Color CARD   = new Color(30,  41,  59);
    private static final Color ACCENT = new Color(56,  189, 248);
    private static final Color GREEN  = new Color(34,  197, 94);
    private static final Color RED    = new Color(239, 68,  68);
    private static final Color YELLOW = new Color(250, 204, 21);
    private static final Color WHITE  = new Color(241, 245, 249);
    private static final Color GRAY   = new Color(148, 163, 184);

    // ── State ────────────────────────────────────────────────────
    private CardLayout  cardLayout;
    private JPanel      mainPanel;
    private Map<String, BankAccount> db;
    private BankAccount account;

    // ── Shared UI refs ───────────────────────────────────────────
    private JTextField     accField;
    private JPasswordField pinField;
    private JLabel         loginErr;
    private JLabel         greetingLbl;
    private JLabel         balAmtLbl;
    private JLabel         balAccLbl;
    private JTextArea      stmtArea;

    // ════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════
    public ATMFrame() {
        // Load accounts from database files
        db = DatabaseManager.loadAllAccounts();
        setupFrame();
        buildCards();
        navigate(WELCOME);
    }

    private void setupFrame() {
        setTitle("Java Bank ATM");
        setSize(500, 660);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG);
    }

    private void buildCards() {
        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(BG);
        mainPanel.add(welcomeScreen(),   WELCOME);
        mainPanel.add(loginScreen(),     LOGIN);
        mainPanel.add(menuScreen(),      MENU);
        mainPanel.add(balanceScreen(),   BALANCE);
        mainPanel.add(depositScreen(),   DEPOSIT);
        mainPanel.add(withdrawScreen(),  WITHDRAW);
        mainPanel.add(statementScreen(), STATEMENT);
        add(mainPanel);
    }

    // Navigate + auto-refresh each screen's data
    private void navigate(String card) {
        switch (card) {
            case MENU:      refreshGreeting();  break;
            case BALANCE:   refreshBalance();   break;
            case STATEMENT: refreshStatement(); break;
        }
        cardLayout.show(mainPanel, card);
    }

    // ════════════════════════════════════════════════════════════
    //  SCREEN 1 — WELCOME
    // ════════════════════════════════════════════════════════════
    private JPanel welcomeScreen() {
        JPanel p = bgPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        JLabel icon = new JLabel("🏦", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        g.gridy = 0; g.insets = ins(0, 0, 8, 0);
        p.add(icon, g);

        g.gridy = 1; g.insets = ins(0, 0, 4, 0);
        p.add(lbl("JAVA BANK", 32, Font.BOLD, ACCENT), g);

        g.gridy = 2; g.insets = ins(0, 0, 6, 0);
        p.add(lbl("ATM Simulation with Persistent Database", 12, Font.PLAIN, GRAY), g);

        // DB status indicator
        JLabel dbStatus = lbl("  Database Connected — Data saves automatically", 11, Font.PLAIN, GREEN);
        g.gridy = 3; g.insets = ins(0, 30, 40, 30);
        p.add(dbStatus, g);

        JButton btn = bigBtn("  INSERT CARD ", ACCENT);
        btn.addActionListener(e -> navigate(LOGIN));
        g.gridy = 4; g.insets = ins(0, 80, 0, 80);
        p.add(btn, g);

        // Credentials hint box
        // JPanel hint = new JPanel(new GridLayout(4, 1, 2, 2));
        // hint.setBackground(CARD);
        // hint.setBorder(new EmptyBorder(12, 20, 12, 20));
        // hint.add(lbl("── Demo Credentials ──", 11, Font.BOLD, GRAY));
        // hint.add(lbl("A/C: 123456   PIN: 1111   Rahul Sharma", 11, Font.PLAIN, GRAY));
        // hint.add(lbl("A/C: 789012   PIN: 2222   Priya Verma",  11, Font.PLAIN, GRAY));
        // hint.add(lbl("A/C: 345678   PIN: 3333   Amit Kumar",   11, Font.PLAIN, GRAY));
        // g.gridy = 5; g.insets = ins(28, 40, 0, 40);
        // p.add(hint, g);

        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  SCREEN 2 — LOGIN
    // ════════════════════════════════════════════════════════════
    private JPanel loginScreen() {
        JPanel outer = bgPanel(new BorderLayout());
        outer.add(topBar("  Account Login"), BorderLayout.NORTH);

        JPanel form = bgPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        JLabel icon = new JLabel("", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        g.gridy = 0; g.insets = ins(24, 0, 24, 0);
        form.add(icon, g);

        form.add(fieldLbl("Account Number"), row(g, 1, ins(4, 70, 2, 70)));
        accField = inputField();
        form.add(accField, row(g, 2, ins(0, 70, 14, 70)));

        form.add(fieldLbl("4-Digit PIN"), row(g, 3, ins(4, 70, 2, 70)));
        pinField = new JPasswordField();
        styleInput(pinField);
        form.add(pinField, row(g, 4, ins(0, 70, 6, 70)));

        loginErr = lbl("", 12, Font.BOLD, RED);
        form.add(loginErr, row(g, 5, ins(4, 0, 4, 0)));

        JButton loginBtn = bigBtn("LOGIN  →", GREEN);
        loginBtn.addActionListener(e -> doLogin());
        form.add(loginBtn, row(g, 6, ins(12, 70, 8, 70)));

        JButton back = bigBtn("←  BACK", CARD);
        back.setBorder(BorderFactory.createLineBorder(GRAY, 1));
        back.addActionListener(e -> { clearLogin(); navigate(WELCOME); });
        form.add(back, row(g, 7, ins(0, 70, 20, 70)));

        pinField.addActionListener(e -> doLogin()); // Enter key submits

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private void doLogin() {
        String acc = accField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (acc.isEmpty() || pin.isEmpty()) {
            loginErr.setText("  Please fill in all fields!");
            return;
        }
        BankAccount found = db.get(acc);
        if (found == null || !found.validatePin(pin)) {
            loginErr.setText("  Invalid account number or PIN!");
            pinField.setText("");
            return;
        }
        account = found;
        clearLogin();
        navigate(MENU);
    }

    private void clearLogin() {
        accField.setText("");
        pinField.setText("");
        loginErr.setText("");
    }

    // ════════════════════════════════════════════════════════════
    //  SCREEN 3 — MAIN MENU
    // ════════════════════════════════════════════════════════════
    private JPanel menuScreen() {
        JPanel outer = bgPanel(new BorderLayout());
        outer.add(topBar("  Main Menu"), BorderLayout.NORTH);

        JPanel body = bgPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        greetingLbl = lbl("Welcome!", 18, Font.BOLD, YELLOW);
        g.gridy = 0; g.insets = ins(10, 0, 6, 0);
        body.add(greetingLbl, g);

        // DB save reminder label
        JLabel saved = lbl("  All transactions are saved to database", 11, Font.PLAIN, GREEN);
        g.gridy = 1; g.insets = ins(0, 0, 24, 0);
        body.add(saved, g);

        String[][] opts = {
            { "   Check Balance",  BALANCE   },
            { "    Deposit Money",  DEPOSIT   },
            { "    Withdraw Money", WITHDRAW  },
            { "   Mini Statement", STATEMENT },
        };
        for (int i = 0; i < opts.length; i++) {
            JButton btn = menuBtn(opts[i][0]);
            String dest = opts[i][1];
            btn.addActionListener(e -> navigate(dest));
            g.gridy = i + 2;
            g.insets = ins(5, 60, 5, 60);
            body.add(btn, g);
        }

        JButton logout = bigBtn("  LOGOUT", RED);
        logout.addActionListener(e -> { account = null; navigate(WELCOME); });
        g.gridy = 6; g.insets = ins(24, 60, 10, 60);
        body.add(logout, g);

        outer.add(body, BorderLayout.CENTER);
        return outer;
    }

    private void refreshGreeting() {
        if (greetingLbl != null && account != null)
            greetingLbl.setText("Welcome,  " + account.getAccountHolder() + "!");
    }

    // ════════════════════════════════════════════════════════════
    //  SCREEN 4 — BALANCE
    // ════════════════════════════════════════════════════════════
    private JPanel balanceScreen() {
        JPanel outer = bgPanel(new BorderLayout());
        outer.add(topBar("  Balance Enquiry"), BorderLayout.NORTH);

        JPanel body = bgPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        JLabel icon = new JLabel(" ", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        g.gridy = 0; g.insets = ins(30, 0, 10, 0);
        body.add(icon, g);

        body.add(lbl("Available Balance", 14, Font.PLAIN, GRAY),
                 row(g, 1, ins(0, 0, 10, 0)));

        balAmtLbl = lbl("Rs. 0.00", 40, Font.BOLD, GREEN);
        body.add(balAmtLbl, row(g, 2, ins(0, 0, 8, 0)));

        balAccLbl = lbl("", 12, Font.PLAIN, GRAY);
        body.add(balAccLbl, row(g, 3, ins(0, 0, 40, 0)));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 6));
        info.setBackground(CARD);
        info.setBorder(new EmptyBorder(12, 16, 12, 16));
        info.add(lbl("  Balance loaded from database", 11, Font.PLAIN, GRAY));
        info.add(lbl("  Your data is saved automatically", 11, Font.PLAIN, GRAY));
        g.gridy = 4; g.insets = ins(0, 50, 40, 50);
        body.add(info, g);

        JButton back = bigBtn("←  BACK TO MENU", ACCENT);
        back.addActionListener(e -> navigate(MENU));
        g.gridy = 5; g.insets = ins(0, 70, 20, 70);
        body.add(back, g);

        outer.add(body, BorderLayout.CENTER);
        return outer;
    }

    private void refreshBalance() {
        if (account == null || balAmtLbl == null) return;
        balAmtLbl.setText("Rs. " + String.format("%,.2f", account.getBalance()));
        balAccLbl.setText("A/C: " + account.getAccountNumber()
                        + "   |   " + account.getAccountHolder());
    }

    // ════════════════════════════════════════════════════════════
    //  SCREEN 5 — DEPOSIT  ★ saves to DB after every deposit ★
    // ════════════════════════════════════════════════════════════
    private JPanel depositScreen() {
        JPanel outer = bgPanel(new BorderLayout());
        outer.add(topBar("⬆  Deposit Money"), BorderLayout.NORTH);

        JPanel body = bgPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        g.gridy = 0; g.insets = ins(20, 0, 10, 0);
        body.add(lbl("Enter Amount to Deposit (Rs.)", 13, Font.PLAIN, WHITE), g);

        JTextField amtField = inputField();
        amtField.setHorizontalAlignment(JTextField.CENTER);
        body.add(amtField, row(g, 1, ins(0, 70, 12, 70)));

        body.add(quickBtnRow(amtField), row(g, 2, ins(0, 40, 16, 40)));

        JLabel msg = lbl("", 13, Font.BOLD, GREEN);
        body.add(msg, row(g, 3, ins(0, 0, 6, 0)));

        // DB save status label
        JLabel dbMsg = lbl("", 11, Font.PLAIN, GRAY);
        body.add(dbMsg, row(g, 4, ins(0, 0, 10, 0)));

        JButton depBtn = bigBtn("DEPOSIT  ", GREEN);
        depBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                if (account.deposit(amt)) {
                    // ★ SAVE TO DATABASE immediately after deposit ★
                    DatabaseManager.saveAccount(account);

                    msg.setForeground(GREEN);
                    msg.setText("  Rs." + String.format("%,.2f", amt) + " deposited!");
                    dbMsg.setText("  Saved to database — balance updated");
                    amtField.setText("");
                } else {
                    msg.setForeground(RED);
                    msg.setText("  Amount must be greater than 0!");
                    dbMsg.setText("");
                }
            } catch (NumberFormatException ex) {
                msg.setForeground(RED);
                msg.setText("  Please enter a valid number!");
                dbMsg.setText("");
            }
        });
        body.add(depBtn, row(g, 5, ins(0, 70, 8, 70)));

        JButton back = bigBtn("←  BACK TO MENU", ACCENT);
        back.addActionListener(e -> {
            msg.setText(""); dbMsg.setText(""); amtField.setText("");
            navigate(MENU);
        });
        body.add(back, row(g, 6, ins(0, 70, 20, 70)));

        outer.add(body, BorderLayout.CENTER);
        return outer;
    }

    // ════════════════════════════════════════════════════════════
    //  SCREEN 6 — WITHDRAW  ★ saves to DB after every withdraw ★
    // ════════════════════════════════════════════════════════════
    private JPanel withdrawScreen() {
        JPanel outer = bgPanel(new BorderLayout());
        outer.add(topBar("⬇  Withdraw Money"), BorderLayout.NORTH);

        JPanel body = bgPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        g.gridy = 0; g.insets = ins(20, 0, 10, 0);
        body.add(lbl("Enter Amount to Withdraw (Rs.)", 13, Font.PLAIN, WHITE), g);

        JTextField amtField = inputField();
        amtField.setHorizontalAlignment(JTextField.CENTER);
        body.add(amtField, row(g, 1, ins(0, 70, 12, 70)));

        body.add(quickBtnRow(amtField), row(g, 2, ins(0, 40, 16, 40)));

        JLabel msg = lbl("", 13, Font.BOLD, GREEN);
        body.add(msg, row(g, 3, ins(0, 0, 6, 0)));

        JLabel dbMsg = lbl("", 11, Font.PLAIN, GRAY);
        body.add(dbMsg, row(g, 4, ins(0, 0, 10, 0)));

        JButton wdBtn = bigBtn("WITHDRAW  ✓", RED);
        wdBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                if (account.withdraw(amt)) {
                    // ★ SAVE TO DATABASE immediately after withdraw ★
                    DatabaseManager.saveAccount(account);

                    msg.setForeground(GREEN);
                    msg.setText("  Rs." + String.format("%,.2f", amt) + " withdrawn!");
                    dbMsg.setText("  Saved to database — balance updated");
                    amtField.setText("");
                } else {
                    msg.setForeground(RED);
                    msg.setText("  Insufficient balance or invalid amount!");
                    dbMsg.setText("");
                }
            } catch (NumberFormatException ex) {
                msg.setForeground(RED);
                msg.setText("  Please enter a valid number!");
                dbMsg.setText("");
            }
        });
        body.add(wdBtn, row(g, 5, ins(0, 70, 8, 70)));

        JButton back = bigBtn("←  BACK TO MENU", ACCENT);
        back.addActionListener(e -> {
            msg.setText(""); dbMsg.setText(""); amtField.setText("");
            navigate(MENU);
        });
        body.add(back, row(g, 6, ins(0, 70, 20, 70)));

        outer.add(body, BorderLayout.CENTER);
        return outer;
    }

    // ════════════════════════════════════════════════════════════
    //  SCREEN 7 — MINI STATEMENT
    // ════════════════════════════════════════════════════════════
    private JPanel statementScreen() {
        JPanel outer = bgPanel(new BorderLayout());
        outer.add(topBar("  Mini Statement"), BorderLayout.NORTH);

        JPanel body = bgPanel(new BorderLayout());
        body.setBorder(new EmptyBorder(12, 20, 10, 20));

        stmtArea = new JTextArea();
        stmtArea.setEditable(false);
        stmtArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        stmtArea.setBackground(new Color(15, 23, 42));
        stmtArea.setForeground(WHITE);
        stmtArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        stmtArea.setLineWrap(true);
        stmtArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(stmtArea);
        scroll.setBorder(BorderFactory.createLineBorder(ACCENT, 1));
        scroll.getViewport().setBackground(new Color(15, 23, 42));
        body.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = bgPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        JButton back = bigBtn("←  BACK TO MENU", ACCENT);
        back.addActionListener(e -> navigate(MENU));
        btnRow.add(back);
        body.add(btnRow, BorderLayout.SOUTH);

        outer.add(body, BorderLayout.CENTER);
        return outer;
    }

    private void refreshStatement() {
        if (account == null || stmtArea == null) return;
        List<String> txns = account.getTransactions();
        StringBuilder sb  = new StringBuilder();
        sb.append("==============================================\n");
        sb.append("    JAVA BANK  —  MINI STATEMENT\n");
        sb.append("==============================================\n");
        sb.append("    Account  : ").append(account.getAccountNumber()).append("\n");
        sb.append("    Holder   : ").append(account.getAccountHolder()).append("\n");
        sb.append("    Source   : Loaded from Database\n");
        sb.append("==============================================\n\n");

        if (txns.isEmpty()) {
            sb.append("    No transactions found.\n");
        } else {
            // Show last 10 transactions newest first
            int from = Math.max(0, txns.size() - 10);
            for (int i = txns.size() - 1; i >= from; i--) {
                sb.append("  ").append(txns.get(i)).append("\n\n");
            }
        }

        sb.append("==============================================\n");
        sb.append("    Current Balance : Rs.")
          .append(String.format("%,.2f", account.getBalance())).append("\n");
        sb.append("==============================================");
        stmtArea.setText(sb.toString());
        stmtArea.setCaretPosition(0);
    }

    // ════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════════════════════
    private JPanel bgPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG);
        return p;
    }

    private JLabel lbl(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        return l;
    }

    private JLabel fieldLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(GRAY);
        return l;
    }

    private JTextField inputField() {
        JTextField f = new JTextField();
        styleInput(f);
        return f;
    }

    private void styleInput(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBackground(CARD);
        f.setForeground(WHITE);
        f.setCaretColor(ACCENT);
        f.setPreferredSize(new Dimension(320, 44));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
    }

    private JButton bigBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(320, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton menuBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        b.setForeground(WHITE);
        b.setBackground(CARD);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setPreferredSize(new Dimension(320, 50));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(0, 20, 0, 20)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(51, 65, 85)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(CARD); }
        });
        return b;
    }

    private JPanel quickBtnRow(JTextField target) {
        JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
        row.setOpaque(false);
        for (int amt : new int[]{ 500, 1000, 2000, 5000 }) {
            JButton b = new JButton("Rs." + amt);
            b.setFont(new Font("Segoe UI", Font.BOLD, 11));
            b.setForeground(ACCENT);
            b.setBackground(CARD);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(ACCENT, 1));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> target.setText(String.valueOf(amt)));
            row.add(b);
        }
        return row;
    }

    private JPanel topBar(String title) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(15, 23, 42));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT),
            BorderFactory.createEmptyBorder(16, 24, 14, 24)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl.setForeground(ACCENT);
        bar.add(lbl, BorderLayout.WEST);
        return bar;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx   = 0;
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        return g;
    }

    private GridBagConstraints row(GridBagConstraints g, int r, Insets i) {
        g.gridy = r; g.insets = i; return g;
    }

    private Insets ins(int t, int l, int b, int r) {
        return new Insets(t, l, b, r);
    }
}
