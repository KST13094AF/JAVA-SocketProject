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
	ChatServer myServer; // ChatServer ��ü
	Socket mySocket; // Ŭ���̾�Ʈ ����
	String name;	// Ŭ���̾�Ʈ �̸��� �ޱ� ���� ����
	InetAddress ip;
	PrintWriter out; // ����� ��Ʈ��
	BufferedReader in;
	Boolean ThreadState = false; //������ ������ ���� ���� 

	// ���� ��¥�� �޾ƿͼ� �������.
	LocalTime time = LocalTime.now();
	String formatter = "[HH:mm:ss a]";
	DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(formatter);
	String realTime = time.format(formatter2);
	
	//������
	public ChatThread(ChatServer server, Socket socket) 
	{
		super("ChatThread");
		myServer = server;
		mySocket = socket;
		ip = socket.getInetAddress(); //������ ���� ���ؼ� �����Ǹ� ����.
		try {
			out = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			// ���ø����� ���� �̸��� �޾ƿ�.
			name = in.readLine();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	// �޼��� ����
	public void sendMessage(String msg) throws IOException // �޽����� ����
	{
		out.println(msg);
		out.flush();
	}
	// ���� ����(Ư���� �� �մ� Ŭ���̾�Ʈ)
	public void disconnect(String name) // ������ ����
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
	// ��ȭ���� ������ �ȴٸ� ���� ����(Ư�� x)
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

	public void run() // ������ ����
	{
		try {
			while (!ThreadState) { // Ŭ���̾�Ʈ ���� �޽����� ó���ϱ� ���� ��ٸ�
				String inLine = in.readLine(); // ��ٸ��� �ִٰ� Ŭ���̾�Ʈ�� ���� �޽����� �ִ� ��� �о����
				if (!inLine.equals("") && !inLine.equals(null)) {
					messageProcess(inLine); // Ŭ���̾�Ʈ�� ���� �޽����� Ȯ���Ͽ� ���� ������ ��� Ŭ���̾�Ʈ���� ��ε�ĳ��Ʈ
				}
			}
		} catch (Exception e) {
			disconnect(name);
		}
	}

	// Ŭ���̾�Ʈ�� ���� �޽����� Ȯ���� �� ó��
	public void messageProcess(String msg) {
		System.out.println("["+realTime+ "] "+msg); // ȭ�鿡 ���
		String filter[] = { "�ù�", "����", "�ٺ�", "��û��", "������", "������","���","������"}; // �弳 ����
		
		StringTokenizer st = new StringTokenizer(msg, "|"); // ��Ģ�� ���� ���� �޽����� �и��Ͽ� Ȯ��
		String command = st.nextToken(); // �޽����� ��� command �κ�
		String talk = st.nextToken(); // �޽����� ��ȭ talk �κ�
		// �̷����� �弳�� �����ؼ� ���� .
		for(int i =0; i<filter.length; i++) {
			talk = talk.replace(filter[i], "�̷�");
		}
		if (command.equals("LOGIN")) { // ���� �޽����� LOGIN �̸� ó�� ���� �޽����̱� ������ ���� ó��
			String enter = "["+realTime+ "] "+"[����] " + name +"���� �����ϼ̽��ϴ�.";
			System.out.println(enter);
			try { // ���ο� Ŭ���̾�Ʈ�� �����Ͽ� �߰��� Ŭ���̾�Ʈ ���� ��ε�ĳ��Ʈ
				myServer.broadcast(enter);
				myServer.broadcast("[���� �����ڼ�] " + myServer.clientNum + "��");
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else if (command.equals("LOGOUT")) { // ���� �޽����� LOGOUT �̸� ���� �޽����̹Ƿ� ���ŵ� Ŭ���̾�Ʈ�� ����
			try { // ��ε�ĳ��Ʈ
				myServer.clientNum--;
				myServer.broadcast("["+realTime+ "] "+"[���� ����] " + talk);
				myServer.broadcast("[���� �����ڼ�] " + myServer.clientNum + "��");
			} catch (IOException e) {
				System.out.println(e.toString());
			}
			disconnect(talk.trim()); // ���� ����
		} 	// �ӼӸ� ��� ���� ��ũ�������� �Ľ� �� ��� ����
		else if (command.equals("WHISPER")) {
			StringTokenizer token = new StringTokenizer(talk, "<>[]"); // <����> �� �ȳ� [ȫ�浿] <����> ��
			String sname = token.nextToken();
			token.nextToken();
			String name = token.nextToken();
			String realTalk = token.nextToken();
			String talk2 = "From " + "[" + sname + "] : " + realTalk;
			try {
				if (myServer.clientMap.get(name) == null) {
					out.println("["+realTime+ "] "+" �ش� �����ڴ� �������� �ƴմϴ�.");
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
		// �����Ѵٸ� �� ����� ������ ���������� ������ ����Ǿ��ٴ� �޼��� ����
		else if (command.equals("exit")) {
			try {
				myServer.clientNum--;
				disconnect(talk.trim());
				myServer.broadcast("["+realTime+ "] "+talk + "���� ���忡 ���� ����Ǿ����ϴ�.", talk);
				myServer.broadcast("[���� �����ڼ�] " + myServer.clientNum + "��", talk);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		} else {
			try { // LOGIN, LOGOUT �̿��� ���� �Ϲ� �޽����� ��� Ŭ���̾�Ʈ���� ���� �޽��� ����
				myServer.broadcast("["+realTime+ "] "+talk);
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
}