/*
 * QuickChat Messaging Application
 * ST10477675 - Part 3
 */
package com.mycompany.st10477675part3;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Iterator;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ST10477675Part3 {

    // Array to store messages
    static JSONArray messageStorage = new JSONArray();
    static int totalMessages = 0;

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, """
                Welcome to QuickChat!
                To use this app:
                1. Log in with your user name and password.
                2. Send messages or quit.
                3. Messages are saved securely when you exit.
                Let's get started!""");

        String user = JOptionPane.showInputDialog("Enter your user name:");
        String pass = JOptionPane.showInputDialog("Enter your password:");

        if (!"Thabiso".equals(user)) {
            JOptionPane.showMessageDialog(null, "Login failed! Incorrect user name.");
            return;
        }
        if (!"2005".equals(pass)) {
            JOptionPane.showMessageDialog(null, "Login failed! Incorrect password.");
            return;
        }

        JOptionPane.showMessageDialog(null, "Login successful! Welcome, Thabiso.");

        loadMessagesFromJSON(); // Load previous messages

        String[] options = {
            "Send Messages",
            "Show Recently Sent Messages",
            "Display All Senders & Recipients",
            "Display Longest Message",
            "Search by Message ID",
            "Search Messages by Recipient",
            "Delete Message by Hash",
            "Full Message Report",
            "Quit"
        };

        while (true) {
            JList<String> optionList = new JList<>(options);
            optionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(optionList);

            int choice = JOptionPane.showConfirmDialog(
                    null,
                    scrollPane,
                    "QuickChat - Choose an Option",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (choice == JOptionPane.OK_OPTION) {
                int selectedIndex = optionList.getSelectedIndex();
                switch (selectedIndex) {
                    case 0 -> sendMultipleMessages(); // Send messages using phone number
                    case 1 -> showMessages();
                    case 2 -> displaySendersAndRecipients();
                    case 3 -> displayLongestMessage();
                    case 4 -> searchByMessageID();
                    case 5 -> searchByRecipient();
                    case 6 -> deleteByMessageHash();
                    case 7 -> displayFullReport();
                    case 8 -> {
                        saveMessagesToJSON();
                        JOptionPane.showMessageDialog(null, "Thank you for using QuickChat! Goodbye!");
                        return;
                    }
                    default -> JOptionPane.showMessageDialog(null, "Invalid option selected.");
                }
            }
        }
    }

    // Method to send multiple messages
    static void sendMultipleMessages() {
        try {
            String numMessagesInput = JOptionPane.showInputDialog("How many messages would you like to send? (e.g., 3)");
            int numMessages = Integer.parseInt(numMessagesInput);
            if (numMessages <= 0) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number of messages.");
                return;
            }

            String senderNumber = "+27Thabiso"; // Fixed sender name for now

            for (int i = 0; i < numMessages; i++) {
                String recipientInput = JOptionPane.showInputDialog("Enter recipient phone number (last 9 digits):");
                String message = JOptionPane.showInputDialog("Enter your message: (e.g., Hello!)");

                if (recipientInput == null || message == null) {
                    JOptionPane.showMessageDialog(null, "Operation cancelled.");
                    break;
                }

                if (!validatePhoneNumber(recipientInput)) {
                    JOptionPane.showMessageDialog(null, "Invalid phone number. Must be exactly 9 digits.");
                    continue;
                }

                if (message.length() > 250) {
                    JOptionPane.showMessageDialog(null, "Message must be less than 250 characters.");
                    continue;
                }

                String messageId = generateUniqueMessageID();
                String messageHash = generateMessageHash(messageId, totalMessages + 1, message);

                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("MessageID", messageId);
                jsonMessage.put("Sender", senderNumber);
                jsonMessage.put("Recipient", "+27" + recipientInput);
                jsonMessage.put("Message", message);
                jsonMessage.put("MessageHash", messageHash);

                messageStorage.add(jsonMessage);

                JOptionPane.showMessageDialog(null, String.format("""
                        Message Sent!
                        Message ID: %s
                        Sender: %s
                        Recipient: +27%s
                        Message: %s
                        Message Hash: %s""",
                        messageId, senderNumber, recipientInput, message, messageHash));

                totalMessages++;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
        }
    }

    // Validate phone number (must be 9 digits)
    static boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\d{9}");
    }

    // Generate unique message ID
    static String generateUniqueMessageID() {
        Random random = new Random();
        long id = Math.abs(random.nextLong()) % 1_000_000_000L;
        return String.format("%010d", id);
    }

    // Generate message hash
    static String generateMessageHash(String messageId, int numMessagesSent, String message) {
        String firstTwoDigits = messageId.substring(0, 2);
        String numMessagesStr = String.valueOf(numMessagesSent);
        String[] words = message.split("\\s+");
        String firstWord = words[0].toUpperCase();
        String lastWord = words.length > 0 ? words[words.length - 1].toUpperCase() : "";
        return firstTwoDigits + ":" + numMessagesStr + ":" + firstWord + lastWord;
    }

    // Save messages to JSON
    static void saveMessagesToJSON() {
        try (FileWriter file = new FileWriter("storedMessages.json")) {
            file.write(messageStorage.toJSONString());
            System.out.println("Messages saved to storedMessages.json");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving messages: " + e.getMessage());
        }
    }

    // Load messages from JSON
    static void loadMessagesFromJSON() {
        try {
            FileReader reader = new FileReader("storedMessages.json");
            JSONParser parser = new JSONParser();
            messageStorage = (JSONArray) parser.parse(reader);
            totalMessages = messageStorage.size();
            System.out.println("Loaded " + totalMessages + " messages.");
        } catch (IOException | ParseException e) {
            System.out.println("No saved messages found.");
        }
    }

    // Show recent messages
    static void showMessages() {
        if (messageStorage.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages found.");
            return;
        }

        StringBuilder sb = new StringBuilder("Recently Sent Messages:\n");
        for (Object obj : messageStorage) {
            JSONObject msg = (JSONObject) obj;
            sb.append("To: ").append(msg.get("Recipient"))
              .append(" | Message: ").append(msg.get("Message")).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    // Display senders and recipients
    static void displaySendersAndRecipients() {
        if (messageStorage.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages found.");
            return;
        }

        StringBuilder sb = new StringBuilder("Senders and Recipients:\n");
        for (Object obj : messageStorage) {
            JSONObject msg = (JSONObject) obj;
            sb.append("Sender: ").append(msg.get("Sender"))
              .append(", Recipient: ").append(msg.get("Recipient")).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    // Display longest message
    static void displayLongestMessage() {
        JSONObject longest = null;
        for (Object obj : messageStorage) {
            JSONObject msg = (JSONObject) obj;
            if (longest == null ||
                msg.get("Message").toString().length() > longest.get("Message").toString().length()) {
                longest = msg;
            }
        }
        if (longest != null) {
            JOptionPane.showMessageDialog(null, String.format("""
                    Longest Message:
                    Sender: %s
                    Recipient: %s
                    Message: %s""",
                    longest.get("Sender"), longest.get("Recipient"), longest.get("Message")));
        } else {
            JOptionPane.showMessageDialog(null, "No messages found.");
        }
    }

    // Search by message ID
    static void searchByMessageID() {
        String input = JOptionPane.showInputDialog("Enter Message ID to search:");
        if (input == null) return;

        for (Object obj : messageStorage) {
            JSONObject msg = (JSONObject) obj;
            if (msg.get("MessageID").equals(input)) {
                JOptionPane.showMessageDialog(null, String.format("""
                        Found Message:
                        Recipient: %s
                        Message: %s""",
                        msg.get("Recipient"), msg.get("Message")));
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message ID not found.");
    }

    // Search by recipient phone number
    static void searchByRecipient() {
        String input = JOptionPane.showInputDialog("Enter recipient phone number (last 9 digits):");
        if (input == null) return;

        String fullRecipientNumber = "+27" + input;
        StringBuilder sb = new StringBuilder("Messages for ").append(fullRecipientNumber).append(":\n");
        boolean found = false;

        for (Object obj : messageStorage) {
            JSONObject msg = (JSONObject) obj;
            if (msg.get("Recipient").equals(fullRecipientNumber)) {
                found = true;
                sb.append("ID: ").append(msg.get("MessageID"))
                  .append(", Message: ").append(msg.get("Message")).append("\n");
            }
        }

        JOptionPane.showMessageDialog(null, found ? sb.toString() : "No messages found for recipient.");
    }

    // Delete message by hash
    static void deleteByMessageHash() {
        String input = JOptionPane.showInputDialog("Enter message hash to delete:");
        if (input == null) return;

        Iterator<JSONObject> iterator = messageStorage.iterator();
        while (iterator.hasNext()) {
            JSONObject msg = iterator.next();
            if (msg.get("MessageHash").equals(input)) {
                iterator.remove();
                totalMessages--;
                JOptionPane.showMessageDialog(null, "Message deleted.");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message hash not found.");
    }

    // Display full message report
    static void displayFullReport() {
        if (messageStorage.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages found.");
            return;
        }

        StringBuilder sb = new StringBuilder("Full Message Report:\n");
        for (Object obj : messageStorage) {
            JSONObject msg = (JSONObject) obj;
            sb.append("ID: ").append(msg.get("MessageID"))
              .append(", Hash: ").append(msg.get("MessageHash"))
              .append(", Sender: ").append(msg.get("Sender"))
              .append(", Recipient: ").append(msg.get("Recipient"))
              .append(", Message: ").append(msg.get("Message")).append("\n");
        }

        JOptionPane.showMessageDialog(null, sb.toString());
    }
}