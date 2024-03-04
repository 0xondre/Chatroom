package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run(){
        try{
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
        }catch (IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        done=true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch (IOException ignored){
        }
    }

    private String getFileToSend() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the file path to send: ");
        String filePath = scanner.nextLine();
        return filePath;
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String message = inReader.readLine();
                    if(message.equals("/quit")){
                        out.println(message);
                        inReader.close();
                        shutdown();
                    } else if (message.equals("/sendfile")) {
                    String filePath = getFileToSend();
                    sendFile(filePath);
                    }
                    else {
                        out.println(message);
                    }
                }
            }catch (IOException e){
                shutdown();
            }
        }

        private void sendFile(String filePath) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Error: File not found.");
                return;
            }

            out.println("FILE_TRANSFER_START");
            out.println(file.getName());
            out.println(file.length());

            FileInputStream fileInputStream = new FileInputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(Arrays.toString(buffer), 0, bytesRead);
            }

            fileInputStream.close();
            out.println("FILE_TRANSFER_END");
            System.out.println("File sent successfully!");
        }
    }


    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
