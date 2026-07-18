package chat.blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockingChatServer {
    private static final int PORT = 9000;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT); // 서버에서 클라이언트 요청 접수 담당
        List<ClientHandler> clients = new CopyOnWriteArrayList<>(); // CopyOnWriteArrayList: 여러 스레드가 동시에 접근해도 안전한(Thread-Safe) 자바 배열 리스트

        System.out.println("서버 시작 클라이언트 대기중");

        while (true){
            Socket clientSocket = serverSocket.accept();    // 서버는 먼저 다음 코드로 9000번 포트를 열고 연결 요청을 대기한다.
                                                            // accept()는 대표적인 블로킹 I/O다. 클라이언트가 접속할 때 까지 블록킹
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

            ClientHandler clientHandler = new ClientHandler(clientSocket, clients); // 클라이언트 소켓을 ClientHandler에 전달함.

            Thread thread = new Thread(clientHandler);
            //clientHandler는 Runnable을 구현하고 있기 때문에 Thread에 전달할 수 있다.
            thread.start();
            //start()를 호출하면 새로운 스레드가 만들어지고 그 스레드가 ClientHandler.run()을 실행
            /*
            thread.start()
            → 새로운 스레드 생성
            → 새로운 스레드가 run() 실행

            thread.run()
            → 새로운 스레드가 만들어지지 않음
            → 현재 메인 스레드가 그냥 run() 실행
            */

        }


    }
}

/*
* ServerSocket
→ 새로운 손님을 받는 접수 창구

clientSocket
→ 접속한 손님 한 명과 통신하는 전용 연결
* */