import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class RecognizeApp {
    //When Honor initializes, we should try to open a connection
    Connection conn = null;
    public RecognizeApp() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("app.properties");
        } catch (FileNotFoundException e) {
            //System.out.println("File not found in current directory, looking in src directory");
            try {
                input = new FileInputStream("src/app.properties");
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(e);
            }
        }
        try {
            prop.load(input);

            String databaseUrl = prop.getProperty("database.url");
            String databaseUsername = prop.getProperty("database.username");
            String databasePassword = prop.getProperty("database.password");

            conn = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
        } catch (SQLException ex) {
            // handle any errors
            /*
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            */
            throw new RuntimeException(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /////////////////////
    // Viewing Commands//
    /////////////////////
    public void printTopFive() throws SQLException {
        System.out.println("Here are our five most recent honor recipients:");
        // Create a PreparedStatement
        String sql = "SELECT Users.Username AS RecipientUsername " +
                "FROM RecentGiftRecipients " +
                "JOIN Users ON RecentGiftRecipients.RecipientID = Users.UserID";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);

        // Execute the SELECT statement
        ResultSet resultSet = preparedStatement.executeQuery();

        // Process the ResultSet
        int count = 0;
        while (resultSet.next()) {
            count +=1;
            String userName = resultSet.getString("RecipientUsername");
            System.out.println("- Recipient " + count + ": " + userName);
        }
        preparedStatement.close();
        resultSet.close();
    }
    public void viewAllUsers() throws SQLException {
        String sql = "SELECT * FROM Users";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);

        // Execute the SELECT statement
        ResultSet resultSet = preparedStatement.executeQuery();

        // Process the ResultSet
        while (resultSet.next()) {
            int userID = resultSet.getInt("UserID");
            String userName = resultSet.getString("Username");
            int recognitionCount = resultSet.getInt("RecognitionCount");
            int giftCount = resultSet.getInt("GiftCount");
            System.out.println("User " + userID + ": " + userName + " has " + recognitionCount + " recognitions and " + giftCount + " gifts");
        }
        preparedStatement.close();
        resultSet.close();
    }
    public void viewGiftsForUser() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        String sql = "SELECT g.Name, g.Description, g.Value, u2.Username\n" +
                    "FROM SendGift sg\n" +
                    "JOIN Gifts g ON sg.GiftID = g.GiftID\n" +
                    "JOIN Users u ON sg.RecipientID = u.UserID\n" +
                    "JOIN Users u2 ON sg.SenderID = u2.UserID\n" +
                    "WHERE u.Username = ?";

        System.out.println("Who would you like to view the gifts for? (Enter exact username)");
        String username = scanner.nextLine();

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);

        // Execute the SELECT statement
        ResultSet resultSet = preparedStatement.executeQuery();

        System.out.println(username + " Has the following gifts:");
        // Process the ResultSet
        while (resultSet.next()) {
            String giftName = resultSet.getString("Name");
            String giftDescription = resultSet.getString("Description");
            int giftPrice = resultSet.getInt("Value");
            String sender = resultSet.getString("Username");
            System.out.println("From " + sender + ":\t" + giftName + " | $" + giftPrice + " | " + giftDescription);
        }
        preparedStatement.close();
        resultSet.close();
    }
    public void viewAppreciationsForUser() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        String sql = "SELECT a.HelpfulnessRating, a.Message, a.Date, u2.Username\n" +
                "FROM WriteAppreciation wa \n" +
                "JOIN Appreciation a ON wa.AppreciationID = a.AppreciationID\n" +
                "JOIN Users u ON wa.RecipientID = u.UserID\n" +
                "JOIN Users u2 ON wa.SenderID = u2.UserID\n" +
                "WHERE u.Username = ?";

        System.out.println("Who would you like to view the gifts for? (Enter exact username)");
        String username = scanner.nextLine();

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);

        // Execute the SELECT statement
        ResultSet resultSet = preparedStatement.executeQuery();

        System.out.println(username + " Has the following appreciations:\n");
        // Process the ResultSet
        while (resultSet.next()) {
            int helpfulnessRating = resultSet.getInt("HelpfulnessRating");
            String message = resultSet.getString("Message");
            Date date = resultSet.getDate("Date");
            String sender = resultSet.getString("Username");
            System.out.println("From: " + sender + " on " + date.toString() + " | Rating: " + helpfulnessRating + "/5");
            System.out.println(message + "\n");
        }
        preparedStatement.close();
        resultSet.close();
    }
    public void viewSharesForUser() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        String sql = "SELECT su.Username\n" +
                     "FROM Shares s\n" +
                     "JOIN Users u ON s.UserID = u.UserID\n" +
                     "JOIN Users su ON s.HelperID = su.UserID\n" +
                     "WHERE u.Username = ?";

        System.out.println("Who would you like to view the shares list for? (Enter exact username)");
        String username = scanner.nextLine();

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);

        // Execute the SELECT statement
        ResultSet resultSet = preparedStatement.executeQuery();

        System.out.println(username + " Has shared the following users:");
        // Process the ResultSet
        while (resultSet.next()) {
            String sharee = resultSet.getString("Username");
            System.out.println("\t" + sharee);
        }
        preparedStatement.close();
        resultSet.close();
    }
    public void viewFollowingForUser() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        String sql = "SELECT su.Username\n" +
                "FROM Follows s\n" +
                "JOIN Users u ON s.UserID = u.UserID\n" +
                "JOIN Users su ON s.HelperID = su.UserID\n" +
                "WHERE u.Username = ?";

        System.out.println("Who would you like to view the following list for? (Enter exact username)");
        String username = scanner.nextLine();

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);

        // Execute the SELECT statement
        ResultSet resultSet = preparedStatement.executeQuery();

        System.out.println(username + " follows the following users:");
        // Process the ResultSet
        while (resultSet.next()) {
            String sharee = resultSet.getString("Username");
            System.out.println("\t" + sharee);
        }
        preparedStatement.close();
        resultSet.close();
    }
    public void viewTopHonorees() throws SQLException {
        String sql = "SELECT * FROM TopUsersView";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);

        // Execute the SELECT statement
        ResultSet resultSet = preparedStatement.executeQuery();

        System.out.println("The Top Three Honorees are");
        // Process the ResultSet
        int count = 1;
        while (resultSet.next()) {
            String username = resultSet.getString("Username");
            int giftCount = resultSet.getInt("GiftCount");
            int recognitionCount = resultSet.getInt("RecognitionCount");
            System.out.println("\t" + count + ". " + username + " | " + giftCount + " Gifts | " + recognitionCount + " Appreciations");
            count++;
        }
        preparedStatement.close();
        resultSet.close();
    }
    ////////////////////
    // Update Commands//
    ////////////////////
    public void addUser() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("What is this user's email?");
        String email = scanner.nextLine();
        System.out.println("What is this user's password?");
        String password = scanner.nextLine();
        System.out.println("What is this user's username?");
        String username = scanner.nextLine();

        // SQL INSERT statement with prepared statement
        String sql = "INSERT INTO Users (email, password, username) VALUES (?, ?, ?)";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        // Set the values for the prepared statement
        preparedStatement.setString(1, email);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3, username);

        // Execute the statement
        preparedStatement.executeUpdate();
        System.out.println("User " + username + "added successfully!");
    }
    public void deleteUser() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which user to delete? (Enter exact username)");
        String username = scanner.nextLine();
        // SQL INSERT statement with prepared statement
        String sql = "DELETE FROM Users WHERE Username = ?";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        // Set the values for the prepared statement
        preparedStatement.setString(1, username);

        // Execute the statement
        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected == 0) {
            System.out.println("\"" + username + "\" Not in database");
        } else {
            System.out.println("User \"" + username + "\" deleted successfully");
        }
    }
    public void addGift() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Who is sending the gift?");
        String sender = scanner.nextLine();
        System.out.println("Who is the gift for?");
        String receiver = scanner.nextLine();

        System.out.println("What is the name of the gift?");
        String giftName = scanner.nextLine();
        System.out.println("Write a description for the gift.");
        String giftDesc = scanner.nextLine();
        System.out.println("What is the value of the gift? (In $)");
        String giftValue = scanner.nextLine();

        // SQL INSERT statement with prepared statement
        String sql = "CALL SendGiftProcedure(?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        // Set the values for the prepared statement
        preparedStatement.setString(1, sender);
        preparedStatement.setString(2, receiver);
        preparedStatement.setString(3, giftName);
        preparedStatement.setString(4, giftDesc);
        preparedStatement.setString(5, giftValue);

        // Execute the statement
        preparedStatement.executeUpdate();
        System.out.println("Gift: " + giftName + " from " + sender + " to " + receiver + " added successfully");
    }
    public void addAppreciation() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Who is sending the appreciation?");
        String sender = scanner.nextLine();
        System.out.println("Who is the appreciation for?");
        String receiver = scanner.nextLine();

        System.out.println("How would you rate " + receiver);
        String rating = scanner.nextLine();
        System.out.println("Write a message for " + receiver);
        String message = scanner.nextLine();

        // SQL INSERT statement with prepared statement
        String sql = "CALL SendAppreciationProcedure(?, ?, ?, ?)";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        // Set the values for the prepared statement
        preparedStatement.setString(1, sender);
        preparedStatement.setString(2, receiver);
        preparedStatement.setInt(3, Integer.parseInt(rating));
        preparedStatement.setString(4, message);

        // Execute the statement
        preparedStatement.executeUpdate();

        System.out.println("Appreciation for " + receiver + " has been sent successfully");
    }
    public void follow() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Pick a user to alter");
        String user = scanner.nextLine();
        System.out.println("Who should this user follow?");
        String helper = scanner.nextLine();

        String sql = "INSERT INTO Follows (UserID, HelperID)\n" +
                "SELECT u1.UserID, u2.UserID\n" +
                "FROM Users u1\n" +
                "JOIN Users u2 ON u2.Username = ? AND u1.Username = ?";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        // Set the values for the prepared statement
        preparedStatement.setString(1, helper);
        preparedStatement.setString(2, user);

        // Execute the statement
        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected == 0) {
            System.out.println("Follow failed");
        } else {
            System.out.println(user + " is now following " + helper);
        }
    }
    public void share () throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Pick a user to alter");
        String user = scanner.nextLine();
        System.out.println("Who should this user share?");
        String helper = scanner.nextLine();

        String sql = "INSERT INTO Shares (UserID, HelperID)\n" +
                "SELECT u1.UserID, u2.UserID\n" +
                "FROM Users u1\n" +
                "JOIN Users u2 ON u2.Username = ? AND u1.Username = ?";

        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        // Set the values for the prepared statement
        preparedStatement.setString(1, helper);
        preparedStatement.setString(2, user);

        // Execute the statement
        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected == 0) {
            System.out.println("Share failed");
        } else {
            System.out.println(user + " shared " + helper);
        }
    }
    ////////////////////////
    //General Running Loop//
    ////////////////////////
    public void selectOptions() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int choice;
        boolean exitCond = false;

        while (!exitCond) {
            System.out.println("\n-----------------------------\nWhat would you like to do? (Enter just a number)");
            System.out.println("(1) Update Database");
            System.out.println("(2) View Database");
            System.out.println("(3) Exit");
            // Process the user's choice
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("(1) Add New User");
                    System.out.println("(2) Delete User");
                    System.out.println("(3) Add New Gift");
                    System.out.println("(4) Add New Appreciation");
                    System.out.println("(5) Follow a User");
                    System.out.println("(6) Share a User");
                    choice = scanner.nextInt();
                    switch (choice) {
                        case 1:
                            System.out.println("Adding User");
                            addUser();
                            break;
                        case 2:
                            System.out.println("Deleting User");
                            deleteUser();
                            break;
                        case 3:
                            System.out.println("Add Gift");
                            addGift();
                            break;
                        case 4:
                            System.out.println("Add Appreciation");
                            addAppreciation();
                            break;
                        case 5:
                            System.out.println("Adding a follower to a user");
                            follow();
                            break;
                        case 6:
                            System.out.println("Adding a share to a user");
                            share();
                            break;
                        default:
                            System.out.println("Invalid choice. Please enter a valid number.");
                            break;
                    }
                    break;
                case 2:
                    // Call a method to view the database
                    System.out.println("(1) View All Users");
                    System.out.println("(2) View Gifts Received by User");
                    System.out.println("(3) View Appreciations Received by User");
                    System.out.println("(4) View Followers for a User");
                    System.out.println("(5) View Shares for a User");
                    System.out.println("(6) View top 3 Honorees");
                    choice = scanner.nextInt();
                    switch (choice) {
                        case 1:
                            System.out.println("Viewing All Users");
                            viewAllUsers();
                            break;
                        case 2:
                            System.out.println("Viewing Gifts Received by User");
                            viewGiftsForUser();
                            break;
                        case 3:
                            System.out.println("Viewing Appreciations for User");
                            viewAppreciationsForUser();
                            break;
                        case 4:
                            System.out.println("Viewing Followers for User");
                            viewFollowingForUser();
                            break;
                        case 5:
                            System.out.println("Viewing Shares for User");
                            viewSharesForUser();
                            break;
                        case 6:
                            System.out.println("Viewing top 3 honorees");
                            viewTopHonorees();
                            break;
                        default:
                            System.out.println("Invalid choice. Please enter a valid number.");
                            break;
                    }
                    break;
                case 3:
                    System.out.println("Exiting");
                    exitCond = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid number.");
            }
        }
        // Close the scanner to avoid resource leak
        scanner.close();
    }
    public void run() {
        System.out.println("Welcome to the Honor Admin Control Panel!");
        try {
            printTopFive();
            selectOptions();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    public static void main(String[] args) {
        RecognizeApp app = new RecognizeApp();
        app.run();
    }
}