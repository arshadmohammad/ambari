**********************************
Run Ambari Server
**********************************

1) Create database, populate tables,correct database properties
2) Correct the java.home
3) Change change the following based on OS in 
    ambari-server.user
    server.os_typ
    server.os_family
4) run ambari-server.cmd/ambari-server.sh setup -j C:\jdk1.7.0_67
     or if modified all the properties manually run
   run ambari-server.cmd/ambari-server.sh start


++++++++++++++++++
Populate Database
++++++++++++++++++
Run following commands in mysql console
mysql -u root -p
drop database ambari;
create database amabri;
use ambari;
source C:\Users\arshad\Desktop\Ambari-DDL-MySQL-CREATE.sql






**********************************
Run Ambari Agent
*********************************
To manually start the ambari server do following
1) Modify the ambari server host
2) run ambari-agent.cmd/ambari-agent.sh setup
3)run ambari-agent.cmd/ambari-agent.sh start 
Note: when running in windows make sure to run from admin user, as it requires admin privileges to install and run services


