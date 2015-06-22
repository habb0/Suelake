@echo off

REM Windows
REM Starts up a SQLDatabase remote instance and binds it to RMI register

REM Start the SQLDatabase class main method. Database implementation gets the database access properties filename from blunk.properties
REM arg0 = blunk.properties: properties file for Blunk environment
REM arg1 = RMI url: RMI url to bind the SQLDatabase instance to 

java com.blunk.storage.sql.SQLDatabase blunk.properties rmi://localhost/blunk_sqldatabase

REM Do not close the console window
pause