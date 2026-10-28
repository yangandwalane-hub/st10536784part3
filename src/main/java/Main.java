import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner input = new Scanner(System.in);

    // The registered user's details
    static String registeredUsername;
    static String registeredPassword;

    // All the message arrays live in the store
    static MessageStore store = new MessageStore();

    // JSON file that the stored messages are saved to and read from
    static final Path STORED_MESSAGES_FILE = Path.of("storedMessages.json");

    // Username: exactly 5 characters and contains an underscore
    public static boolean checkUserName(String username) {
        return username.length() == 5 && username.contains("_");
    }

    // Password: 8+ characters, capital, lowercase, number and special character
    public static boolean checkPasswordComplexity(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    // Cell number: +27 followed by 9 digits
    public static boolean checkCellPhoneNumber(String number) {
        return number.matches("\\+27[0-9]{9}");
    }

    public static String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome, it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    // Reads a whole number and asks again if the user types something that is not a number
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    // Keeps asking until each detail is valid
    public static void registerUser() {
        String username;

        do {
            System.out.print("Enter your username: ");
            username = input.nextLine();

            if (!checkUserName(username)) {
                System.out.println(
                    "Username is incorrectly formatted. Please try again"
                );
            }
        } while (!checkUserName(username));

        String password;

        do {
            System.out.print("Enter your password: ");
            password = input.nextLine();

            if (!checkPasswordComplexity(password)) {
                System.out.println(
                    "Password is incorrectly formatted. Please try again"
                );
            }
        } while (!checkPasswordComplexity(password));

        String cellPhone;

        do {
            System.out.print("Enter your cellphone number (+27): ");
            cellPhone = input.nextLine();

            if (!checkCellPhoneNumber(cellPhone)) {
                System.out.println(
                    "Cellphone number is incorrectly formatted. Please try again"
                );
            }
        } while (!checkCellPhoneNumber(cellPhone));

        registeredUsername = username;
        registeredPassword = password;

        System.out.println("User registered successfully.");
    }

    // Keeps asking until the login details match
    public static void loginUser() {
        boolean loggedIn = false;

        while (!loggedIn) {
            System.out.print("Enter your username to login: ");
            String username = input.nextLine();

            System.out.print("Enter your password to login: ");
            String password = input.nextLine();

            loggedIn = username.equals(registeredUsername)
                    && password.equals(registeredPassword);

            System.out.println(returnLoginStatus(loggedIn));
        }
    }

    // SEND MESSAGES
    public static void sendMessages() {
        int numMessages;

        do {
            numMessages = readInt(
                "How many messages do you want to enter? "
            );

            if (numMessages <= 0) {
                System.out.println(
                    "Please enter a number greater than 0."
                );
            }
        } while (numMessages <= 0);

        for (int i = 0; i < numMessages; i++) {
            System.out.println(
                "--- Message " + (i + 1) + " of " + numMessages + " ---"
            );

            String recipient;

            do {
                System.out.print(
                    "Enter the recipient cell number (with international code): "
                );

                recipient = input.nextLine();

                if (!Message.isRecipientValid(recipient)) {
                    System.out.println(
                        "Cell phone number is incorrectly formatted. Please try again."
                    );
                }
            } while (!Message.isRecipientValid(recipient));

            String text;

            do {
                System.out.print(
                    "Enter your message (max 250 characters): "
                );

                text = input.nextLine();

                System.out.println(Message.checkMessageLength(text));

            } while (!Message.isMessageLengthValid(text) || text.isBlank());

            Message message = store.createMessage(registeredUsername, recipient, text);

            System.out.println(
                "Message ID generated: " + message.getMessageID()
            );

            int choice;

            do {
                System.out.println("1) Send Message");
                System.out.println("2) Disregard Message");
                System.out.println("3) Store Message to send later");

                choice = readInt("Choose an option: ");

                if (choice < 1 || choice > 3) {
                    System.out.println(
                        "Invalid option, please choose 1, 2 or 3."
                    );
                }
            } while (choice < 1 || choice > 3);

            store.addMessage(message, choice);

            System.out.println(Message.sentMessage(choice));

            if (choice == MessageStore.SEND) {
                System.out.println(message.getDetails());
            } else if (choice == MessageStore.STORE) {
                saveStoredMessages();
            }
        }

        System.out.println("Total number of messages sent: " + store.totalSent());
    }

    // Saves the stored messages array to the JSON file
    public static void saveStoredMessages() {
        try {
            store.saveStoredMessages(STORED_MESSAGES_FILE);
        } catch (IOException e) {
            System.out.println("Could not save the stored messages: " + e.getMessage());
        }
    }

    // Reads the JSON file into the stored messages array and shows what was read
    public static void loadStoredMessages() {
        try {
            List<Message> loaded = store.loadStoredMessages(STORED_MESSAGES_FILE);

            if (loaded.isEmpty()) {
                System.out.println("No stored messages were found in " + STORED_MESSAGES_FILE + ".");
            } else {
                System.out.println("Read " + loaded.size() + " message(s) from "
                        + STORED_MESSAGES_FILE + ":");
                System.out.println(store.displayReport(loaded));
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not read the stored messages: " + e.getMessage());
        }
    }

    // STORED MESSAGES MENU
    public static void storedMessagesMenu() {
        int option = 0;

        while (option != 7) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages sent or stored for a recipient");
            System.out.println("5) Delete a message using its message hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Back to main menu");

            option = readInt("Choose an option: ");

            switch (option) {
                case 1:
                    System.out.println(store.displaySendersAndRecipients(store.getStoredMessages()));
                    break;
                case 2:
                    String longest = store.longestMessage(store.getStoredMessages());
                    System.out.println(longest.isEmpty()
                            ? "There are no stored messages."
                            : "Longest stored message: " + longest);
                    break;
                case 3:
                    System.out.print("Enter the message ID: ");
                    System.out.println(store.searchByMessageID(input.nextLine().trim()));
                    break;
                case 4:
                    System.out.print("Enter the recipient cell number: ");
                    List<String> found = store.searchByRecipient(input.nextLine().trim());
                    System.out.println(found.isEmpty()
                            ? "No messages found for that recipient."
                            : String.join("\n", found));
                    break;
                case 5:
                    System.out.print("Enter the message hash: ");
                    System.out.println(store.deleteByMessageHash(input.nextLine().trim()));
                    saveStoredMessages();
                    break;
                case 6:
                    System.out.println(store.displayReport(store.getStoredMessages()));
                    break;
                case 7:
                    break;
                default:
                    System.out.println("Invalid option, please choose 1 to 7.");
            }
        }
    }

    public static void main(String[] args) {
        registerUser();
        loginUser();

        System.out.println("Welcome to QuickChat.");

        // Fill the stored messages array from the JSON file
        loadStoredMessages();

        int option = 0;

        while (option != 3) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");

            option = readInt("Choose an option: ");

            if (option == 1) {
                sendMessages();
            } else if (option == 2) {
                System.out.println(store.displayReport(store.getSentMessages()));
            } else if (option == 4) {
                storedMessagesMenu();
            } else if (option != 3) {
                System.out.println(
                    "Invalid option, please choose 1, 2, 3 or 4."
                );
            }
        }

        System.out.println("Goodbye.");
        input.close();
    }
}
