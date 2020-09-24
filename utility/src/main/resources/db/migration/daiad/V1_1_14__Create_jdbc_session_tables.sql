--
-- Session
--

-- https://github.com/spring-projects/spring-session/tree/master/spring-session-jdbc/src/main/resources/org/springframework/session/jdbc

CREATE SCHEMA IF NOT EXISTS "web"; 

CREATE TABLE web.spring_session_home (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT spring_session_home_pk PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX spring_session_home_ix1 ON web.spring_session_home (SESSION_ID);
CREATE INDEX spring_session_home_ix2 ON web.spring_session_home (EXPIRY_TIME);
CREATE INDEX spring_session_homeix3 ON web.spring_session_home (PRINCIPAL_NAME);

CREATE TABLE web.spring_session_home_attributes (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BYTEA NOT NULL,
	CONSTRAINT spring_session_home_attributes_pk PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT spring_session_home_attributes_fl FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES web.spring_session_home(PRIMARY_ID) ON DELETE CASCADE
);

CREATE TABLE web.spring_session_utility (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT spring_session_utility_pk PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX spring_session_utility_ix1 ON web.spring_session_utility (SESSION_ID);
CREATE INDEX spring_session_utility_ix2 ON web.spring_session_utility (EXPIRY_TIME);
CREATE INDEX spring_session_utility_ix3 ON web.spring_session_utility (PRINCIPAL_NAME);

CREATE TABLE web.spring_session_utility_attributes (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BYTEA NOT NULL,
	CONSTRAINT spring_session_utility_attributes_pk PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT spring_session_utility_attributes_fl FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES web.spring_session_utility(PRIMARY_ID) ON DELETE CASCADE
);