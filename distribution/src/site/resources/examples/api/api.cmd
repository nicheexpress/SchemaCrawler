@echo off
del /f /q *.class
javac -classpath ../schemacrawler-6.3.jar ApiExample.java
java -classpath ../schemacrawler-6.3.jar;../hsqldb.jar;. ApiExample
