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

        System.out.print("닉네임 입력: ");
        String nickname = sc.nextLine();

        writer.println(nickname);


        /*
         * 서버 메시지를 계속 받는 수신 전용 스레드
         */
        Thread receiverThread = new Thread(() -> {
            try {
                while (true) {
                    String receivedMessage = reader.readLine();

                    if (receivedMessage == null) {
                        System.out.println();
                        System.out.println("서버와의 연결이 종료되었습니다.");
                        break;
                    }

                    System.out.println();
                    System.out.println("채팅 메시지 : " + receivedMessage);
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.out.println();
                    System.out.println("서버와의 통신이 끊어졌습니다.");
                }
            }
        });

        receiverThread.start();


        while (true) {
            System.out.println("메시지 입력: ");
            String typingMessage = sc.nextLine();

            writer.println(typingMessage);

            if (typingMessage.equals("/quit")) {
                break;
            }

        }

        socket.close();
        sc.close();
    }
}
