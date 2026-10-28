// A single chat message with its generated ID and hash
public class Message {

    private final int messageNumber;
    private final String messageID;
    private final String sender;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    public Message(int messageNumber, String sender, String recipient, String messageText) {
        this.messageNumber = messageNumber;
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;

        // 10-digit message ID, e.g. 0000000001
        this.messageID = String.format("%010d", messageNumber);
        this.messageHash = createMessageHash();
    }

    // Recipient: +27 followed by 9 digits
    public static boolean isRecipientValid(String recipient) {
        return recipient.matches("\\+27[0-9]{9}");
    }

    public static boolean isMessageLengthValid(String text) {
        return text.length() <= 250;
    }

    public static String checkMessageLength(String text) {
        if (isMessageLengthValid(text)) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters. Please shorten it.";
    }

    // Hash format: first two digits of the ID : message number : FIRSTWORDLASTWORD
    private String createMessageHash() {
        String[] words = messageText.trim().split("\\s+");
        String firstWord = words[0];
        String lastWord = words[words.length - 1];

        return (messageID.substring(0, 2) + ":" + messageNumber + ":"
                + firstWord + lastWord).toUpperCase();
    }

    // Message text for the option the user chose (1 = send, 2 = disregard, 3 = store)
    public static String sentMessage(int choice) {
        if (choice == 1) {
            return "Message successfully sent.";
        } else if (choice == 2) {
            return "Message disregarded.";
        }
        return "Message stored to send later.";
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nSender: " + sender
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText;
    }
}
