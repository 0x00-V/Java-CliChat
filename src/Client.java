import java.io.*;
import java.net.*;

public class Client {
    private Socket soc;
    private BufferedReader consoleIn;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String addr, int port){
        try {
            soc = new Socket(addr, port);
            System.out.println("Chat connected: " + soc);
            consoleIn = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(soc.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            new Thread(() -> {
                try{
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null){
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            String userInput;
            while ((userInput = consoleIn.readLine()) != null){
                out.println(userInput);
                if (userInput.equalsIgnoreCase("exit")){
                    break;
                }
            }
            soc.close();
            consoleIn.close();
            out.close();
        } catch (Exception e){
            System.out.println("Unexpected exception: " + e.getMessage());
        }
    }
    public static void main(String[] args){
        new Client("127.0.0.1", 6969);
    }
}
