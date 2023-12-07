CREATE DATABASE HonorDB;
USE HonorDB;

CREATE TABLE Users(
    UserID Int AUTO_INCREMENT PRIMARY KEY,
    Email Varchar(25),
    Password Varchar(25),
    Username Varchar(25) UNIQUE KEY,
    RecognitionCount Int DEFAULT 0,
    GiftCount Int DEFAULT 0
);

CREATE TABLE Gifts(
	GiftID Int AUTO_INCREMENT PRIMARY KEY,
	Name Varchar(25),
	Description Varchar(200),
	Value Int
);

CREATE TABLE Appreciation(
	AppreciationID Int AUTO_INCREMENT Key,
	HelpfulnessRating Int,
	Message Varchar(200),
	Date DATE
);

CREATE TABLE Follows(
	UserID Int REFERENCES Users(UserID),
	HelperID Int REFERENCES Users(UserID)
);
CREATE TABLE Shares(
	UserID Int REFERENCES Users(UserID),
	HelperID Int REFERENCES Users(UserID)
);
CREATE TABLE SendGift(
	SenderID Int REFERENCES Users(UserID),
	GiftID Int REFERENCES Gifts(GiftID),
	RecipientID Int REFERENCES Users(UserID)
);
CREATE TABLE WriteAppreciation(
	SenderID Int REFERENCES Users(UserID),
	AppreciationID Int REFERENCES Appreciation(AppreciationID),
	RecipientID Int REFERENCES Users(UserID)
);
CREATE TABLE RecentGiftRecipients (
    ID Int AUTO_INCREMENT PRIMARY KEY,
    RecipientID INT REFERENCES Users(UserID)
);

DELIMITER //
CREATE TRIGGER increment_gift_count AFTER INSERT ON SendGift
FOR EACH ROW
BEGIN
    UPDATE Users
    SET GiftCount = GiftCount + 1
    WHERE UserID = NEW.RecipientID;
END;

CREATE TRIGGER increment_recognition_count AFTER INSERT ON WriteAppreciation
FOR EACH ROW
BEGIN
    UPDATE Users
    SET RecognitionCount = RecognitionCount + 1
    WHERE UserID = NEW.RecipientID;
END//

CREATE TRIGGER update_recent_sendgift_recipients AFTER INSERT ON SendGift
FOR EACH ROW
BEGIN
    -- Insert the new sendgift information into the RecentSendGiftRecipients table
        INSERT INTO RecentGiftRecipients (RecipientID)
        VALUES (NEW.RecipientID);

        -- Create a temporary table to store the IDs of the records to be deleted
        CREATE TEMPORARY TABLE TempTableToDelete (ID INT);

        -- Populate the temporary table with IDs that need to be deleted
        INSERT INTO TempTableToDelete (ID)
        SELECT ID
        FROM RecentGiftRecipients
        ORDER BY ID DESC
        LIMIT 1 OFFSET 5;

        -- Delete records from RecentSendGiftRecipients using the temporary table
        DELETE FROM RecentGiftRecipients
        WHERE ID IN (SELECT ID FROM TempTableToDelete);

        -- Drop the temporary table
        DROP TEMPORARY TABLE IF EXISTS TempTableToDelete;
END//

CREATE PROCEDURE SendGiftProcedure(
    IN sender_username VARCHAR(25),
    IN receiver_username VARCHAR(25),
    IN gift_name VARCHAR(25),
    IN gift_description VARCHAR(200),
    IN gift_value INT
)
BEGIN
    DECLARE sender_id, receiver_id, gift_id INT;

    -- Get SenderID
    SELECT UserID INTO sender_id
    FROM Users
    WHERE Username = sender_username;

    -- Get ReceiverID
    SELECT UserID INTO receiver_id
    FROM Users
    WHERE Username = receiver_username;

    -- Insert into Gifts table
    INSERT INTO Gifts (Name, Description, Value)
    VALUES (gift_name, gift_description, gift_value);

    -- Get GiftID
    SELECT LAST_INSERT_ID() INTO gift_id;

    -- Insert into SendGift table
    INSERT INTO SendGift (SenderID, GiftID, RecipientID)
    VALUES (sender_id, gift_id, receiver_id);
END//

CREATE PROCEDURE SendAppreciationProcedure(
    IN sender_username VARCHAR(25),
    IN receiver_username VARCHAR(25),
    IN rating INT,
    IN message VARCHAR(200)
)
BEGIN
    DECLARE sender_id, receiver_id, appreciation_id INT;

    -- Get SenderID
    SELECT UserID INTO sender_id
    FROM Users
    WHERE Username = sender_username;

    -- Get ReceiverID
    SELECT UserID INTO receiver_id
    FROM Users
    WHERE Username = receiver_username;

    -- Insert into Gifts table
    INSERT INTO Appreciation (HelpfulnessRating, Message, Date)
    VALUES (rating, message, NOW());

    -- Get Appreciation
    SELECT LAST_INSERT_ID() INTO appreciation_id;

    -- Insert into SendGift table
    INSERT INTO WriteAppreciation (SenderID, AppreciationID, RecipientID)
    VALUES (sender_id, appreciation_id, receiver_id);
END//

DELIMITER ;

CREATE VIEW TopUsersView AS
SELECT UserID, Username, RecognitionCount, GiftCount, (RecognitionCount + GiftCount) AS TotalGiftsRecognitions
FROM Users
ORDER BY TotalGiftsRecognitions DESC
LIMIT 3;

INSERT INTO Users (email, password, username) VALUES
("bob@gmail.com", "bob123", "Bob Smith"),
("jeremy@gmail.com", "jeremy123", "Jeremy Fitzgerald"),
("collin@gmail.com", "collin123", "Collin Ray"),
("pollett@gmail.com", "pollett123", "Chris Pollett"),
("justin@gmail.com", "123justin", "Justin Mok"),
("draven@gmail.com", "draven123", "Draven Lee"),
("henry@gmail.com", "henry123", "Henry Crow");

-- Insert data into the Gifts table
INSERT INTO Gifts (Name, Description, Value) VALUES
('Gold Ring', '$1000 of 24 Karat goodness', 1000),
('Banana', 'High in potassium', 20),
('Laptop', 'Now you can do your CS157 Homework!', 300),
('Pet Cat', 'I dont think you should be gifting animals..', 200),
('Couch', 'I hope this comes with movers', 1200),
('Tinder Subscription', 'You are kinda lonely so here you go', 15);

-- Insert data into the Appreciation table
INSERT INTO Appreciation (HelpfulnessRating, Message, Date) VALUES
(2, 'Great help!', '2023-01-01'),
(3, 'Excellent assistance!', '2023-02-15'),
(4, 'Thanks a lot!', '2023-03-20'),
(5, 'I love chris pollett', '2023-03-25');

-- Insert data into the Follows table
-- User1 follows User2
INSERT INTO Follows VALUES
(1, 2),
(2, 3),
(3, 4),
(4, 5);

-- Insert data into the Shares table
-- User1 shares User2
INSERT INTO Shares VALUES
(1, 3),
(2, 4),
(3, 5),
(4, 6);

-- Insert data into the SendGift table
-- User1 sends GiftID to User2
INSERT INTO SendGift VALUES
(1, 1, 2),
(2, 2, 3),
(3, 3, 4),
(4, 4, 5),
(1, 5, 3),
(1, 6, 4);

-- Insert data into the WriteAppreciation table
-- User1 sends AppreciationID to User2
INSERT INTO WriteAppreciation VALUES
(1, 1, 2),
(2, 2, 3),
(3, 3, 4),
(1, 4, 4);


