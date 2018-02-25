# FTP-Server-and-Client
FTP server implemented to serve clients for basic linux commands as part of Distributed Computing Systems course
---------------
**Description:**

 FTP server logic is included in myftpserver.java and myftp.java includes the client's functionality to get connected to server and perform the following linux commands as supported by the server:
  1. `get <remote_filename>`: Copy file with the name <remote_filename> from remote directory to local directory.
  2. `put <local_filename>` : Copy file with the name <local_filename> from local directory to remote directory.
	3. `delete <remote_filename>` : Delete the file with the name <remote_filename> from the remote directory.
	4. `ls` : List the files and subdirectories in the remote directory.
	5. `cd (cd <remote_direcotry_name/subDirectory> or cd ..)` : Change to the <remote_direcotry_name > on the remote machine or change to the parent 	   	   directory of the current directory
	6. `mkdir <remote_directory_name>` : Create directory named <remote_direcotry_name> as the sub-directory of the current working directory on 	           	   the remote machine.
	7. `pwd`  : Print the current working directory on the remote machine.
	8. `quit` : End the FTP session.
  
**Compilation and Execution instructions:**

- Server and client should be started from different terminals
- To start the server, compile server file with `javac myftpserver.java` and to run `java myftpserver <port_no>`
- To start the client, compile with `javac myftp.java` and to run `java myftp <localhost/server_ipaddress> <port_no>`

**Team Members:**

- Chandana Marneni
- Shubha Mishra
