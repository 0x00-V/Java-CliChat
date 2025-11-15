import java.io.*;
import java.net.*;
import java.util.*;


class CliHandler implements Runnable{
    private Socket cliSoc;
    private List<CliHandler> clients;
    private PrintWriter out;
    private BufferedReader in;

    public CliHandler(Socket soc, List<CliHandler> clients) throws IOException{
        this.cliSoc = soc;
        this.clients = clients;
        this.out = new PrintWriter(cliSoc.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(cliSoc.getInputStream()));
    }

    public void run(){
        String inLine;
        try {
            while ((inLine = in.readLine()) != null){
                synchronized(clients){
                    for (CliHandler c : clients) {
                        if (c != this){
                            c.out.println(cliSoc.getRemoteSocketAddress() + " said: " + inLine);
                        }
                    }
                }
                System.out.println(cliSoc.getRemoteSocketAddress() + " said: " + inLine);
            }
        } catch (IOException e){
            System.out.println("Client disconnected: " + cliSoc);
        } finally{
            try{
                in.close();
                out.close();
                cliSoc.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            clients.remove(this);
        }
    }
}

public class Server{
    private static List<CliHandler> clients = new ArrayList<>();


    public static void main(String[] args) throws IOException{

        ServerSocket srvSoc = new ServerSocket(6969);


        while(true) {
            Socket cliSoc = srvSoc.accept();
            System.out.println("Client connected: "+cliSoc.getInetAddress()+" "+cliSoc.getPort());
            CliHandler clientThread = new CliHandler(cliSoc, clients);
            clients.add(clientThread);
            new Thread(clientThread).start();
        }
    }
}