import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Holds all the message arrays and the features that search them.
// allMessages, messageIDs and messageHashes are parallel arrays: the same index
// in each one refers to the same message.
public class MessageStore {

    public static final int SEND = 1;
    public static final int DISREGARD = 2;
    public static final int STORE = 3;

    private final ArrayList<Message> sentMessages = new ArrayList<>();
    private final ArrayList<Message> disregardedMessages = new ArrayList<>();
    private final ArrayList<Message> storedMessages = new ArrayList<>();

    private final ArrayList<Message> allMessages = new ArrayList<>();
    private final ArrayList<String> messageIDs = new ArrayList<>();
    private final ArrayList<String> messageHashes = new ArrayList<>();

    private int lastMessageNumber = 0;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Creates a message with the next message number
    public Message createMessage(String sender, String recipient, String text) {
        lastMessageNumber++;
        return new Message(lastMessageNumber, sender, recipient, text);
    }

    // Puts the message in the correct arrays for the user's choice
    public void addMessage(Message message, int choice) {
        allMessages.add(message);
        messageIDs.add(message.getMessageID());
        messageHashes.add(message.getMessageHash());

        if (choice == SEND) {
            sentMessages.add(message);
        } else if (choice == DISREGARD) {
            disregardedMessages.add(message);
        } else {
            storedMessages.add(message);
        }
    }

    public List<Message> getSentMessages() {
        return sentMessages;
    }

    public List<Message> getDisregardedMessages() {
        return disregardedMessages;
    }

    public List<Message> getStoredMessages() {
        return storedMessages;
    }

    public List<Message> getAllMessages() {
        return allMessages;
    }

    public List<String> getMessageIDs() {
        return messageIDs;
    }

    public List<String> getMessageHashes() {
        return messageHashes;
    }

    public int totalSent() {
        return sentMessages.size();
    }

    // Sender and recipient of every message in the list
    public String displaySendersAndRecipients(List<Message> messages) {
        if (messages.isEmpty()) {
            return "There are no messages to show.";
        }

        StringBuilder result = new StringBuilder();
        for (Message message : messages) {
            result.append("Sender: ").append(message.getSender())
                  .append(" | Recipient: ").append(message.getRecipient())
                  .append("\n");
        }
        return result.toString().trim();
    }

    // Longest message text in the list, or an empty string when the list is empty
    public String longestMessage(List<Message> messages) {
        String longest = "";
        for (Message message : messages) {
            if (message.getMessageText().length() > longest.length()) {
                longest = message.getMessageText();
            }
        }
        return longest;
    }

    // Finds the ID in the parallel array and returns the recipient and message
    public String searchByMessageID(String messageID) {
        int index = messageIDs.indexOf(messageID);

        if (index == -1) {
            return "Message ID " + messageID + " was not found.";
        }

        Message message = allMessages.get(index);
        return "Recipient: " + message.getRecipient()
                + "\nMessage: " + message.getMessageText();
    }

    // Every sent or stored message text for one recipient
    public List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();

        for (Message message : allMessages) {
            boolean sentOrStored = sentMessages.contains(message)
                    || storedMessages.contains(message);

            if (sentOrStored && message.getRecipient().equals(recipient)) {
                found.add(message.getMessageText());
            }
        }
        return found;
    }

    // Removes the message with this hash from every array
    public String deleteByMessageHash(String messageHash) {
        int index = messageHashes.indexOf(messageHash);

        if (index == -1) {
            return "No message with hash " + messageHash + " was found.";
        }

        Message message = allMessages.get(index);

        allMessages.remove(index);
        messageIDs.remove(index);
        messageHashes.remove(index);
        sentMessages.remove(message);
        disregardedMessages.remove(message);
        storedMessages.remove(message);

        return "Message: \"" + message.getMessageText() + "\" successfully deleted.";
    }

    // Report with the hash, recipient and message of each message in the list
    public String displayReport(List<Message> messages) {
        if (messages.isEmpty()) {
            return "There are no messages to report on.";
        }

        StringBuilder report = new StringBuilder();
        for (Message message : messages) {
            report.append(message.getDetails()).append("\n\n");
        }
        return report.toString().trim();
    }

    // Writes the stored messages array to a JSON file
    public void saveStoredMessages(Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(storedMessages, writer);
        }
    }

    // Reads the JSON file into the stored messages array.
    // Messages that are already in the arrays (same ID) are skipped.
    // Returns the messages that were read from the file.
    public List<Message> loadStoredMessages(Path file) throws IOException {
        List<Message> loaded = new ArrayList<>();

        if (!Files.exists(file)) {
            return loaded;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Message[] fromFile = gson.fromJson(reader, Message[].class);

            if (fromFile == null) {
                return loaded;
            }

            for (Message message : fromFile) {
                loaded.add(message);

                if (!messageIDs.contains(message.getMessageID())) {
                    addMessage(message, STORE);
                    lastMessageNumber = Math.max(lastMessageNumber, message.getMessageNumber());
                }
            }
        }
        return loaded;
    }
}
