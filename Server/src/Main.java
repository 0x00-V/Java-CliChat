import java.io.*;
import java.net.*;



public class Main {
    static class Server{
        private Socket soc = null;
        private ServerSocket srvSoc = null;
        private DataInputStream in = null;

        public Server(int port){
            try{
                srvSoc = new ServerSocket(port);
                System.out.println("Server started on port: " + port);
                soc = srvSoc.accept();

                in = new DataInputStream(new BufferedInputStream(soc.getInputStream()));
                String username = in.readUTF();
                System.out.println(username + " has connected.");
                String message = "";

                while(!message.equals("exit")){
                    try{
                        message = in.readUTF();
                        System.out.println(message);
                    } catch (IOException ioe){
                        System.out.println("Caught an Error: " + ioe);
                    }
                }
                System.out.println("A client has disconnected");
                soc.close();
                in.close();
            } catch (IOException ioe){
                System.out.println("Caught an Error: " + ioe);
            }
        }
    }

    public static void main(String[] args){
        Server srv = new Server(6969);
    }
}

