package chat.blocking;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class BlockingClientHandler {
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
        String typingMessage = sc.nextLine();
        writer.println(typingMessage);

        String response = reader.readLine();

        System.out.println("서버 응답 : " + response);

        socket.close();
    }

}
