import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageStoreTest {

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    private MessageStore store;
    private Message message4;

    // Test data messages 1-5 are added by the developer, not typed in
    @BeforeEach
    void setUp() {
        store = new MessageStore();

        store.addMessage(store.createMessage("Developer", "+27834557896", TEXT_1), MessageStore.SEND);
        store.addMessage(store.createMessage("Developer", "+27838884567", TEXT_2), MessageStore.STORE);
        store.addMessage(store.createMessage("Developer", "+27834484567", TEXT_3), MessageStore.DISREGARD);

        message4 = store.createMessage("Developer", "0838884567", TEXT_4);
        store.addMessage(message4, MessageStore.SEND);

        store.addMessage(store.createMessage("Developer", "+27838884567", TEXT_5), MessageStore.STORE);
    }

    private List<String> texts(List<Message> messages) {
        return messages.stream().map(Message::getMessageText).toList();
    }

    @Test
    void sentMessagesArrayCorrectlyPopulated() {
        List<String> expected = Arrays.asList(TEXT_1, TEXT_4);

        assertEquals(expected, texts(store.getSentMessages()));
    }

    @Test
    void disregardedAndStoredArraysCorrectlyPopulated() {
        assertEquals(Arrays.asList(TEXT_3), texts(store.getDisregardedMessages()));
        assertEquals(Arrays.asList(TEXT_2, TEXT_5), texts(store.getStoredMessages()));
    }

    @Test
    void messageIDAndHashArraysCorrectlyPopulated() {
        assertEquals(Arrays.asList("0000000001", "0000000002", "0000000003",
                "0000000004", "0000000005"), store.getMessageIDs());

        assertEquals("00:1:DIDCAKE?", store.getMessageHashes().get(0));
        assertEquals("00:2:WHERETIME.", store.getMessageHashes().get(1));
        assertEquals("00:4:ITTIME!", store.getMessageHashes().get(3));
    }

    @Test
    void displayLongestMessage() {
        assertEquals(TEXT_2, store.longestMessage(store.getAllMessages()));
        assertEquals(TEXT_2, store.longestMessage(store.getStoredMessages()));
    }

    @Test
    void longestMessageOfEmptyListIsEmpty() {
        assertEquals("", store.longestMessage(new MessageStore().getStoredMessages()));
    }

    @Test
    void searchForMessageID() {
        String expected = "Recipient: 0838884567\nMessage: " + TEXT_4;

        assertEquals(expected, store.searchByMessageID(message4.getMessageID()));
    }

    @Test
    void searchForMessageIDThatDoesNotExist() {
        assertEquals("Message ID 0000000099 was not found.",
                store.searchByMessageID("0000000099"));
    }

    @Test
    void searchAllMessagesSentOrStoredForARecipient() {
        List<String> expected = Arrays.asList(TEXT_2, TEXT_5);

        assertEquals(expected, store.searchByRecipient("+27838884567"));
    }

    @Test
    void searchForRecipientIgnoresDisregardedMessages() {
        assertTrue(store.searchByRecipient("+27834484567").isEmpty());
    }

    @Test
    void deleteMessageUsingMessageHash() {
        String hash = store.getMessageHashes().get(1);
        String expected = "Message: \"" + TEXT_2 + "\" successfully deleted.";

        assertEquals(expected, store.deleteByMessageHash(hash));

        assertEquals(Arrays.asList(TEXT_5), texts(store.getStoredMessages()));
        assertEquals(4, store.getMessageIDs().size());
        assertFalse(store.getMessageHashes().contains(hash));
    }

    @Test
    void deleteMessageWithUnknownHash() {
        assertEquals("No message with hash 00:9:NOPE was found.",
                store.deleteByMessageHash("00:9:NOPE"));
    }

    @Test
    void displayReportShowsHashRecipientAndMessage() {
        String report = store.displayReport(store.getSentMessages());

        assertTrue(report.contains("Message Hash: 00:1:DIDCAKE?"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: " + TEXT_1));
        assertTrue(report.contains("Message Hash: 00:4:ITTIME!"));
        assertTrue(report.contains("Recipient: 0838884567"));
        assertTrue(report.contains("Message: " + TEXT_4));
        assertFalse(report.contains(TEXT_2));
    }

    @Test
    void displaySenderAndRecipientOfStoredMessages() {
        String expected = "Sender: Developer | Recipient: +27838884567\n"
                + "Sender: Developer | Recipient: +27838884567";

        assertEquals(expected, store.displaySendersAndRecipients(store.getStoredMessages()));
    }

    @Test
    void storedMessagesAreSavedToAndReadFromJson(@TempDir Path folder) throws IOException {
        Path file = folder.resolve("storedMessages.json");
        store.saveStoredMessages(file);

        MessageStore newStore = new MessageStore();
        List<Message> loaded = newStore.loadStoredMessages(file);

        assertEquals(Arrays.asList(TEXT_2, TEXT_5), texts(loaded));
        assertEquals(Arrays.asList(TEXT_2, TEXT_5), texts(newStore.getStoredMessages()));
        assertEquals(store.getStoredMessages().get(0).getMessageHash(),
                newStore.getMessageHashes().get(0));

        // Reading the same file again must not add duplicates
        newStore.loadStoredMessages(file);
        assertEquals(2, newStore.getStoredMessages().size());

        // New messages carry on after the highest loaded number
        assertEquals("0000000006", newStore.createMessage("Developer", "+27830000000", "Hi").getMessageID());
    }

    @Test
    void loadingAMissingJsonFileReturnsNothing(@TempDir Path folder) throws IOException {
        assertTrue(new MessageStore().loadStoredMessages(folder.resolve("missing.json")).isEmpty());
    }
}
