package Chatting_03;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class ChatServer extends Thread {
	HashMap<String, ChatThread> clientMap = new HashMap<String, ChatThread>();
	Vector<InetAddress> ban = new Vector<InetAddress>();
	int clientNum = 0; // ���ӵ� Ŭ���̾�Ʈ�� ��

	// ��� Ŭ���̾�Ʈ ��ε�ĳ��Ʈ
	public void broadcast(String msg) throws IOException {
		synchronized (clientMap) {
			for (Entry<String, ChatThread> hm : clientMap.entrySet()) {
				synchronized (hm.getValue()) {
					hm.getValue().sendMessage(msg);
				}
			}
		}
	}

	// Ư�� �ι��� ������ ��ε�ĳ��Ʈ
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

	// ����ĳ��Ʈ ���
	public void unicast(String name, String msg) throws IOException {
		synchronized (clientMap) {
			ChatThread client = clientMap.get(name);
			synchronized (client) {
				client.sendMessage(msg);
			}
		}
	}

	// ����� clientVector�� ����Ǿ� �ִ� Ŭ���̾�Ʈ ������ ����
	public void removeClient(String name) {
		synchronized (clientMap) {
			clientMap.remove(name);
			System.gc();
		}
	}

	// ó�� ���� �Ǿ����� ban���Ϳ� ����� �����ǰ� �ƴϰ� �ߺ��� �̸��� ������� �ʾҴٸ� ��ü ����.
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

	// ������ ������ �����Ǹ� ���Ϳ� ����
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
	
	// ������ �����带 ������ ��ɾ ���� �� �� �ְ� ��.
	public void run() {
		while (true) {
			Scanner s = new Scanner(System.in);
			System.out.println("************��ɾ� �Է�************"); // ����> ��ȭ��
			String cmd = s.nextLine().trim();
			if (!cmd.equals("") && !cmd.equals(null) && cmd.contains(">")) {
				StringTokenizer st = new StringTokenizer(cmd, ">");
				if (st.countTokens() > 0) {
					cmd = st.nextToken();
					if(cmd.equals("����")) {
						try {
							broadcast("���������� �����Ͽ����ϴ�.");
							System.out.println("�ý����� ����˴ϴ�.");
							serverStop();
							s.close();
							return;
						} catch (IOException e) {}
					}
					String name = st.nextToken();
					if (cmd.equals("����") && clientMap.get(name) != null) {
						ChatThread ban = clientMap.get(name);
						try {
							addBanList(name);
							ban.sendMessage("exit");
							ban.messageProcess("exit|" + name);
						} catch (IOException e) {
						}
						System.out.println(name + "����ڸ� �����մϴ�.");
					} else
						System.out.println("�߸��� ��� �Ǵ� ����ڰ� �������� �ʽ��ϴ�.");
				} else
					System.out.println("�߸��� ����Դϴ�.");
			} else 
				System.out.println("��ɾ �Է��� �ֽʽÿ�.");
		}
	}

	// ������ ���� ���� �޼ҵ�
	public static void main(String[] args) {
		// ���� ����
		ServerSocket myServerSocket = null;
		// ChatServer ��ü ����
		ChatServer myServer = new ChatServer();
		try {
			// ���� ��Ʈ50000�� ������ ���� ���� ����
			myServerSocket = new ServerSocket(50000);
		} catch (IOException e) {
			System.out.println(e.toString());
			System.exit(-1);
		}

		System.out.println("[���� ��� ����] " + myServerSocket);

		try {
			// �ټ��� Ŭ���̾�Ʈ ������ ó���ϱ� ���� �ݺ������� ����
			while (true) {
				// Ŭ���̾�Ʈ�� ���ӵǾ��� ��� �� Ŭ���̾�Ʈ�� ó���ϱ� ���� ChatThread ��ü ����
				ChatThread client = new ChatThread(myServer, myServerSocket.accept());
				// ���� �����尡 �������� ���� ���¶�� ����.
				if (myServer.getState() == Thread.State.NEW)
					myServer.start();
				// �ؽ��� �÷��ǿ� Ŭ���̾�Ʈ ����.
				myServer.addClient(client);
				// ê������ ����
				client.start();
				// ������ Ŭ���̾�Ʈ�� �� ����
				
				System.out.println("[���� �����ڼ�] " + myServer.clientNum + "��");
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
}