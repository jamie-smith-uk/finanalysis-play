CREATE TABLE STATEMENT(
	Key		SERIAL	PRIMARY KEY,
	Date		DATE 	NOT NULL,
	Type		TEXT    NOT NULL,
	Descrption	INT     NOT NULL,
	Value		REAL	NOT NULL,
	Category	TEXT
);