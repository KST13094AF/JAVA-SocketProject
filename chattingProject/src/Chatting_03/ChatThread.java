package Chatting_03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

public class ChatThread extends Thread {
	ChatServer myServer; // ChatServer 객체
	Socket mySocket; // 클라이언트 소켓
	String name;	// 클라이언트 이름을 받기 위한 변수
	InetAddress ip;
	PrintWriter out; // 입출력 스트림
	BufferedReader in;
	Boolean ThreadState = false; //스레드 조작을 위한 변수 

	// 현재 날짜를 받아와서 출력해줌.
	LocalTime time = LocalTime.now();
	String formatter = "[HH:mm:ss a]";
	DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(formatter);
	String realTime = time.format(formatter2);
	
	//생성자
	public ChatThread(ChatServer server, Socket socket) 
	{
		super("ChatThread");
		myServer = server;
		mySocket = socket;
		ip = socket.getInetAddress(); //아이피 밴을 위해서 아이피를 저장.
		try {
			out = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			// 애플릿에서 보낸 이름을 받아옴.
			name = in.readLine();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	// 메세지 전송
	public void sendMessage(String msg) throws IOException // 메시지를 전송
	{
		out.println(msg);
		out.flush();
	}
	// 연결 종료(특정할 수 잇는 클라이언트)
	public void disconnect(String name) // 연결을 종료
	{
		try {
			ThreadState = true;
			out.flush();
			in.close();
			out.close();
			mySocket.close();
			myServer.removeClient(name);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	// 대화명이 오버랩 된다면 연결 종료(특정 x)
	public void OverLapdisConnect() {
		try {
			if(Thread.State.RUNNABLE!=null)
				ThreadState = true;
			out.flush();
			in.close();
			out.close();
			mySocket.close();
			myServer = null;
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	public void run() // 쓰레드 시작
	{
		try {
			while (!ThreadState) { // 클라이언트 보낸 메시지를 처리하기 위해 기다림
				String inLine = in.readLine(); // 기다리고 있다가 클라이언트가 보낸 메시지가 있는 경우 읽어들임
				if (!inLine.equals("") && !inLine.equals(null)) {
					messageProcess(inLine); // 클라이언트가 보낸 메시지를 확인하여 현재 접속한 모든 클라이언트에게 브로드캐스트
				}
			}
		} catch (Exception e) {
			disconnect(name);
		}
	}

	// 클라이언트가 보낸 메시지를 확인한 후 처리
	public void messageProcess(String msg) {
		System.out.println("["+realTime+ "] "+msg); // 화면에 출력
		String filter[] = { "시발", "병신", "바보", "멍청이", "개새기", "개새끼","등신","머저리"}; // 욕설 필터
		
		StringTokenizer st = new StringTokenizer(msg, "|"); // 규칙에 따라 받은 메시지를 분리하여 확인
		String command = st.nextToken(); // 메시지의 명령 command 부분
		String talk = st.nextToken(); // 메시지의 대화 talk 부분
		// 이런으로 욕설을 필터해서 보냄 .
		for(int i =0; i<filter.length; i++) {
			talk = talk.replace(filter[i], "이런");
		}
		if (command.equals("LOGIN")) { // 받은 메시지가 LOGIN 이면 처음 접속 메시지이기 때문에 접속 처리
			String enter = "["+realTime+ "] "+"[접속] " + name +"님이 입장하셨습니다.";
			System.out.println(enter);
			try { // 새로운 클라이언트가 접속하여 추가된 클라이언트 수를 브로드캐스트
				myServer.broadcast(enter);
				myServer.broadcast("[현재 접속자수] " + myServer.clientNum + "명");
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else if (command.equals("LOGOUT")) { // 받은 메시지가 LOGOUT 이면 종료 메시지이므로 제거된 클라이언트의 수를
			try { // 브로드캐스트
				myServer.clientNum--;
				myServer.broadcast("["+realTime+ "] "+"[접속 종료] " + talk);
				myServer.broadcast("[현재 접속자수] " + myServer.clientNum + "명");
			} catch (IOException e) {
				System.out.println(e.toString());
			}
			disconnect(talk.trim()); // 연결 종료
		} 	// 귓속말 기능 구현 토크나이저로 파싱 후 결과 대입
		else if (command.equals("WHISPER")) {
			StringTokenizer token = new StringTokenizer(talk, "<>[]"); // <애플> 야 안녕 [홍길동] <애플> 야
			String sname = token.nextToken();
			token.nextToken();
			String name = token.nextToken();
			String realTalk = token.nextToken();
			String talk2 = "From " + "[" + sname + "] : " + realTalk;
			try {
				if (myServer.clientMap.get(name) == null) {
					out.println("["+realTime+ "] "+" 해당 접속자는 접속중이 아닙니다.");
					out.flush();
				} else {
					out.println("["+realTime+ "] "+ " To ["+name+"] "+realTalk);
					out.flush();
					myServer.unicast(name, "["+realTime+ "] "+talk2);
				}
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} 
		// 강퇴한다면 그 대상을 제외한 나머지에게 누군가 강퇴되었다는 메세지 전송
		else if (command.equals("exit")) {
			try {
				myServer.clientNum--;
				disconnect(talk.trim());
				myServer.broadcast("["+realTime+ "] "+talk + "님이 방장에 의해 강퇴되었습니다.", talk);
				myServer.broadcast("[현재 접속자수] " + myServer.clientNum + "명", talk);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			try { // LOGIN, LOGOUT 이외의 경우는 일반 메시지로 모든 클라이언트에게 받은 메시지 전송
				myServer.broadcast("["+realTime+ "] "+talk);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
}