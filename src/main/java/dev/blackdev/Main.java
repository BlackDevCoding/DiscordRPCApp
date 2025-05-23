package dev.blackdev;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.time.Instant;
import java.util.Properties;

public class Main {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties config = new Properties();

    public static void main(String[] args) throws IOException {
        loadConfig();
        setDarkTheme();

        JFrame frame = new JFrame("Discord RPC Config");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.DARK_GRAY);

        JTextField appIdField = createTextField("Application ID", config.getProperty("appId", ""), panel);
        JTextField largeImageField = createTextField("Large Image", config.getProperty("largeImage", ""), panel);
        JTextField largeTextField = createTextField("Large Text", config.getProperty("largeText", ""), panel);
        JTextField smallImageField = createTextField("Small Image", config.getProperty("smallImage", ""), panel);
        JTextField smallTextField = createTextField("Small Text", config.getProperty("smallText", ""), panel);
        JTextField detailsField = createTextField("Details", config.getProperty("details", ""), panel);
        JTextField stateField = createTextField("State", config.getProperty("state", ""), panel);
        JTextField partyIdField = createTextField("Party ID", config.getProperty("partyId", ""), panel);
        JTextField joinSecretField = createTextField("Join Secret", config.getProperty("joinSecret", ""), panel);

        JButton startButton = new JButton("Start RPC");
        startButton.setBackground(new Color(114, 137, 218));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> {
            saveConfig(appIdField.getText(), largeImageField.getText(), largeTextField.getText(),
                    smallImageField.getText(), smallTextField.getText(), detailsField.getText(),
                    stateField.getText(), partyIdField.getText(), joinSecretField.getText());

            new Thread(() -> startRPC(appIdField.getText(), largeImageField.getText(), largeTextField.getText(),
                    smallImageField.getText(), smallTextField.getText(), detailsField.getText(),
                    stateField.getText(), partyIdField.getText(), joinSecretField.getText())).start();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.add(startButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static JTextField createTextField(String label, String value, JPanel panel) {
        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(Color.LIGHT_GRAY);
        JTextField field = new JTextField(value);
        field.setBackground(new Color(60, 63, 65));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createLineBorder(new Color(114, 137, 218)));
        panel.add(jLabel);
        panel.add(field);
        return field;
    }

    private static void setDarkTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", Color.DARK_GRAY);
            UIManager.put("Label.foreground", Color.LIGHT_GRAY);
            UIManager.put("TextField.background", new Color(60, 63, 65));
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("TextField.caretForeground", Color.WHITE);
            UIManager.put("Button.background", new Color(114, 137, 218));
            UIManager.put("Button.foreground", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            config.load(input);
        } catch (IOException ignored) {}
    }

    private static void saveConfig(String appId, String largeImage, String largeText, String smallImage, String smallText,
                                   String details, String state, String partyId, String joinSecret) {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            config.setProperty("appId", appId);
            config.setProperty("largeImage", largeImage);
            config.setProperty("largeText", largeText);
            config.setProperty("smallImage", smallImage);
            config.setProperty("smallText", smallText);
            config.setProperty("details", details);
            config.setProperty("state", state);
            config.setProperty("partyId", partyId);
            config.setProperty("joinSecret", joinSecret);
            config.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void startRPC(String appId, String largeImage, String largeText, String smallImage, String smallText,
                                 String details, String state, String partyId, String joinSecret) {
        try (CreateParams params = new CreateParams()) {
            params.setClientID(Long.parseLong(appId));
            params.setFlags(CreateParams.getDefaultFlags());

            try (Core core = new Core(params)) {
                try (Activity activity = new Activity()) {
                    activity.setDetails(details);
                    activity.setState(state);
                    activity.timestamps().setStart(Instant.now());
                    activity.assets().setLargeImage(largeImage);
                    activity.assets().setLargeText(largeText);
                    activity.assets().setSmallImage(smallImage);
                    activity.assets().setSmallText(smallText);
                    activity.party().setID(partyId);
                    activity.secrets().setJoinSecret(joinSecret);
                    core.activityManager().updateActivity(activity);
                }
                while (true) {
                    core.runCallbacks();
                    Thread.sleep(16);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}