BlockingChatServer.java를 생성했지만 아무것도 몰라서 자료가 필요할꺼같다...
일단은 포트를 열어서 클라이언트 한명만 들어올수있게...
## BlockingChatServer.java 목표
1. 서버를 실행
2. 클라이언트 접속

IOException : input output 입출력 작업중 발생할 수 있는 오류를 뜻함
- 메서드 선언부에 붙이는 구문으로 해당 메서드가 입출력예외를 발생시킬 수 있음
- 메서드를 호출한 곳에서 예외를 처리하도록 위임하는 방식

Oracle java SE 공식 문서에서는 다음과 같이 정의
- Socket : 원격 호스트에 연결하는 TCP 통신 엔드포인트(연결된 상대와 실제 통신 담당)
- ServerSocket : 클라이언트의 연결을 받아들이는 TCP 서버용 리스닝 소켓(연결 요청 접수 담당)

accept()도 I/O인 이유

I/O는 단순히 데이터를 읽거나 쓰는 것만 의미하지 않는다. 외부 장치나 운영체제의 네트워크 상태를 기다리는 작업도 I/O에 포함된다.

네트워크 서버에서 대표적인 I/O 작업은 다음과 같다.

serverSocket.accept();              // 연결 요청 기다리기
clientSocket.getInputStream().read(); // 클라이언트 데이터 읽기
clientSocket.getOutputStream().write(); // 클라이언트에게 데이터 쓰기

세 작업 모두 조건이 충족되지 않으면 스레드가 기다릴 수 있다.

accept()는 클라이언트 연결을 기다린다.
read()는 클라이언트가 데이터를 보내기를 기다린다.
write()는 OS 송신 버퍼에 공간이 생기기를 기다릴 수 있다.

운영체제 커널이 내부적으로 관리하는 TCP 연결 대기 큐를 뜻해.

ServerSocket serverSocket = new ServerSocket(PORT);

이 코드가 실행되면 운영체제는 9000번 포트를 리스닝 상태로 만들고, 해당 포트로 들어오는 연결 요청을 내부적으로 관리해.

흐름은 다음과 같아.

클라이언트 A 연결 완료 ┐
클라이언트 B 연결 완료 ├─ 운영체제 내부 연결 대기 큐
클라이언트 C 연결 완료 ┘
↓
serverSocket.accept()
↓
Socket 하나 반환

accept()는 네가 만든 큐에서 꺼내는 것이 아니라, 운영체제가 관리하는 연결 대기 목록에서 완료된 TCP 연결 하나를 받아오는 시스템 호출이야.

