CREATE TABLE EVENTS (
ID BIGINT NOT NULL, 
MESSAGE_CONTENT LONGTEXT, 
EVENT_EXTENSION LONGTEXT, 
EI_TIMESTAMP DATETIME, 
EI_EVENT_TYPE VARCHAR(255), 
ORIG_CUSTOM_ID VARCHAR(255), 
ORIG_PROCESS_ID VARCHAR(255), 
ORIG_HOSTENAME VARCHAR(128), 
ORIG_IP VARCHAR(64), 
MI_PORT_TYPE VARCHAR(255), 
MI_OPERATION_NAME VARCHAR(255), 
MI_MESSAGE_ID VARCHAR(255), 
MI_FLOW_ID VARCHAR(64), 
MI_TRANSPORT_TYPE VARCHAR(255), 
PRIMARY KEY (ID));

CREATE TABLE SEQUENCE (SEQ_NAME VARCHAR(50) NOT NULL, SEQ_COUNT DECIMAL(38), PRIMARY KEY (SEQ_NAME));
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('EVENT_SEQ', 0);

CREATE TABLE EVENTS_CUSTOMINFO (
ID BIGINT NOT NULL, 
EVENT_ID BIGINT NOT NULL, 
CUST_KEY VARCHAR(255), 
CUST_VALUE VARCHAR(255), 
PRIMARY KEY (ID,EVENT_ID));
