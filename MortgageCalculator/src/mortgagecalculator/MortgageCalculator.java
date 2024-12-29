package mortgagecalculator;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.sql.*;

public class MortgageCalculator {
    
    // List to store loan records
    private static ArrayList<LoanAccount> loanAccounts = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mortgage Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));
        
        JLabel lblCustomerID = new JLabel("Customer ID:");
        JLabel lblLoanAmount = new JLabel("Loan Amount:");
        JLabel lblTerm = new JLabel("Term (Years):");
        JLabel lblInterestRate = new JLabel("Interest Rate:");
        JLabel lblHomeValue = new JLabel("Home Value:");
        JLabel lblMonthlyPayment = new JLabel("Monthly Payment:");
        JLabel lblTotalInterest = new JLabel("Total Interest:");
        
        JTextField txtCustomerID = new JTextField();
        JTextField txtLoanAmount = new JTextField();
        JTextField txtTerm = new JTextField();
        JTextField txtInterestRate = new JTextField();
        JTextField txtHomeValue = new JTextField();
        JTextField txtMonthlyPayment = new JTextField();
        JTextField txtTotalInterest = new JTextField();
        
        // Make HomeValue and MonthlyPayment fields read-only
        txtHomeValue.setEditable(false);
        txtMonthlyPayment.setEditable(false);

        JButton btnCalculate = new JButton("Calculate");
        JButton btnSubmitToDB = new JButton("Submit to DB");
        JButton btnExit = new JButton("Exit");
        
        panel.add(lblCustomerID);
        panel.add(txtCustomerID);
        panel.add(lblLoanAmount);
        panel.add(txtLoanAmount);
        panel.add(lblTerm);
        panel.add(txtTerm);
        panel.add(lblInterestRate);
        panel.add(txtInterestRate);
        panel.add(lblHomeValue);
        panel.add(txtHomeValue);
        panel.add(lblMonthlyPayment);
        panel.add(txtMonthlyPayment);
        panel.add(lblTotalInterest);
        panel.add(txtTotalInterest);
        panel.add(btnCalculate);
        panel.add(btnSubmitToDB);
        panel.add(btnExit);
        
        frame.add(panel);
        frame.setVisible(true);
        
        // Action Listeners for Buttons
        btnCalculate.addActionListener(e -> {
            try {
                // Retrieve values from form
                double loanAmount = Double.parseDouble(txtLoanAmount.getText());
                double interestRate = Double.parseDouble(txtInterestRate.getText());
                int termYears = Integer.parseInt(txtTerm.getText());
                int termMonths = termYears * 12;
                
                // Create LoanAccount object and calculate
                LoanAccount loan = new LoanAccount(loanAmount, interestRate, termMonths);
                loan.calculateLoan();
                
                // Store the loan in the ArrayList
                loanAccounts.add(loan);
                
                // Update text fields with results
                txtHomeValue.setText(String.format("%.2f", loan.getHomeValue()));
                txtMonthlyPayment.setText(String.format("%.2f", loan.getMonthlyPayment()));
                txtTotalInterest.setText(String.format("%.2f", loan.getTotalInterest()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: Please enter valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnSubmitToDB.addActionListener(e -> {
            try {
                // Submit the records to DB
                submitLoansToDatabase();
                loanAccounts.clear();  // Clear the ArrayList after submission
                JOptionPane.showMessageDialog(frame, "Loans submitted to the database.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnExit.addActionListener(e -> System.exit(0));
    }
    
    // Method to submit all loan records to the database
    private static void submitLoansToDatabase() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlserver://NADEEM;databaseName=loanaccounts"); 
        String query = "INSERT INTO loans (loanamount, interestrate, term, homevalue, monthlypayment, totalinterest) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(query);

        for (LoanAccount loan : loanAccounts) {
            ps.setDouble(1, loan.getLoanAmount());
            ps.setDouble(2, loan.getInterestRate());
            ps.setInt(3, (int) loan.getTerm());
            ps.setDouble(4, loan.getHomeValue());
            ps.setDouble(5, loan.getMonthlyPayment());
            ps.setDouble(6, loan.getTotalInterest());
            ps.addBatch(); // Add to batch
        }

        ps.executeBatch(); // Execute all batch insertions
        conn.close();
    }
}

class LoanAccount {
    public double loanAmount;
    public double interestRate;
    public double term; // Term in months
    public double homeValue;
    public double monthlyPayment;
    public double totalInterest;
    
    public LoanAccount(double loanAmount, double interestRate, double term) {
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.term = term;
    }
    
    // Method to calculate the loan
    public void calculateLoan() {
        double calcInterest = interestRate / (12 * 100);
        this.monthlyPayment = loanAmount * (calcInterest / (1 - Math.pow(1 + calcInterest, -this.term)));
        this.homeValue = loanAmount * 1.25;
        this.totalInterest = loanAmount * interestRate * (term / 12);
    }
    
    // Getter methods
    public double getLoanAmount() {
        return loanAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getTerm() {
        return term;
    }

    public double getHomeValue() {
        return homeValue;
    }

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public double getTotalInterest() {
        return totalInterest;
    }

    // Overridden toString() method
    @Override
    public String toString() {
        return "LoanAmount: " + loanAmount + ", InterestRate: " + interestRate + ", Term: " + term +
               ", HomeValue: " + homeValue + ", MonthlyPayment: " + monthlyPayment + ", TotalInterest: " + totalInterest;
    }
}
