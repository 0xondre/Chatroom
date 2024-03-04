package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private final ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run(){
        try {
            server = new ServerSocket(9999);
            ExecutorService pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(client);
                connections.add(connectionHandler);
                pool.execute(connectionHandler);
            }
        } catch (Exception e) {
            shutdown();
        }

    }

    public void broadcast(String message){
        for(ConnectionHandler connection: connections){
            if(connection != null){
                connection.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
                for (ConnectionHandler connection : connections) {
                    connection.shutdown();
                }
            }
        }catch (IOException ignored){
        }
    }

    class ConnectionHandler implements Runnable{

        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;

        public ConnectionHandler(Socket client){
            this.client = client;

        }

        @Override
        public void run(){
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Please enter a nickname: ");
                String nickname = in.readLine();
                System.out.println(nickname + " connected");
                broadcast(nickname + " joined the chat");

                String message;
                while ((message=in.readLine()) != null){
                    if(message.startsWith("/nick")){
                        String[] messageArray = message.split(" ", 2);
                        if(messageArray.length==2){
                            System.out.println(nickname + " renamed to " + messageArray[1]);
                            broadcast(nickname + " renamed to " + messageArray[1]);
                            nickname = messageArray[1];
                            out.println("Successfully renamed to " + nickname);
                        } else {
                            out.println("No nickname provided");
                        }

                    }else if(message.startsWith("/quit")){
                        broadcast(nickname + " left the chat");
                        shutdown();
                    }else if (message.startsWith("FILE_TRANSFER_START")) {
                        String filename = in.readLine();
                        long fileSize = Long.parseLong(in.readLine());
                        receiveFile(filename, fileSize);
                    }else{
                        broadcast(nickname + ": " + message);
                    }
                }
            }catch (IOException e){
                shutdown();
            }
        }


        private void receiveFile(String filename, long fileSize) throws IOException {
            File file = new File("received_files/" + filename);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // Open file output stream
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            // Receive file data in chunks
            byte[] buffer = new byte[1024];
            int bytesReceived = 0;
            while (bytesReceived < fileSize) {
                int bytesRead = client.getInputStream().read(buffer);
                if (bytesRead == -1) {
                    throw new IOException("Unexpected end of file stream");
                }
                fileOutputStream.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead;
            }

            // Close streams and confirm transfer
            fileOutputStream.close();
            out.println("FILE_TRANSFER_SUCCESS");
            System.out.println("File received: " + filename);
        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            }catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
