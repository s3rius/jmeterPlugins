package com.s3rius.jmeterPlugins.JWTEncoder;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JWTEncoderGUI extends AbstractConfigGui {

    private JTextField variableNameField;
    private JTextField jwtKeyField;
    private JTextPane jwtClaimsField;

    /***
     * Main constructor.
     * 
     * It just initializes GUI.
     */
    public JWTEncoderGUI() {
        initGui();
    }

    /***
     * Create GUI using java swing technology.
     * 
     * 
     * It just allocates all text fields we need and
     * places them nicely using GridBagLayout.
     */
    private void initGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        Container topPanel = makeTitlePanel();

        add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        mainPanel.add(new JLabel("Variable name: "), labelConstraints);

        editConstraints.gridx = 1;
        editConstraints.gridy = 0;
        mainPanel.add(variableNameField = new JTextField("jwt_token"), editConstraints);

        labelConstraints.gridx = 0;
        labelConstraints.gridy = 1;
        mainPanel.add(new JLabel("JWT key: "), labelConstraints);

        editConstraints.gridx = 1;
        editConstraints.gridy = 1;
        mainPanel.add(jwtKeyField = new JTextField("secret"), editConstraints);

        labelConstraints.gridy = 2;
        mainPanel.add(new JLabel("JWT claims"), labelConstraints);

        GridBagConstraints claimConstraints = new GridBagConstraints();
        claimConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        claimConstraints.fill = GridBagConstraints.HORIZONTAL;
        claimConstraints.gridx = 0;
        claimConstraints.gridy = 3;
        claimConstraints.gridheight = 10;
        claimConstraints.gridwidth = 2;

        mainPanel.add(jwtClaimsField = new JTextPane(), claimConstraints);
        jwtClaimsField.setText("{\n    \"claim\": \"value\"\n}");
        JPanel container = new JPanel(new BorderLayout());
        container.add(mainPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
    }

    /***
     * This string is used in .jmx files.
     */
    @Override
    public String getLabelResource() {
        return "jwt_encoder";
    }

    /***
     * This string is shown in context menu.
     */
    @Override
    public String getStaticLabel() {
        return "Generate JWT";
    }

    @Override
    public TestElement createTestElement() {
        JWTEncoder encoder = new JWTEncoder();
        modifyTestElement(encoder);
        return encoder;
    }

    /***
     * This method is called when gui fields
     * are saved.
     * 
     * Here we just set all parameters we need in our encoder.
     */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);

        if (element instanceof JWTEncoder) {
            JWTEncoder encoder = (JWTEncoder) element;

            encoder.setVariableName(this.variableNameField.getText());
            encoder.setJwtKey(this.jwtKeyField.getText());
            encoder.setJwtClaims(this.jwtClaimsField.getText());
        }
    }

    /***
     * Called when configuration is loaded from
     * file.
     * 
     * This method sets everything from JWTEncoder
     * to GUI text fields.
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (element instanceof JWTEncoder) {
            JWTEncoder encoder = (JWTEncoder) element;

            jwtKeyField.setText(encoder.getJwtKey());
            jwtClaimsField.setText(encoder.getJwtClaims());
            variableNameField.setText(encoder.getVariableName());
            Border border = null;

            /// Updating borders for invalid fields.
            if (encoder.isValidClaims()) {
                border = BorderFactory.createEmptyBorder();
            } else {
                border = BorderFactory.createLineBorder(Color.RED);
            }
            jwtClaimsField.setBorder(border);

            if (encoder.getJwtKey() == null || encoder.getJwtKey().isEmpty()) {
                border = BorderFactory.createLineBorder(Color.RED);
            } else {
                border = BorderFactory.createEmptyBorder();
            }
            jwtKeyField.setBorder(border);

            if (encoder.getVariableName() == null || encoder.getVariableName().isEmpty()) {
                border = BorderFactory.createLineBorder(Color.RED);
            } else {
                border = BorderFactory.createEmptyBorder();
            }
            variableNameField.setBorder(border);
            highlightJson();
        }
    }

    /***
     * Highlight JSON properly.
     * 
     * It sets different colors for
     * background, strings, numbers and the null.
     * 
     */
    private void highlightJson() {
        Color bgColor = Color.decode("#282A36");
        Color fgColor = Color.decode("#F8F8F2");
        Color stringColor = Color.decode("#F1FA8C");
        Color numberColor = Color.decode("#BD93F9");

        this.jwtClaimsField.setBackground(bgColor);
        this.jwtClaimsField.setCaretColor(fgColor);
        this.jwtClaimsField.setForeground(fgColor);

        SimpleAttributeSet foregroundAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(foregroundAttr, fgColor);

        SimpleAttributeSet stringAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(stringAttr, stringColor);

        SimpleAttributeSet numberAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(numberAttr, numberColor);

        this.jwtClaimsField.getStyledDocument().setCharacterAttributes(
                0,
                this.jwtClaimsField.getText().length(),
                foregroundAttr,
                false);

        for (int index = jwtClaimsField.getText().indexOf("null"); index >= 0; index = jwtClaimsField.getText()
                .indexOf("null", index + 1)) {
            jwtClaimsField.getStyledDocument().setCharacterAttributes(
                    index,
                    4,
                    numberAttr,
                    false);
        }

        Matcher matcherForNumbers = Pattern.compile("\\d+(\\.\\d*)?").matcher(jwtClaimsField.getText());

        while (matcherForNumbers.find()) {
            jwtClaimsField.getStyledDocument().setCharacterAttributes(
                    matcherForNumbers.start(),
                    matcherForNumbers.end() - matcherForNumbers.start(),
                    numberAttr,
                    false);
        }

        Matcher matcherForStrings = Pattern.compile("\".*\"").matcher(jwtClaimsField.getText());

        while (matcherForStrings.find()) {
            jwtClaimsField.getStyledDocument().setCharacterAttributes(
                    matcherForStrings.start(),
                    matcherForStrings.end() - matcherForStrings.start(),
                    stringAttr,
                    false);
        }

    }

}
