@echo off
javac ClientOperations\CohortInterface.java
javac ClientOperations\CoordinatorInterface.java
javac ClientOperations\CohortImpl.java
javac Logger\ServerLogger.java
javac rmi_server_cohort\RMI_Server_Cohort.java
java rmi_server_cohort.RMI_Server_Cohort %1 %2