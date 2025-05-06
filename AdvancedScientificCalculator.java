import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class AdvancedScientificCalculator extends JFrame implements ActionListener {

    private JTextField displayField;
    private JTextArea historyArea;
    private JDialog historyDialog;

    private double num1 = 0;
    private String operator = "";
    private boolean expectingSecondNumber = false;
    private boolean errorState = false;

    private double memory = 0;

    private List<String> calculationHistory = new ArrayList<>();

    private static final double E_VALUE = Math.E;
    private static final double PI_VALUE = Math.PI;

    public AdvancedScientificCalculator() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus L&F not found, using default.");
        }

        setTitle("Advanced Scientific Calculator");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        displayField = new JTextField("0");
        displayField.setEditable(false);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setFont(new Font("SansSerif", Font.BOLD, 24));
        displayField.setBorder(BorderFactory.createCompoundBorder(
                displayField.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        add(displayField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 5, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] buttonLabels = {
                "%", "CE", "C", "<-", "/",
                "sin", "cos", "tan", "x^y", "sqrt",
                "log10", "ln", "e", "x^2", "1/x",
                "7", "8", "9", "*", "n!",
                "4", "5", "6", "-", "π",
                "1", "2", "3", "+", "±",
                "M+", "MR", "MC", "0", "="
        };

        Font buttonFont = new Font("SansSerif", Font.PLAIN, 14);
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(buttonFont);
            button.addActionListener(this);
            if (!label.matches("[0-9.]") && !label.matches("M[+|RC]") && !label.equals("<-") && !label.equals("±") && !label.equals("CE") && !label.equals("C") && !label.equals("e") && !label.equals("π")) {
                 button.setBackground(new Color(210, 210, 220));
            }
             if (label.equals("=")) {
                 button.setBackground(new Color(150, 200, 250));
             }
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);

        JButton historyButton = new JButton("History");
        historyButton.setFont(buttonFont);
        historyButton.addActionListener(e -> showHistory());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(historyButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setupHistoryDialog();

        setLocationRelativeTo(null);
    }

    private void setupHistoryDialog() {
        historyDialog = new JDialog(this, "Calculation History", false);
        historyDialog.setSize(350, 400);
        historyDialog.setLayout(new BorderLayout(5, 5));
        historyDialog.setLocationRelativeTo(this);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        historyDialog.add(scrollPane, BorderLayout.CENTER);

        JButton clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.addActionListener(e -> {
            calculationHistory.clear();
            historyArea.setText("");
        });

        JPanel historyBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        historyBottomPanel.add(clearHistoryButton);
        historyDialog.add(historyBottomPanel, BorderLayout.SOUTH);
    }

    private void showHistory() {
        StringBuilder historyContent = new StringBuilder();
        for (String entry : calculationHistory) {
            historyContent.append(entry).append("\n");
        }
        historyArea.setText(historyContent.toString());
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
        historyDialog.setVisible(true);
    }

    private void addToHistory(String calculation, String result) {
        if (errorState) return;
        String entry = calculation + " = " + result;
        calculationHistory.add(entry);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();
        handleButtonClick(command);
    }

    private void handleButtonClick(String label) {
        if (errorState && !label.equals("C") && !label.equals("CE")) {
           clearAll();
        }

        if (errorState && !(label.equals("C") || label.equals("CE"))) {
             displayError("Press C or CE");
             return;
        }


        try {
            switch (label) {
                case "0": case "1": case "2": case "3": case "4":
                case "5": case "6": case "7": case "8": case "9":
                    if (expectingSecondNumber) {
                        displayField.setText(label);
                        expectingSecondNumber = false;
                    } else {
                        if (displayField.getText().equals("0")) {
                            displayField.setText(label);
                        } else {
                            displayField.setText(displayField.getText() + label);
                        }
                    }
                    break;
                case ".":
                     if (expectingSecondNumber) {
                        displayField.setText("0.");
                        expectingSecondNumber = false;
                     } else if (!displayField.getText().contains(".")) {
                        displayField.setText(displayField.getText() + ".");
                    }
                    break;

                case "+": case "-": case "*": case "/": case "x^y":
                    handleBinaryOperator(label);
                    break;

                case "sqrt": performUnaryOperation("sqrt", x -> Math.sqrt(x), x -> x >= 0); break;
                case "x^2":  performUnaryOperation("sqr", x -> x * x, null); break;
                case "1/x":  performUnaryOperation("1/", x -> 1.0 / x, x -> x != 0); break;
                case "%":    performUnaryOperation("%", x -> x / 100.0, null); break;
                case "sin":  performUnaryOperation("sin", x -> Math.sin(Math.toRadians(x)), null); break;
                case "cos":  performUnaryOperation("cos", x -> Math.cos(Math.toRadians(x)), null); break;
                case "tan":  performUnaryOperation("tan", x -> Math.tan(Math.toRadians(x)), x -> Math.cos(Math.toRadians(x)) != 0); break;
                case "log10": performUnaryOperation("log", x -> Math.log10(x), x -> x > 0); break;
                case "ln":   performUnaryOperation("ln", x -> Math.log(x), x -> x > 0); break;
                case "n!":   performFactorial(); break;
                case "±":    negateDisplay(); break;


                 case "π":
                    displayField.setText(formatDouble(PI_VALUE));
                    expectingSecondNumber = false;
                    break;
                case "e":
                    displayField.setText(formatDouble(E_VALUE));
                    expectingSecondNumber = false;
                    break;

                case "=":
                    calculateResult();
                    break;

                case "C":
                    clearAll();
                    break;
                case "CE":
                    clearEntry();
                    break;
                case "<-":
                    backspace();
                    break;

                case "M+":
                    try {
                        memory += getCurrentDisplayValue();
                        expectingSecondNumber = true;
                    } catch (NumberFormatException ex) {
                        displayError("Invalid number for M+");
                    }
                    break;
                case "MR":
                    displayField.setText(formatDouble(memory));
                    expectingSecondNumber = false;
                    break;
                case "MC":
                    memory = 0;
                    expectingSecondNumber = true;
                    break;

                default:
                    System.err.println("Unhandled button: " + label);
            }
        } catch (NumberFormatException ex) {
            displayError("Invalid number format");
        } catch (ArithmeticException ex) {
             displayError(ex.getMessage());
        } catch (Exception ex) {
             displayError("Error");
             ex.printStackTrace();
        }
    }


    private double getCurrentDisplayValue() throws NumberFormatException {
        if(errorState || displayField.getText().isEmpty()) return 0;
        return Double.parseDouble(displayField.getText());
    }

     private void handleBinaryOperator(String newOperator) {
        if (errorState) return;

        if (!operator.isEmpty() && !expectingSecondNumber) {
            calculateResult();
             if(errorState) return;
        }

        try {
            num1 = getCurrentDisplayValue();
            operator = newOperator;
            expectingSecondNumber = true;
         } catch (NumberFormatException ex) {
             displayError("Invalid number before operator");
        }
    }

     private void calculateResult() {
        if (errorState || operator.isEmpty() || expectingSecondNumber ) {
             if(expectingSecondNumber && operator.isEmpty()){
                 return;
             }
              if(expectingSecondNumber) return;
               if (!operator.isEmpty()){
                    // Allow repeat operation? Implicitly works with current state sometimes.
               } else {
                   return;
               }
        }

        double num2;
        try {
             num2 = getCurrentDisplayValue();
        } catch (NumberFormatException ex) {
             displayError("Invalid second number");
             return;
         }

        String calculationString = formatDouble(num1) + " " + operator + " " + formatDouble(num2);
        double result = 0;

        switch (operator) {
            case "+": result = num1 + num2; break;
            case "-": result = num1 - num2; break;
            case "*": result = num1 * num2; break;
            case "/":
                if (num2 == 0) {
                    displayError("Division by zero");
                    return;
                }
                result = num1 / num2;
                break;
            case "x^y":
                result = Math.pow(num1, num2);
                break;
            default:
                 displayError("Unknown operator");
                return;
        }

        if (Double.isInfinite(result) || Double.isNaN(result)) {
             displayError("Result is Undefined");
         } else {
            String resultStr = formatDouble(result);
            displayField.setText(resultStr);
            addToHistory(calculationString, resultStr);

            num1 = result;
            operator = "";
            expectingSecondNumber = true;
         }
    }

    private void performUnaryOperation(String opName, java.util.function.DoubleUnaryOperator operation, java.util.function.Predicate<Double> validator) {
         if (errorState) return;
        try {
             double value = getCurrentDisplayValue();
             String originalValueStr = formatDouble(value);

             if (validator != null && !validator.test(value)) {
                 displayError("Invalid input for " + opName);
                 return;
             }

             double result = operation.applyAsDouble(value);

             if (Double.isInfinite(result) || Double.isNaN(result)) {
                 displayError("Result Undefined (" + opName + ")");
             } else {
                 String resultStr = formatDouble(result);
                 displayField.setText(resultStr);
                 addToHistory(opName + "(" + originalValueStr + ")", resultStr);
                 expectingSecondNumber = true;
             }
         } catch (NumberFormatException ex) {
             displayError("Invalid number for " + opName);
         }
    }

    private void performFactorial() {
        if (errorState) return;
        try {
            double value = getCurrentDisplayValue();
            String originalValueStr = formatDouble(value);

            if (value < 0 || value != Math.floor(value)) {
                 displayError("Factorial requires non-negative integer");
                 return;
            }

            if (value > 20) {
                displayError("Factorial input too large");
                return;
            }

             int n = (int) value;
             double result = 1;
             for (int i = 2; i <= n; i++) {
                 result *= i;
             }

              if (Double.isInfinite(result)) {
                 displayError("Factorial result too large");
              } else {
                String resultStr = formatDouble(result);
                displayField.setText(resultStr);
                addToHistory(originalValueStr + "!", resultStr);
                expectingSecondNumber = true;
             }

        } catch (NumberFormatException ex) {
             displayError("Invalid number for !");
        }
    }

    private void negateDisplay() {
         if (errorState || displayField.getText().equals("0")) return;

        try {
             double value = getCurrentDisplayValue();
            displayField.setText(formatDouble(-value));
             expectingSecondNumber = false;
         } catch (NumberFormatException ex) {
             displayError("Cannot negate value");
         }
    }

     private void clearAll() {
        displayField.setText("0");
        num1 = 0;
        operator = "";
        expectingSecondNumber = false;
        errorState = false;
    }

    private void clearEntry() {
        if(errorState) {
            clearAll();
            return;
        }
        displayField.setText("0");
         if (expectingSecondNumber) {
            // operator and num1 remain
         } else {
             num1 = 0;
             operator = "";
         }
          errorState = false;
    }

     private void backspace() {
         if (errorState) return;
        String currentText = displayField.getText();
        if (currentText.length() > 0 && !currentText.equals("0")) {
            currentText = currentText.substring(0, currentText.length() - 1);
            if (currentText.isEmpty() || currentText.equals("-")) {
                currentText = "0";
            }
             displayField.setText(currentText);
        } else {
            displayField.setText("0");
        }
          expectingSecondNumber = false;
    }


    private void displayError(String message) {
        displayField.setText(message);
        errorState = true;
        operator = "";
        expectingSecondNumber = false;
        num1 = 0;
    }

    private String formatDouble(double value) {
        if (Double.isNaN(value)) return "NaN";
        if (Double.isInfinite(value)) return "Infinity";
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.valueOf(value);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdvancedScientificCalculator calculator = new AdvancedScientificCalculator();
            calculator.setVisible(true);
        });
    }
}
