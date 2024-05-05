-- Replace this by the SQL code needed to create your database

CREATE TABLE users (
	username VARCHAR(512) NOT NULL,
	password VARCHAR(512) NOT NULL,
	name	 VARCHAR(512) NOT NULL,
	phone	 BIGINT NOT NULL,
	address	 VARCHAR(512) NOT NULL,
	banned BOOL NOT NULL,
	PRIMARY KEY(username)
);

INSERT INTO users (username, password, name, phone, address, banned) VALUES
('john_doe', 'password', 'John Doe', 1234567890, '123 Main St, City', false),
('jane_smith', 'letmein', 'Jane Smith', 9876543210, '456 Elm St, Town', false),
('peter_parker', 'iwantaccess', 'Peter Parker', 5551234567, '789 Oak Ave, Metropolis', false),
('alice_wonder', 'itsme', 'Alice Wonder', 9998887777, '321 Pine Rd, Wonderland', false),
('bob_jones', 'bidenslover', 'Bob Jones', 1112223333, '555 Vine St, Burgerland', false),
('admin', 'admin', 'admin', 1, 'Corporate', false);


CREATE TABLE auction (
    id     SERIAL,
    title     VARCHAR(512) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time     TIMESTAMP NOT NULL,
    status     BOOL NOT NULL,
    highest_bid DOUBLE PRECISION, 
    item_id     INTEGER NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE notification (
    message VARCHAR(512) NOT NULL,
    recipient_username VARCHAR(512) NOT NULL,
    noti_time TIMESTAMP NOT NULL,
    PRIMARY KEY(message, recipient_username),
    FOREIGN KEY (recipient_username) REFERENCES users(username)
);

CREATE TABLE item (
	id		 SERIAL,
	description	 TEXT NOT NULL,
	condition	 VARCHAR(512) NOT NULL,
	min_price	 DOUBLE PRECISION NOT NULL,
	users_username VARCHAR(512) NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE message (
	id		 BIGSERIAL,
	comment	 VARCHAR(512) NOT NULL,
	post_time	 TIMESTAMP NOT NULL,
	users_username VARCHAR(512) NOT NULL,
	auction_id	 INTEGER NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE bid (
	id		 SERIAL,
	amount	 DOUBLE PRECISION NOT NULL,
	bid_time	 TIMESTAMP NOT NULL,
	auction_id	 INTEGER NOT NULL,
	users_username VARCHAR(512) NOT NULL,
	valid BOOL NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE tokens (
    username VARCHAR(512) NOT NULL,
    token     VARCHAR(512) NOT NULL,
    timeout TIMESTAMP NOT NULL,
    PRIMARY KEY(username)
);

CREATE TABLE records (
    id                SERIAL,
    banned_user        VARCHAR(512) NOT NULL,
    banned_auction    INTEGER,
    banned_bid        INTEGER,
    PRIMARY KEY(id)
);

ALTER TABLE auction ADD CONSTRAINT auction_fk1 FOREIGN KEY (item_id) REFERENCES item(id);
ALTER TABLE item ADD CONSTRAINT item_fk1 FOREIGN KEY (users_username) REFERENCES users(username);
ALTER TABLE message ADD CONSTRAINT message_fk1 FOREIGN KEY (users_username) REFERENCES users(username);
ALTER TABLE message ADD CONSTRAINT message_fk2 FOREIGN KEY (auction_id) REFERENCES auction(id);
ALTER TABLE bid ADD CONSTRAINT bid_fk1 FOREIGN KEY (auction_id) REFERENCES auction(id);
ALTER TABLE bid ADD CONSTRAINT bid_fk2 FOREIGN KEY (users_username) REFERENCES users(username);
ALTER TABLE tokens ADD CONSTRAINT tokens_fk1 FOREIGN KEY (username) REFERENCES users(username);
ALTER TABLE records ADD CONSTRAINT records_fk1 FOREIGN KEY (banned_user) REFERENCES users(username);
ALTER TABLE records ADD CONSTRAINT records_fk2 FOREIGN KEY (banned_auction) REFERENCES auction(id);
ALTER TABLE records ADD CONSTRAINT records_fk3 FOREIGN KEY (banned_bid) REFERENCES bid(id);
COMMIT;