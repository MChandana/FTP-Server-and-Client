/************************************ FTP CLIENT PROGRAM ********************************************
  @authors - Chandana Marneni, Shubha Mishra
 ****************************************************************************************************/
// Package requirements
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/* Requests connection to server at the server listening port...
   Requests services from server using commands...
   Close the connection once all requests are completed by requesting Quit...
 */
public class myftp {

    static Socket client=null;
    static DataInputStream dis=null;
    static DataOutputStream dos=null;

    public static void main(String[] args) {
        int port=Integer.parseInt(args[1]);
        String host_adrs=args[0];

        try {
            client=new Socket(host_adrs,port);
            dis=new DataInputStream(client.getInputStream());
            dos=new DataOutputStream(client.getOutputStream());
            if(client==null || dis==null || dos==null){
                System.err.println("Error opening socket and streams");
                return;
            }

            while (true){
                String result=dis.readUTF();
                System.out.print(result);
                BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
                String command=reader.readLine();
                dos.writeUTF(command);
                dos.flush();
                if(command.equals("quit")){
                    System.out.println("Ending FTP session for this client..."+client);
                    client.close();
                    System.out.println("FTP session ended!");
                    break;
                }

                // Commands ........
                if(command.startsWith("get")){
                    get(command);
                }
                else if(command.startsWith("put")){
                    put(command);
                }
                else{
                    System.out.println(dis.readUTF());
                }

            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if(dis!=null){
                    dis.close();
                }
                if(dos!=null){
                    dos.close();
                }
                if(client!=null){
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /** get <filename> - Retrieve file with given filename from remote machine, if file exists..
    *
    *@param command : command entered by client
    *
    */
    public static void get(String command) {
        String argname = command.substring((command.indexOf(" "))+1, command.length());
        int buffersize;
        try {
            String readupdate =dis.readUTF();
            System.out.println(readupdate);
           if(readupdate.equals("File transfer in progress...")) {
                File getfile = new File(System.getProperty("user.dir") + "/" + argname);
                getfile.createNewFile();
                System.out.println("File transfer in progress");
               buffersize = dis.readInt();
                byte[] buffer = new byte[buffersize];
                FileOutputStream fos = new FileOutputStream(getfile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                dis.read(buffer, 0, buffersize);
                bos.write(buffer, 0, buffersize);
                bos.flush();
                bos.close();
                fos.close();
                System.out.println("File transfer complete!");
            }
            else {
                System.out.println(readupdate);
            }
        }
        catch (IOException e) {
            System.out.println("IOException");
        } catch (NullPointerException np) {
            np.getCause();
            np.getLocalizedMessage();
            System.out.println("Exception raised");
        }
        return;
    }


    /**
    * put <filename> - Send file with given filename from local to remote machine..
     * @param command : command entered by client
     */
    public static void put(String command)
    {
        String argname = command.substring((command.indexOf(" "))+1, command.length());
        try {
            File file_to_send = new File(System.getProperty("user.dir") + "/" + argname);
            if (file_to_send.exists()) {
                System.out.println("File found! Sending file...");
                byte[] buffer = new byte[(int) file_to_send.length()];
                FileInputStream fis = new FileInputStream(file_to_send);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(buffer, 0, buffer.length);
                dos.writeInt(buffer.length);
                dos.write(buffer, 0, buffer.length);
                dos.flush();
                System.out.println("File sent!");
            } else System.out.println("File does not exist!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return;
    }
}

/***************************************** END OF FTP CLIENT PROGRAM *****************************************************/