import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Main{
    private static final int PORT = 6969;
    private static Map<SocketChannel, String> clients = new HashMap<>();
    private static Map<SocketChannel, ByteBuffer> clientBuffers = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port " + PORT);
        while (true){
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isAcceptable()){
                    handleAccept(serverChannel, selector);
                } else if (key.isReadable()){
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
        clientBuffers.put(client, ByteBuffer.allocate(8192));
        System.out.println("New client connected: " + client.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException{
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = clientBuffers.get(client);
        try{
            int bytesRead = client.read(buffer);
            if (bytesRead == -1) {
                disconnectClient(client);
                return;
            }
            buffer.flip();
            while (buffer.remaining() >= 2){
                buffer.mark();
                int utfLen = buffer.getShort() & 0xFFFF;
                if (buffer.remaining() < utfLen){
                    buffer.reset();
                    break;
                }
                byte[] strBytes = new byte[utfLen];
                buffer.get(strBytes);
                String message = new String(strBytes, "UTF-8");
                if (clients.get(client) == null){
                    clients.put(client, message);
                    System.out.println(message + " has joined the chat!");
                    broadcast(client, message + " has joined the chat!");
                } else {
                    if (message.equals("exit")){
                        disconnectClient(client);
                        return;
                    }
                    String fullMessage = clients.get(client) + ": " + message;
                    System.out.println(fullMessage);
                    broadcast(client, fullMessage);
                }
            }
            buffer.compact();
        } catch (IOException e){
            disconnectClient(client);
        }
    }

    private static void broadcast(SocketChannel sender, String message) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(message);
        dos.flush();
        ByteBuffer msgBuffer = ByteBuffer.wrap(baos.toByteArray());
        for (SocketChannel client : clients.keySet()){
            if (client.isOpen() && client != sender){
                try{
                    msgBuffer.rewind();
                    client.write(msgBuffer);
                } catch (IOException e) {
                    System.out.println("Error sending to client: " + e.getMessage());
                }
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
        clientBuffers.remove(client);
        client.close();
    }
}