import java.io.*;
import java.net.*;

public class Main{
    static class Client{
        private Socket soc = null;
        private DataInputStream in = null;
        private DataOutputStream out = null;
        private String username;

        public Client(String addr, int port, String username){
            this.username = username;
            try{
                soc = new Socket(addr, port);
                System.out.println("Connected.");
                in = new DataInputStream(System.in);
                out = new DataOutputStream(soc.getOutputStream());
                out.writeUTF(username);
            } catch (UnknownHostException unknHst){
                System.out.println("Unknown Host: " + unknHst);
                return;
            } catch(IOException ioe){
                System.out.println("Caught an Error: " + ioe);
                return;
            }
            String message = "";
            while(!message.equals("exit")){
                try{
                    message = in.readLine();
                    out.writeUTF(username + ": " + message);
                } catch (IOException ioe){
                    System.out.println("Caught an Error: " + ioe);
                }
            }
            try{
                in.close();
                out.close();
                soc.close();
            } catch (IOException ioe){
                System.out.println("Caught an Error: " + ioe);
            }
        }

    }
    public static void main(String[] args){
        System.out.println("Welcome to TCP CLI Server.\nEnter a name:");
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String input = "Anonymous";
        try{
            input = read.readLine();
        } catch (IOException ioe){
            System.out.println("Caught an Error: " + ioe);
        }

        System.out.println("Hey there, " + input);
        Client cli = new Client("127.0.0.1", 6969, input);
    }
}