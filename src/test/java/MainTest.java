import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// Tests for the registration, login and message checks from Parts 1 and 2
class MainTest {

    @Test
    void usernameMustBeFiveCharactersWithUnderscore() {
        assertTrue(Main.checkUserName("kyl_1"));
        assertFalse(Main.checkUserName("kyle1"));
        assertFalse(Main.checkUserName("kyle_!!!"));
    }

    @Test
    void passwordMustMeetComplexityRules() {
        assertTrue(Main.checkPasswordComplexity("Ch&&sec@ke99!"));
        assertFalse(Main.checkPasswordComplexity("password"));
        assertFalse(Main.checkPasswordComplexity("Short1!"));
    }

    @Test
    void cellNumberMustHaveInternationalCode() {
        assertTrue(Main.checkCellPhoneNumber("+27838968976"));
        assertFalse(Main.checkCellPhoneNumber("08966553"));
    }

    @Test
    void loginStatusMessages() {
        assertEquals("Welcome, it is great to see you again.", Main.returnLoginStatus(true));
        assertEquals("Username or password incorrect, please try again.", Main.returnLoginStatus(false));
    }

    @Test
    void messageLengthIsChecked() {
        assertTrue(Message.isMessageLengthValid("a".repeat(250)));
        assertFalse(Message.isMessageLengthValid("a".repeat(251)));
        assertEquals("Message exceeds 250 characters. Please shorten it.",
                Message.checkMessageLength("a".repeat(251)));
    }

    @Test
    void recipientNumberIsChecked() {
        assertTrue(Message.isRecipientValid("+27718693002"));
        assertFalse(Message.isRecipientValid("08575975889"));
    }

    @Test
    void messageHashUsesFirstAndLastWord() {
        Message message = new Message(1, "Developer", "+27718693002", "Hi tonight");

        assertEquals("0000000001", message.getMessageID());
        assertEquals("00:1:HITONIGHT", message.getMessageHash());
    }

    @Test
    void messageOptionConfirmations() {
        assertEquals("Message successfully sent.", Message.sentMessage(1));
        assertEquals("Message disregarded.", Message.sentMessage(2));
        assertEquals("Message stored to send later.", Message.sentMessage(3));
    }
}
