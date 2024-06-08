CREATE DATABASE IF NOT EXISTS appdb;

USE appdb;

CREATE TABLE IF NOT EXISTS users (
    userid INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS score (
    userid INT,
    maxscore INT,
    date DATE,
    PRIMARY KEY (userid),
    FOREIGN KEY (userid) REFERENCES users(userid)
);

INSERT INTO users (username) VALUES ('Pippo'), ('Pluto'), ('Paperino');

INSERT INTO score (userid, maxscore, date) VALUES (1, 100, '2021-10-01'), (2, 150, '2021-10-05'), (3, 120, '2021-10-10');
