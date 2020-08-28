package Chatting_03;

import java.net.*;
import java.io.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

//192.168.219.164
public class ChatClient extends Applet implements ActionListener, Runnable {
	private static final long serialVersionUID = 1L;

	Socket mySocket = null;
	PrintWriter out = null;
	BufferedReader in = null;

	int setN = 0; // 현재 클라이언트를 0, 1, 2, 3 상태로 나누어 저장함.
	Boolean ThreadState = false; // 스레드 조작을 위한 변수
	String saveName;

	TextField serverIp;
	Button connect;
	Button disconnect;
	Thread clock;
	TextArea memo;
	TextArea clients;
	TextField name;
	TextField input;
	Panel upPanel, downPanel;
	Toolkit toolkit = Toolkit.getDefaultToolkit();

	public void init() {
		// GUI
		setLayout(new BorderLayout());

		// 텍스트 에어리어 보더레이아웃의 중앙에 위치
		memo = new TextArea(10, 55);
		add("Center", memo);
	

		// 패널 생성하여 패널에 IP 주소 입력을 위한 텍스트필드와 연결 버튼 추가
		upPanel = new Panel();

		serverIp = new TextField(12);
		serverIp.setText("192.168.219.164");
		upPanel.add(serverIp);

		connect = new Button("연결");
		disconnect = new Button("연결 해제");
		connect.addActionListener(this);
		disconnect.addActionListener(this);
		upPanel.add(connect);
		upPanel.add(disconnect);

		// 생성된 패널을 보더레이아웃의 위쪽에 위치
		add("North", upPanel);

		// 패널 생성하여 대화명을 위한 텍스트필드와 입력을 위한 텍스트필드 추가

		downPanel = new Panel();
		name = new TextField(8);
		name.setText("대화명");
		downPanel.add(name);
		input = new TextField(32);
		input.setEditable(false);
		input.addActionListener(this); // 사용자가 엔터키를 누르면 메시지가 전송되도록 이벤트 연결
		downPanel.add(input);
		add("South", downPanel); // 보더레이아웃의 아래쪽에 패널 위치
	}

	// 서버에서 보내는 메세지를 읽어서 memo에 append
	public void run() {
		try {
			while (!ThreadState) {
				String msg = in.readLine(); // 상대방이 보낸 메시지를 읽어들임
				if (!msg.equals("") && !msg.equals(null)) {
					if (msg.equals("exit")) { // 받은 메시지가 exit라면 상태를 2로 바꾸고 종료
						setN = 2;
						stop();
					}else {
						memo.append(msg + "\n"); // 내 화면의 memo에 받은 메시지 출력
					}
				}
			}
		} catch (IOException e) {
			memo.append("연결 종료...\n");
		}
	}

	// 엑션 리스너 구현
	public void actionPerformed(ActionEvent e) // connect 버튼이 눌린 경우와 input 텍스트필드에
	{ // 엔터가 들어왔을 경우 실행
		if (e.getSource() == connect && setN == 0) { // 커넥션이 눌려서 발생하는데 상태가 1보다 낮은 상태여야함
			try {
				setN = 1;
				mySocket = new Socket(serverIp.getText(), 50000); // 포트 아이피 커넥션

				// 생성된 소켓을 이용해 서버와의 입출력 스트림을 생성
				out = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
				in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
				saveName = name.getText();
				// 스트림 생성 후 바로 애플릿의 대화명을 보내줌
				out.println(name.getText());
				out.flush();
				name.setEditable(false);
				// 두번째로 현재 클라이언트 정보가 잘 들어갔는지에 대한 대답을 받음
				String msg = in.readLine();
				// 받은 메세지가 ipBan이라면 아이피주소와 접근 시도 메세지를 주고 stop 하고 현재 클라이언트 상태를 3으로
				if (msg.equals("ipBan")) {
					out.println(mySocket.getInetAddress() + " 접근 시도...");
					out.flush();
					setN = 3;
					stop();
				}
				// 받은 메세지가 OverLap이라면 클라이언트명이 중복되므로 상태를 실행전인 0으로 바꾸고 stop
				else if (msg.equals("OverLap")) {
					name.setText("대화명");
					memo.append("대화명이 중복됩니다.\n");
					setN = 0;
					stop();
				}
				// 받은 메세지가 OK라면 로그인 성공함. 스레드 실행
				else if (msg.equals("OK")) {
					out.println("LOGIN|" + mySocket); // 초기 서버에 접속한 후 접속 메시지를 보냄
					out.flush();
					input.setEditable(true);
					memo.append("[접속하였습니다]" + "\n"); // 내 화면의 텍스트에어리어 memo에 접속메시지 출력
					memo.setEditable(false);
					serverIp.setEditable(false);
					// 쓰레드를 동작시킴
					if (clock == null) {
						clock = new Thread(this);
						clock.start();
					}
				}
			} catch (UnknownHostException ie) {
				setN = 0;
				System.out.println(ie.toString());
			} catch (IOException ie) {
				setN = 0;
				System.out.println(ie.toString());
			}

		}
		// 디스 커넥션 버튼을 눌렀을때 현재 상태가 0 2 3 이면 소용 없으므로 비프음을 내고 메세지 출력
		else if (e.getSource() == disconnect) {
			if (setN == 0 || setN == 2 || setN == 3) {
				toolkit.beep();
				memo.append("연결되어 있지 않습니다..\n");
			}
			// 상태가 1이라면 정상 종료
			else {
				memo.setEditable(true);
				serverIp.setEditable(true);
				input.setEditable(false);
				stop();
			}
		}
		// input 텍스트필드에 엔터가 입력될 경우
		else if (e.getSource() == input) {
			String data = input.getText(); // input 텍스프필드의 값을 읽어서
			input.setText("");
			// 이름변경이 되면 안되기 때문에 계속해서 저장해줌.
			name.setText(saveName);
			// 형식에 맞춰 서버에 메시지를 전송 <홍길동> msg
			if (data.contains(">") && data.contains("<")) { // <애플>
				out.println("WHISPER|" + "[" + name.getText() + "]" + " to " + data);
				out.flush(); // 버퍼에 있는 출력 메시지를 상대방에게 강제로 전송
			} else {
				out.println("TALK|" + "[" + name.getText() + "]" + " : " + data);
				out.flush(); // 버퍼에 있는 출력 메시지를 상대방에게 강제로 전송
			}
		}
		// 중복 클릭으로 인한 데이터 중복을 막기위해 상태가 1인 경우 이미 접속중인 상태
		else if (setN == 1) {
			memo.append("이미 접속중 입니다.. \n");
			toolkit.beep();
			return;
		}
		// 현재 상태가 0 1 2 가 아닌 3이라면 강퇴당했음으로 권한이 없음.
		else {
			memo.append("접근 권한이 없습니다.. \n");
			toolkit.beep();
			return;
		}
	}

	public void stop() // 쓰레드를 종료시키고 종료 메시지를 서버에 전송하고 모든 연결을 닫음
	{
		name.setEditable(true);
		if ((clock != null) && (clock.isAlive())) {
			clock = null; // 쓰레드 종료
		}
		// 현재 상태에 맞는 대화를 보내고 비프음. 또는 정상 종료 후 스레드 와 스트림 소켓 반납.
		if (setN == 0) {
			out.println("대화명 중복으로 인한 커넥션 종료\n");
			out.flush();
			memo.append("종료....\n");
			toolkit.beep();
		} else if (setN == 1) {
			out.println("LOGOUT|" + name.getText());
			out.flush();
			setN = 0;
		} else if (setN == 2) {
			memo.append("방장에 의해 강퇴당하였습니다...\n");
			toolkit.beep();
		} else if (setN == 3) {
			memo.append("해당 IP는 사용할 수 없는 IP 입니다..\n");
			toolkit.beep();
		}
		// 모든 스트림과 소켓 연결을 끊음
		try {
			mySocket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			memo.append(e.toString() + "\n");
		}
	}
}
