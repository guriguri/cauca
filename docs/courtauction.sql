/* CREATE USER */
-- 
-- CREATE USER 'courtauction'@'%' IDENTIFIED BY 'courtauction.1234';
-- GRANT ALL PRIVILEGES ON courtauction.* TO 'courtauction'@'%' WITH GRANT option;
-- FLUSH PRIVILEGES;
--
/* CREATE DATABASE */
-- CREATE DATABASE courtauction;

CREATE TABLE courtauction (
	id INT(11) NOT NULL AUTO_INCREMENT,
	caNo VARCHAR(16) NOT NULL,
	caDesc VARCHAR(100),
	itemNo INT NOT NULL,
	itemType VARCHAR(16) NOT NULL,
	addr0 VARCHAR(16) NOT NULL,
	addr1 VARCHAR(16) NOT NULL,
	addr2 VARCHAR(16) NOT NULL,
	addr VARCHAR(100),	
	addrInfo VARCHAR(100),
	remarks VARCHAR(100),
	value INT(11) NOT NULL,
	valueMin INT(11) NOT NULL,
	auctionInfo VARCHAR(64) NOT NULL,
	auctionTel VARCHAR(64) NOT NULL,
	auctionDate DATE NOT NULL,
	auctionLoc VARCHAR(64) NOT NULL,
	status VARCHAR(8) NOT NULL,
	PRIMARY KEY (id)
);

CREATE UNIQUE INDEX COURTAUCTION_UK_01 ON courtauction (caNo);