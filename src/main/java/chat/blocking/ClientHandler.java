package chat.blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientHandler implements Runnable{

    private final Socket socket; // 클라이언트 소켓 보관
    private final List<ClientHandler> clients;
    private PrintWriter writer;

    private String nickname;

    public ClientHandler(Socket socket, List<ClientHandler> list){ // 서버가 전달한 socket을 객체 필드에 저장
        this.clients = list;
        this.socket =socket;
    }

    public synchronized void send(String message) {
        writer.println(message);
    }

    private void broadcast(String message){
        for(ClientHandler client : clients){
            client.send(message);
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(), StandardCharsets.UTF_8));
            /*
            * socket.getInputStream() -> 클라이언트가 보낸 바이트를 읽는 통로
            * InputStreamReader -> 바이트를 UTF-8 문자로 변환
            * BufferedReader -> 문자를 한 줄씩 읽을 수 있게 함
            * */


            writer = new PrintWriter(socket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8);
            /*
            * socket.getOutputStream() -> 클라이언트에게 바이트를 보내는 통로
            * PrintWriter -> 문자열을 전송
            * */

            //클라이언트가 처음보낸 한줄은 닉네임
            nickname = reader.readLine();

            if(nickname == null) return;

            clients.add(this);
            broadcast(nickname + " 입장");

            while (true){
                String message = reader.readLine();

                if (message == null) {
                    System.out.println(
                            "클라이언트 연결 종료 : "
                                    + socket.getRemoteSocketAddress()
                    );
                    break;
                }

                if (message.equals("/quit")) {
                    break;
                }


                System.out.println("메시지 내용 : " + message);
                broadcast(nickname + " : " + message);


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(clients.remove(this)){
                broadcast(nickname + " 퇴장");
            };
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
