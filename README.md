# Java Blocking Socket 서버 기초

## 1. 학습 목표

`BlockingChatServer.java`를 작성하여 다음 흐름을 이해한다.

1. 서버 실행
2. 특정 포트에서 클라이언트 연결 대기
3. 클라이언트 한 명의 접속 수락
4. 연결된 클라이언트와 통신할 `Socket` 객체 생성

---

## 2. 기본 서버 코드

```java
package chat.blocking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingChatServer {

    private static final int PORT = 9000;

    public static void main(String[] args) throws IOException {

        // 9000번 포트를 사용하는 서버 소켓 생성
        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("서버가 시작되었습니다.");
        System.out.println("클라이언트의 연결을 기다립니다.");

        // 클라이언트가 접속할 때까지 현재 스레드 대기
        Socket clientSocket = serverSocket.accept();

        System.out.println(
                "클라이언트 접속: "
                        + clientSocket.getRemoteSocketAddress()
        );

        // 연결 종료
        clientSocket.close();
        serverSocket.close();
    }
}
```

이 코드는 클라이언트 한 명의 연결을 수락한 뒤 종료되는 가장 단순한 TCP 서버이다.

---

# 3. `IOException`

## 개념

`IOException`은 파일, 네트워크, 스트림 등 입출력 작업 중 발생할 수 있는 예외이다.

소켓 통신에서는 다음과 같은 상황에서 발생할 수 있다.

* 포트를 열지 못한 경우
* 이미 다른 프로그램이 해당 포트를 사용하고 있는 경우
* 네트워크 연결이 끊어진 경우
* 데이터를 읽거나 쓰는 과정에서 문제가 발생한 경우
* 소켓이 이미 닫힌 경우

```java
public static void main(String[] args) throws IOException
```

여기서 `throws IOException`은 다음 의미이다.

> 이 메서드 내부에서 `IOException`이 발생할 수 있으며, 현재 메서드에서 직접 처리하지 않고 호출한 쪽에 처리를 위임한다.

`main()` 메서드에서 예외를 위임하면 최종적으로 JVM이 예외를 처리하고 오류 내용을 출력한다.

## 직접 처리하는 방법

```java
public static void main(String[] args) {
    try {
        ServerSocket serverSocket = new ServerSocket(9000);
    } catch (IOException e) {
        System.out.println("서버를 실행할 수 없습니다.");
        e.printStackTrace();
    }
}
```

학습 초기에는 `throws IOException`으로 작성한 뒤, 예외 처리 구조를 배울 때 `try-catch`로 변경해도 된다.

---

# 4. `ServerSocket`과 `Socket`

Oracle Java SE 공식 문서의 개념을 기준으로 구분하면 다음과 같다.

## `ServerSocket`

`ServerSocket`은 클라이언트의 연결 요청을 기다리고 받아들이는 TCP 서버용 리스닝 소켓이다.

```java
ServerSocket serverSocket = new ServerSocket(9000);
```

이 코드가 실행되면 운영체제에 다음 내용을 요청한다.

> 현재 프로그램이 TCP 9000번 포트로 들어오는 연결 요청을 받겠다.

즉, `ServerSocket`은 클라이언트와 직접 데이터를 주고받는 객체가 아니라 **연결 요청 접수 담당 객체**이다.

## `Socket`

`Socket`은 연결된 상대방과 실제 통신을 담당하는 TCP 통신 엔드포인트이다.

```java
Socket clientSocket = serverSocket.accept();
```

`accept()`가 클라이언트의 연결을 수락하면 해당 클라이언트와 연결된 새로운 `Socket` 객체를 반환한다.

이 `Socket`을 통해 다음 작업을 수행할 수 있다.

```java
clientSocket.getInputStream();  // 클라이언트가 보낸 데이터 읽기
clientSocket.getOutputStream(); // 클라이언트에게 데이터 보내기
```

## 역할 비교

| 객체             | 역할                    |
| -------------- | --------------------- |
| `ServerSocket` | 클라이언트 연결 요청을 기다리고 수락  |
| `Socket`       | 연결된 클라이언트와 실제 데이터 송수신 |

흐름으로 표현하면 다음과 같다.

```text
클라이언트
    │
    │ TCP 연결 요청
    ▼
ServerSocket
    │
    │ accept()
    ▼
Socket
    │
    ├── InputStream
    └── OutputStream
```

---

# 5. 포트를 연다는 의미

```java
ServerSocket serverSocket = new ServerSocket(PORT);
```

이 코드는 단순히 Java 객체만 생성하는 것이 아니다.

운영체제에 다음 작업을 요청한다.

1. TCP 소켓 생성
2. 9000번 포트와 소켓 연결
3. 해당 포트를 리스닝 상태로 변경
4. 들어오는 TCP 연결 요청 관리

서버가 정상적으로 실행되면 운영체제는 9000번 포트로 들어오는 연결 요청을 받을 준비를 한다.

```text
서버 프로그램
    │
    │ new ServerSocket(9000)
    ▼
운영체제
    │
    ├── 9000번 포트 사용
    ├── LISTEN 상태로 변경
    └── TCP 연결 요청 관리
```

이미 다른 프로그램이 9000번 포트를 사용하고 있다면 다음과 같은 예외가 발생할 수 있다.

```text
java.net.BindException: Address already in use
```

---

# 6. `accept()`의 역할

```java
Socket clientSocket = serverSocket.accept();
```

`accept()`는 클라이언트가 서버에 접속할 때까지 기다린다.

클라이언트가 접속하지 않았다면 다음 코드로 넘어가지 않는다.

```java
System.out.println("클라이언트 대기 중");

Socket clientSocket = serverSocket.accept();

// 클라이언트가 접속해야 실행됨
System.out.println("클라이언트 접속 완료");
```

실행 흐름은 다음과 같다.

```text
서버 실행
    ↓
9000번 포트 리스닝
    ↓
accept() 호출
    ↓
클라이언트가 접속할 때까지 대기
    ↓
클라이언트 접속
    ↓
연결된 Socket 반환
    ↓
다음 코드 실행
```

---

# 7. `accept()`도 I/O인 이유

I/O는 단순히 데이터를 읽거나 쓰는 작업만 의미하지 않는다.

외부 장치나 운영체제의 상태가 충족되기를 기다리는 작업도 I/O에 포함된다.

네트워크 서버의 대표적인 I/O 작업은 다음과 같다.

```java
serverSocket.accept();
```

클라이언트의 연결을 기다린다.

```java
clientSocket.getInputStream().read();
```

클라이언트가 데이터를 보내기를 기다린다.

```java
clientSocket.getOutputStream().write(data);
```

운영체제의 송신 버퍼에 데이터를 기록한다. 송신 버퍼가 가득 차 있으면 공간이 생길 때까지 기다릴 수 있다.

세 작업 모두 조건이 충족되지 않으면 현재 스레드가 대기할 수 있다.

따라서 다음 세 작업은 모두 네트워크 I/O이다.

| 작업         | 기다리는 조건  |
| ---------- | -------- |
| `accept()` | 클라이언트 연결 |
| `read()`   | 수신할 데이터  |
| `write()`  | 송신 버퍼 공간 |

---

# 8. 블로킹 I/O

`ServerSocket.accept()`는 대표적인 블로킹 메서드이다.

블로킹이란 작업이 완료될 때까지 현재 스레드가 다음 코드로 진행하지 못하고 기다리는 것을 의미한다.

```java
Socket clientSocket = serverSocket.accept();
System.out.println("다음 코드");
```

클라이언트가 접속하지 않으면 `"다음 코드"`는 출력되지 않는다.

```text
메인 스레드
    │
    ├── accept() 호출
    │
    ├── 클라이언트가 없으면 대기
    │
    ├── 클라이언트 접속
    │
    └── 다음 코드 실행
```

이러한 방식으로 동작하기 때문에 현재 서버는 **Blocking Socket 서버**이다.

---

# 9. 운영체제의 연결 대기 큐

TCP 서버에는 운영체제가 관리하는 연결 대기 영역이 존재한다.

```java
ServerSocket serverSocket = new ServerSocket(PORT);
```

이 코드가 실행되면 운영체제는 9000번 포트를 리스닝 상태로 만들고, 해당 포트로 들어오는 연결 요청을 관리한다.

개념적으로 다음과 같이 이해할 수 있다.

```text
클라이언트 A 연결 완료 ┐
클라이언트 B 연결 완료 ├── 운영체제 내부 연결 대기 영역
클라이언트 C 연결 완료 ┘
                           ↓
                  serverSocket.accept()
                           ↓
                    Socket 하나 반환
```

`accept()`는 개발자가 직접 만든 Java의 `Queue`에서 데이터를 꺼내는 것이 아니다.

운영체제가 관리하는 연결 대기 목록에서 완료된 TCP 연결 하나를 받아오는 작업이다.

따라서 코드에서 `Queue` 자료구조를 생성하지 않았더라도 운영체제 내부에서는 연결 요청을 관리하고 있을 수 있다.

---

# 10. 클라이언트 한 명만 처리하는 서버

다음 코드는 `accept()`를 한 번만 호출한다.

```java
Socket clientSocket = serverSocket.accept();
```

따라서 애플리케이션 코드 기준으로 클라이언트 한 명만 수락하고 처리한다.

```java
public static void main(String[] args) throws IOException {

    ServerSocket serverSocket = new ServerSocket(9000);

    System.out.println("클라이언트 대기 중");

    Socket clientSocket = serverSocket.accept();

    System.out.println(
            "클라이언트 접속: "
                    + clientSocket.getRemoteSocketAddress()
    );

    clientSocket.close();
    serverSocket.close();
}
```

다만 서버가 첫 번째 클라이언트를 처리하는 동안 다른 클라이언트의 연결 요청이 운영체제의 대기 영역에 잠시 들어갈 수는 있다.

애플리케이션이 여러 클라이언트를 계속 수락하려면 `accept()`를 반복해야 한다.

```java
while (true) {
    Socket clientSocket = serverSocket.accept();
}
```

현재 단계에서는 `while`을 사용하지 않고, 한 명의 접속 과정부터 이해하는 것이 목적이다.

---

# 11. `getInputStream()`

```java
clientSocket.getInputStream();
```

`getInputStream()`은 클라이언트가 서버로 보낸 데이터를 읽을 수 있는 입력 통로를 반환한다.

```java
InputStream inputStream = clientSocket.getInputStream();
```

네트워크에서 데이터는 기본적으로 바이트 단위로 전달된다.

예를 들어 클라이언트가 `"hello"`를 보냈다고 해도 네트워크에서는 문자 자체가 아니라 인코딩된 바이트가 전달된다.

```text
클라이언트
    │
    │ 01101000 01100101 01101100 01101100 01101111
    ▼
InputStream
```

`InputStream`은 이 바이트 데이터를 읽는 객체이다.

---

# 12. `InputStreamReader`

`InputStream`은 바이트를 읽지만, 채팅 프로그램에서는 일반적으로 문자열을 다룬다.

따라서 바이트를 문자로 변환하는 객체가 필요하다.

```java
InputStreamReader inputStreamReader =
        new InputStreamReader(
                clientSocket.getInputStream(),
                StandardCharsets.UTF_8
        );
```

`InputStreamReader`는 바이트 기반 입력 스트림을 문자 기반 입력 스트림으로 변환한다.

```text
InputStream에서 받은 바이트
            ↓
      UTF-8로 해석
            ↓
    InputStreamReader
            ↓
    "hello", "안녕하세요"
```

즉, `InputStreamReader`는 바이트와 문자 사이를 연결하는 변환기 역할을 한다.

---

# 13. `StandardCharsets.UTF_8`

```java
StandardCharsets.UTF_8
```

이 코드는 다음 의미이다.

> 들어오는 바이트를 UTF-8 문자 인코딩 규칙으로 해석한다.

클라이언트와 서버가 서로 다른 문자 인코딩을 사용하면 한글 등이 깨질 수 있다.

따라서 양쪽에서 동일하게 UTF-8을 사용하는 것이 좋다.

```java
new InputStreamReader(
        clientSocket.getInputStream(),
        StandardCharsets.UTF_8
);
```

예를 들어 클라이언트가 `"안녕하세요"`를 UTF-8로 변환해서 전송했다면 서버도 UTF-8로 해석해야 정상적인 문자열을 얻을 수 있다.

```text
클라이언트 문자열
"안녕하세요"
      ↓ UTF-8 인코딩
바이트 데이터
      ↓ 네트워크 전송
서버 InputStream
      ↓ UTF-8 디코딩
"안녕하세요"
```

---

# 14. `BufferedReader`

문자열을 한 줄씩 편리하게 읽으려면 `InputStreamReader`를 `BufferedReader`로 감싼다.

```java
BufferedReader reader =
        new BufferedReader(
                new InputStreamReader(
                        clientSocket.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );
```

구조를 바깥쪽부터 보면 다음과 같다.

```text
BufferedReader
    └── InputStreamReader
            └── InputStream
                    └── Socket
```

각 객체의 역할은 다음과 같다.

| 객체                  | 역할                     |
| ------------------- | ---------------------- |
| `Socket`            | 클라이언트와의 TCP 연결         |
| `InputStream`       | 네트워크 바이트 읽기            |
| `InputStreamReader` | 바이트를 문자로 변환            |
| `BufferedReader`    | 문자를 버퍼에 모으고 한 줄 단위로 읽기 |

문자열 한 줄은 다음과 같이 읽을 수 있다.

```java
String message = reader.readLine();
```

`readLine()` 역시 클라이언트가 한 줄을 보낼 때까지 기다리는 블로킹 I/O 메서드이다.

---

# 15. 전체 입력 흐름

클라이언트가 메시지를 보냈을 때 서버에서 문자열로 읽는 과정은 다음과 같다.

```text
클라이언트 문자열
"hello"
    ↓
UTF-8 바이트로 변환
    ↓
TCP 네트워크 전송
    ↓
Socket
    ↓
InputStream
    ↓
InputStreamReader
    ↓
BufferedReader
    ↓
String message
```

코드로 작성하면 다음과 같다.

```java
BufferedReader reader =
        new BufferedReader(
                new InputStreamReader(
                        clientSocket.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );

String message = reader.readLine();

System.out.println("클라이언트 메시지: " + message);
```

---

# 16. 클라이언트 한 명에게 메시지를 받는 서버

```java
package chat.blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BlockingChatServer {

    private static final int PORT = 9000;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(PORT);

        System.out.println("서버가 시작되었습니다.");
        System.out.println("클라이언트의 연결을 기다립니다.");

        Socket clientSocket = serverSocket.accept();

        System.out.println(
                "클라이언트 접속: "
                        + clientSocket.getRemoteSocketAddress()
        );

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

        System.out.println("클라이언트 메시지를 기다립니다.");

        String message = reader.readLine();

        System.out.println("클라이언트 메시지: " + message);

        reader.close();
        clientSocket.close();
        serverSocket.close();

        System.out.println("서버를 종료합니다.");
    }
}
```

이 코드의 블로킹 지점은 두 곳이다.

```java
Socket clientSocket = serverSocket.accept();
```

클라이언트가 접속할 때까지 기다린다.

```java
String message = reader.readLine();
```

클라이언트가 메시지 한 줄을 보낼 때까지 기다린다.

---

# 17. 정리

## 서버 실행

```java
ServerSocket serverSocket = new ServerSocket(9000);
```

9000번 포트를 열고 클라이언트 연결을 기다릴 준비를 한다.

## 클라이언트 접속 대기

```java
Socket clientSocket = serverSocket.accept();
```

클라이언트가 접속할 때까지 스레드가 기다린다.

## 데이터 입력 통로 생성

```java
clientSocket.getInputStream();
```

클라이언트가 보낸 바이트 데이터를 읽을 수 있는 입력 스트림을 가져온다.

## 바이트를 문자로 변환

```java
new InputStreamReader(
        clientSocket.getInputStream(),
        StandardCharsets.UTF_8
);
```

네트워크에서 받은 바이트를 UTF-8 문자열로 해석한다.

## 한 줄 단위로 읽기

```java
BufferedReader reader = new BufferedReader(...);
String message = reader.readLine();
```

클라이언트가 보낸 문자열을 한 줄 단위로 읽는다.

---

# 핵심 요약

```text
ServerSocket
= 연결 요청 접수 담당

Socket
= 연결된 클라이언트와 실제 통신 담당

accept()
= 클라이언트가 접속할 때까지 기다리는 블로킹 I/O

InputStream
= 네트워크 바이트 입력

InputStreamReader
= 바이트를 문자로 변환

BufferedReader
= 문자를 버퍼링하고 한 줄씩 읽기

UTF-8
= 바이트를 문자로 해석하는 인코딩 규칙
```
---
1. COW란?
   COW는 Copy-On-Write의 약자로, 쓰기 작업 시 데이터의 복사본을 만들어 수정 후 원본을 교체하는 전략입니다. 
    읽기 작업은 기존 데이터를 그대로 사용해 락 없이 빠르고 안전하며 다중 스레드 환경에서 읽기 작업이 많을 때 유용하게 사용됩니다.