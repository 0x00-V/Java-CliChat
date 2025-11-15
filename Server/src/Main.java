import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Main {
    private static final int PORT = 6969;
    private static Map<SocketChannel, String> clients = new HashMap<>();

    public static void main(String[] args) throws IOException{
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port " + PORT);
        while (true){
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isAcceptable()) {
                    handleAccept(serverChannel, selector);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private static void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException{
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        clients.put(client, null);
        System.out.println("New client connected: " + client.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try{
            int bytesRead = client.read(buffer);
            if (bytesRead == -1){
                disconnectClient(client);
                return;
            }
            buffer.flip();
            String message = new String(buffer.array(), 0, buffer.limit()).trim();
            if (clients.get(client) == null){
                clients.put(client, message);
                broadcast(client, message + " has joined the chat!");
                return;
            }
            String fullMessage = clients.get(client) + ": " + message;
            broadcast(client, fullMessage);
        } catch (IOException e){
            disconnectClient(client);
        }

    }

    private static void broadcast(SocketChannel sender, String message) throws IOException{
        System.out.println(message);
        ByteBuffer msgBuffer = ByteBuffer.wrap((message + "\n").getBytes());
        for (SocketChannel client : clients.keySet()){
            if (client.isOpen() && client != sender){
                client.write(msgBuffer.duplicate());
            }
        }
    }

    private static void disconnectClient(SocketChannel client) throws IOException{
        String username = clients.get(client);
        if (username != null){
            System.out.println(username + " has disconnected.");
            broadcast(client, username + " has left the chat.");
        }
        clients.remove(client);
        client.close();
    }
}