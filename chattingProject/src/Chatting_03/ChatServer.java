package Chatting_03;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class ChatServer extends Thread {
	HashMap<String, ChatThread> clientMap = new HashMap<String, ChatThread>();
	Vector<InetAddress> ban = new Vector<InetAddress>();
	int clientNum = 0; // 접속된 클라이언트의 수

	// 모든 클라이언트 브로드캐스트
	public void broadcast(String msg) throws IOException {
		synchronized (clientMap) {
			for (Entry<String, ChatThread> hm : clientMap.entrySet()) {
				synchronized (hm.getValue()) {
					hm.getValue().sendMessage(msg);
				}
			}
		}
	}

	// 특정 인물을 제외한 브로드캐스트
	public void broadcast(String msg, String name) throws IOException {
		synchronized (clientMap) {
			for (Entry<String, ChatThread> hm : clientMap.entrySet()) {
				synchronized (hm.getValue()) {
					if (hm.getKey() != name)
						hm.getValue().sendMessage(msg);
				}
			}
		}
	}

	// 유니캐스트 기능
	public void unicast(String name, String msg) throws IOException {
		synchronized (clientMap) {
			ChatThread client = clientMap.get(name);
			synchronized (client) {
				client.sendMessage(msg);
			}
		}
	}

	// 종료시 clientVector에 저장되어 있는 클라이언트 정보를 제거
	public void removeClient(String name) {
		synchronized (clientMap) {
			clientMap.remove(name);
			System.gc();
		}
	}

	// 처음 연결 되었을때 ban백터에 저장된 아이피가 아니고 중복된 이름을 사용하지 않았다면 객체 생성.
	public void addClient(ChatThread client) throws IOException {
		synchronized (clientMap) {
			if (ban.contains(client.ip)) {
				client.sendMessage("ipBan");
				return;
			} else if (clientMap.containsKey(client.name)) {
				client.sendMessage("OverLap");
				client.OverLapdisConnect();
				return;
			} else {
				client.sendMessage("OK");
				clientMap.put(client.name, client);
				clientNum++;
			}
		}
	}

	// 강퇴한 유저의 아이피를 백터에 저장
	public void addBanList(String name) {
		synchronized (clientMap) {
			ChatThread client = clientMap.get(name);
			ban.addElement(client.ip);
		}
	}

	public void serverStop() {
		try {
			for(Entry<String, ChatThread> a : clientMap.entrySet()) {
				a.getValue().mySocket.close();
				clientMap.remove(a.getKey());
			}
			System.exit(-1);
		} catch (Exception e) {
			
		}
	}
	
	// 서버도 스레드를 돌려서 명령어를 수행 할 수 있게 됨.
	public void run() {
		while (true) {
			Scanner s = new Scanner(System.in);
			System.out.println("************명령어 입력************"); // 강퇴> 대화명
			String cmd = s.nextLine().trim();
			if (!cmd.equals("") && !cmd.equals(null) && cmd.contains(">")) {
				StringTokenizer st = new StringTokenizer(cmd, ">");
				if (st.countTokens() > 0) {
					cmd = st.nextToken();
					if(cmd.equals("종료")) {
						try {
							broadcast("서버측에서 종료하였습니다.");
							System.out.println("시스템이 종료됩니다.");
							serverStop();
							s.close();
							return;
						} catch (IOException e) {}
					}
					String name = st.nextToken();
					if (cmd.equals("강퇴") && clientMap.get(name) != null) {
						ChatThread ban = clientMap.get(name);
						try {
							addBanList(name);
							ban.sendMessage("exit");
							ban.messageProcess("exit|" + name);
						} catch (IOException e) {
						}
						System.out.println(name + "사용자를 강퇴합니다.");
					} else
						System.out.println("잘못된 명령 또는 사용자가 존재하지 않습니다.");
				} else
					System.out.println("잘못된 명령입니다.");
			} else 
				System.out.println("명령어를 입력해 주십시오.");
		}
	}

	// 서버의 시작 메인 메소드
	public static void main(String[] args) {
		// 서버 소켓
		ServerSocket myServerSocket = null;
		// ChatServer 객체 생성
		ChatServer myServer = new ChatServer();
		try {
			// 서버 포트50000를 가지는 서버 소켓 생성
			myServerSocket = new ServerSocket(50000);
		} catch (IOException e) {
			System.out.println(e.toString());
			System.exit(-1);
		}

		System.out.println("[서버 대기 상태] " + myServerSocket);

		try {
			// 다수의 클라이언트 접속을 처리하기 위해 반복문으로 구현
			while (true) {
				// 클라이언트가 접속되었을 경우 이 클라이언트를 처리하기 위한 ChatThread 객체 생성
				ChatThread client = new ChatThread(myServer, myServerSocket.accept());
				// 서버 스레드가 생성되지 않은 상태라면 실행.
				if (myServer.getState() == Thread.State.NEW)
					myServer.start();
				// 해쉬맵 컬렉션에 클라이언트 저장.
				myServer.addClient(client);
				// 챗스레드 실행
				client.start();
				// 접속한 클라이언트의 수 증가
				
				System.out.println("[현재 접속자수] " + myServer.clientNum + "명");
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
}