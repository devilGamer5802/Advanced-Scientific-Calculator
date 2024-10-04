import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdvancedScientificCalculator extends JFrame {
    private JTextField displayField;
    private double num1 = 0;
    private double num2 = 0;
    private String operator = "";
    private boolean isResultDisplayed = false;
    private double memory = 0;

    public AdvancedScientificCalculator() {
        // Set up the JFrame
        setTitle("Advanced Scientific Calculator");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the display field
        displayField = new JTextField();
        displayField.setEditable(false);
        add(displayField, BorderLayout.NORTH);

        // Create the number pad and operators
        JPanel buttonPanel = new JPanel(new GridLayout(6, 5));

        String[] buttonLabels = {
            "7", "8", "9", "/", "sqrt",
            "4", "5", "6", "*", "x^2",
            "1", "2", "3", "-", "1/x",
            "0", ".", "=", "+", "C",
            "sin", "cos", "tan", "ln", "exp",
            "M+", "MR", "MC", "M"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleButtonClick(label);
                }
            });
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);
    }

    private void handleButtonClick(String label) {
        if (label.matches("[0-9.]")) {
            if (isResultDisplayed) {
                displayField.setText("");
                isResultDisplayed = false;
            }
            displayField.setText(displayField.getText() + label);
        } else if (label.equals("C")) {
            displayField.setText("");
        } else if (label.equals("=")) {
            num2 = Double.parseDouble(displayField.getText());
            calculateResult();
        } else if (label.equals("+") || label.equals("-") || label.equals("*") || label.equals("/")) {
            if (!operator.isEmpty()) {
                num2 = Double.parseDouble(displayField.getText());
                calculateResult();
            }
            operator = label;
            num1 = Double.parseDouble(displayField.getText());
            isResultDisplayed = true;
        } else if (label.equals("sqrt")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText(String.valueOf(Math.sqrt(value)));
            isResultDisplayed = true;
        } else if (label.equals("x^2")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText(String.valueOf(value * value));
            isResultDisplayed = true;
        } else if (label.equals("1/x")) {
            double value = Double.parseDouble(displayField.getText());
            if (value != 0) {
                displayField.setText(String.valueOf(1.0 / value));
            } else {
                displayField.setText("Error");
            }
            isResultDisplayed = true;
        } else if (label.equals("sin")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText("sin(" + value + ")");
            displayField.setText(String.valueOf(Math.sin(Math.toRadians(value))));
            isResultDisplayed = true;
        } else if (label.equals("cos")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText("cos(" + value + ")");
            displayField.setText(String.valueOf(Math.cos(Math.toRadians(value))));
            isResultDisplayed = true;
        } else if (label.equals("tan")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText("tan(" + value + ")");
            displayField.setText(String.valueOf(Math.tan(Math.toRadians(value))));
            isResultDisplayed = true;
        } else if (label.equals("ln")) {
            double value = Double.parseDouble(displayField.getText());
            if (value > 0) {
                displayField.setText("ln(" + value + ")");
                displayField.setText(String.valueOf(Math.log(value)));
            } else {
                displayField.setText("Error");
            }
            isResultDisplayed = true;
        } else if (label.equals("exp")) {
            double value = Double.parseDouble(displayField.getText());
            displayField.setText("exp(" + value + ")");
            displayField.setText(String.valueOf(Math.exp(value)));
            isResultDisplayed = true;
        } else if (label.equals("M+")) {
            memory += Double.parseDouble(displayField.getText());
        } else if (label.equals("MR")) {
            displayField.setText(String.valueOf(memory));
        } else if (label.equals("MC")) {
            memory = 0;
        } else if (label.equals("M")) {
            displayField.setText(String.valueOf(memory));
        }
    }

    private void calculateResult() {
        if (operator.equals("+")) {
            num1 += num2;
        } else if (operator.equals("-") ){
            num1 -= num2;
        } else if (operator.equals("*")) {
            num1 *= num2;
        } else if (operator.equals("/")) {
            if (num2 != 0) {
                num1 /= num2;
            } else {
                displayField.setText("Error");
                num1 = 0;
            }
        }
        displayField.setText(String.valueOf(num1));
        operator = "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdvancedScientificCalculator calculator = new AdvancedScientificCalculator();
            calculator.setVisible(true);
        });
    }
}
