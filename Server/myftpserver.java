/************************ FTP Server Program ********************************************
  @authors - Chandana Marneni, Shubha Mishra
 ****************************************************************************************/
// Package requirements
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

/* Creates socket and listens for client at user given port number...
   Establishes connection with client and wait for commands...
   Reads the commands requested by client and executes the requests accordingly...
   Assumption - Client always enters correct commands and arguments...
*/
public class myftpserver  {

    public static void main(String[] args) {
        ServerSocket serverSocket=null;
        DataInputStream dis=null;
        DataOutputStream dos=null;
        Socket clientSocket=null;
        int port=Integer.parseInt(args[0]);

        try {
            serverSocket=new ServerSocket(port);
            serverSocket.setSoTimeout(10000000);  // Socket time-out

            while (true){
                System.out.println("----Waiting for new client to connect----"); // Listening for client...
                clientSocket=serverSocket.accept();
                System.out.println("Connection established with client "+clientSocket);
                dis=new DataInputStream(clientSocket.getInputStream());
                dos=new DataOutputStream(clientSocket.getOutputStream());
                System.out.println("Forking the client connection");
                Thread t=new myftpserver2(clientSocket,dis,dos);
                t.start();
            }
        } catch (IOException e) {
            try {
                dos.writeUTF("Error opening the socket!");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Multithreaded server class that handles multiple clients
 */
class myftpserver2 extends Thread{
     DataInputStream dis;
     DataOutputStream dos;
     Socket client;

     myftpserver2(Socket client,DataInputStream dis, DataOutputStream dos){
         this.client=client;
         this.dis=dis;
         this.dos=dos;
     }

     @Override
     public void run(){
            String send;
            String receive;
         String original_path=Paths.get("").toAbsolutePath().toString();
            while (true){
                try{
                    dos.writeUTF("myftp>");
                    dos.flush();
                    receive=dis.readUTF();  // Read client commands
                    if(receive.equals("quit")){
                        System.out.println("Client request to quit session");
                        System.out.println("Disconnecting client..."+this.client);
                        if(this.client!=null) {
                            System.setProperty("user.dir",original_path);
                            this.client.close();
                            System.out.println(this.client + " Client disconnected!");

                        }
                        break;
                    }
                    String commandStr[]=receive.split(" ");
                    switch (commandStr[0]){
                        case "get":
                            get(commandStr);
                            break;
                        case "put":
                            put(commandStr);
                            break;
                        case "delete":
                            delete(commandStr);
                            break;
                        case "ls":
                            list(commandStr);
                            break;
                        case "cd":
                            cd(commandStr);
                            break;
                        case "mkdir":
                            mkdir(commandStr);
                            break;
                        case "pwd":
                            pwd();
                            break;
                            default:
                                System.out.println("Invalid input command!");
                                dos.writeUTF("Invalid input command!");
                                break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try{
                if(dis!=null){
                    dis.close();
                }
                if(dos!=null){
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
     }


    /**
     * get method Send requested file, if found, to client...
     * @param command : client's request command to be executed at server side
     */
     void get(String[] command){
         try {
             String fileName=command[1];
             File file_to_send = new File(System.getProperty("user.dir") + "/" + fileName);
             if (file_to_send.exists()) {
                dos.writeUTF("File transfer in progress...");
                byte[] buffer = new byte[(int) file_to_send.length()];
                 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file_to_send));
                 bis.read(buffer, 0, buffer.length);
                 dos.writeInt(buffer.length);
                 dos.write(buffer, 0, buffer.length);
                 dos.flush();
                 System.out.println("File sent to the client!");
             } else dos.writeUTF("File does not exist on server!");
         }
         catch (IOException e)
         {
             try {
                 dos.writeUTF("Error in copying file");
             } catch (IOException e1) {
                 e1.printStackTrace();
             }
             e.printStackTrace();
         }
     }


    /**
     * put method Creates a local copy of the file sent from client...
     * @param command : clinet's request command
     */
    void put(String[] command)
    {
        String fileName=command[1];
        int buffersize;
        try {
            File filepath = new File(System.getProperty("user.dir") + "/" + fileName);
            filepath.createNewFile();
            buffersize = dis.readInt();
            byte[] buffer = new byte[buffersize];
            FileOutputStream fos = new FileOutputStream(filepath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            dis.read(buffer, 0, buffersize);
            bos.write(buffer, 0, buffersize);
            bos.flush();
            bos.close();
            fos.close();
            System.out.println("File recieved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException np) {
            np.getCause();
            np.getLocalizedMessage();
        }

    }

     /**
      * delete (delete <remote_filename>) – Delete the file with the name <remote_filename>
         from the remote directory.
      *@param command : client's request command
     */
    public void delete(String[] command){
         String fileName=command[1];
         boolean deleted=true;
         File file=new File(fileName);

         try{
             if(file.exists()){
                 deleted=file.delete();
                 if(deleted){
                     dos.writeUTF("File deletion successful!");
                 }else{
                     dos.writeUTF("File deletion failed!");
                 }
             }
             else{
                 dos.writeUTF("File does not exist!");
             }
         } catch (IOException e) {
             try {
                 dos.writeUTF("Error in deleting file..");
             }catch (IOException e1){
                 e1.printStackTrace();
             }
             e.printStackTrace();
         }

    }


    /**
     * ls -- List the files and subdirectories in the remote directory..
     * @param command : client's requested command
     */
    public void list(String[] command){
        String cur_path=Paths.get("").toAbsolutePath().toString();
        File file=new File(System.getProperty("user.dir"));
        String[] result=file.list();
        String output=null;
            try {
                if(result.length!=0) {
                    output = result[0];
                    for (int i = 1; i < result.length; i++) {
                        output = output + "\t" + result[i];
                    }
                    dos.writeUTF(output);
                    dos.flush();

                }else{
                    //dos.writeUTF("No contents to show in this directory!");
                }
            } catch (IOException e) {
                try {
                    dos.writeUTF("Error in listing ocntents of the diretory..");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }

    }

    /* cd dirname - enter the remote directory 'dirname' which is given by client,
       making it the current working directory..
       cd .. - Move to parent directory making it the current working directory..
    */
    public void cd(String[] command){
        String dir=command[1];

        try {
            if (dir.equals("..")) {
                String path=System.getProperty("user.dir");
                String new_path=new File(path).getParent();
                System.setProperty("user.dir",new_path);

            }else{
                String path=System.getProperty("user.dir");
                String new_path=path+"/"+dir;
                System.setProperty("user.dir",new_path);
            }

            dos.writeUTF("Directory changed successfully!");
        } catch (IOException e) {
            try {
                dos.writeUTF("Error in changing directory..");
            }catch (IOException e1){
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    //pwd (pwd) – Print the current working directory on the remote machine.
    //cd in windows
    private void pwd() throws IOException {
        String path=System.getProperty("user.dir");
        try {
            dos.writeUTF(path);
        } catch (IOException e) {
            dos.writeUTF("Error in printing current working directory");
            e.printStackTrace();
        }
    }

    /* mkdir (mkdir <remote_directory_name>) – Create directory named
      <remote_direcotry_name> as the sub-directory of the current working directory on the
       remote machine..
    */
    private void mkdir(String[] commandStr) {
        String newdir=commandStr[1];
        String curDir=System.getProperty("user.dir");
        String newpath=curDir+"/"+newdir;
        try {
            File file = new File(curDir + "/" + newdir);
            boolean dirCreated = file.mkdir();

            if (dirCreated) {
                dos.writeUTF("Directory created successfully!");
            } else {
                dos.writeUTF("Directory creation failed!");
            }
        } catch (IOException e) {
            try {
                dos.writeUTF("Error in creating directory");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
/********************************************* END OF SERVER PROGRAM *************************************************/
