import java.io.*;
import java.net.*;

public class Main{
    static class Client{
        private Socket soc = null;
        private DataInputStream serverIn = null;
        private DataOutputStream out = null;
        private BufferedReader consoleIn = null;
        private String username;

        public Client(String addr, int port, String username){
            this.username = username;
            try {
                soc = new Socket(addr, port);
                System.out.println("Connected to server.");
                serverIn = new DataInputStream(soc.getInputStream());
                out = new DataOutputStream(soc.getOutputStream());
                consoleIn = new BufferedReader(new InputStreamReader(System.in));
                out.writeUTF(username);
                startMessageListener();
                handleUserInput();
            } catch (UnknownHostException unknHst){
                System.out.println("Unknown Host: " + unknHst);
                return;
            } catch (IOException ioe) {
                System.out.println("Caught an Error: " + ioe);
                return;
            }
        }

        private void startMessageListener(){
            Thread listenerThread = new Thread(() -> {
                try{
                    while (true){
                        String message = serverIn.readUTF();
                        System.out.println(message);
                    }
                } catch (IOException e){
                    System.out.println("Disconnected from server.");
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
        }

        private void handleUserInput(){
            String message = "";
            System.out.println("Type your messages (type 'exit' to quit):");
            while (!message.equals("exit")){
                try {
                    message = consoleIn.readLine();
                    if (message != null){
                        out.writeUTF(message);
                    }
                } catch (IOException ioe){
                    System.out.println("Error sending message: " + ioe);
                    break;
                }
            }
            try{
                if (serverIn != null) serverIn.close();
                if (out != null) out.close();
                if (consoleIn != null) consoleIn.close();
                if (soc != null) soc.close();
            } catch (IOException ioe){
                System.out.println("Error closing connection: " + ioe);
            }
        }
    }

    public static void main(String[] args){
        System.out.println("Welcome to TCP CLI Chat Client.\nEnter your name:");
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String input = "Anonymous";
        try{
            input = read.readLine();
            if (input == null || input.trim().isEmpty()){
                input = "Anonymous";
            }
        } catch (IOException ioe){
            System.out.println("Error reading input: " + ioe);
        }
        System.out.println("Hey there, " + input);
        Client cli = new Client("127.0.0.1", 6969, input);
    }
}