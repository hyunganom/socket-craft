package chat.blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class BlockingChatClient  {
    private static final int PORT = 9000;
    private static final String HOST = "localhost";

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(HOST, PORT);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));


        PrintWriter writer = new PrintWriter(socket.getOutputStream(),
                true,
                StandardCharsets.UTF_8);

        Scanner sc = new Scanner(System.in);

        Thread receiverThread = new Thread(()->{
            try {
                while (true){
                    String receivedMessage = reader.readLine();

                    if(receivedMessage == null){
                        break;
                    }

                    System.out.println("채팅 메시지 : " + receivedMessage);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        receiverThread.start();

        while (true) {
            System.out.print("메시지 입력: ");
            String typingMessage = sc.nextLine();

            if (typingMessage.equals("/quit")) {
                break;
            }

            writer.println(typingMessage);
        }

        socket.close();
        sc.close();
    }
}
