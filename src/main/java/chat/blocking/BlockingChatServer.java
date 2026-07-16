package chat.blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BlockingChatServer {
    private static final int PORT = 9000;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT); // 서버에서 클라이언트 요청 접수 담당

        System.out.println("서버 시작 클라이언트 대기중");

        while (true){
            Socket clientSocket = serverSocket.accept();    // 서버는 먼저 다음 코드로 9000번 포트를 열고 연결 요청을 대기한다.
                                                            // accept()는 대표적인 블로킹 I/O다.
                                                            /*클라이언트                      서버
                                                                |                            |
                                                                | -------- SYN ------------> |
                                                                | <----- SYN + ACK --------- |
                                                                | -------- ACK ------------> |
                                                                |                            |
                                                                |       TCP 연결 완료         |*/
            System.out.println("클라이언트 접속 : "
                    + clientSocket.getRemoteSocketAddress());   // 클라이언트 접속 : /[0:0:0:0:0:0:0:1]:53507
                                                                // TCP 연결에는 양쪽의 IP 주소와 포트번호가 필요함
                                                                // Windows가 사용 가능한 포트 하나를 자동으로 골라서 붙임


            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            clientSocket.getInputStream(),StandardCharsets.UTF_8
                    ));

            PrintWriter writer = new PrintWriter(
                    clientSocket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8);

            String message = reader.readLine();

            System.out.println("수신 메시지 : " + message);
            writer.println(message);

            clientSocket.close();

        }


    }
}
