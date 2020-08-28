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

	int setN = 0; // ���� Ŭ���̾�Ʈ�� 0, 1, 2, 3 ���·� ������ ������.
	Boolean ThreadState = false; // ������ ������ ���� ����
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

		// �ؽ�Ʈ ����� �������̾ƿ��� �߾ӿ� ��ġ
		memo = new TextArea(10, 55);
		add("Center", memo);
	

		// �г� �����Ͽ� �гο� IP �ּ� �Է��� ���� �ؽ�Ʈ�ʵ�� ���� ��ư �߰�
		upPanel = new Panel();

		serverIp = new TextField(12);
		serverIp.setText("192.168.219.164");
		upPanel.add(serverIp);

		connect = new Button("����");
		disconnect = new Button("���� ����");
		connect.addActionListener(this);
		disconnect.addActionListener(this);
		upPanel.add(connect);
		upPanel.add(disconnect);

		// ������ �г��� �������̾ƿ��� ���ʿ� ��ġ
		add("North", upPanel);

		// �г� �����Ͽ� ��ȭ���� ���� �ؽ�Ʈ�ʵ�� �Է��� ���� �ؽ�Ʈ�ʵ� �߰�

		downPanel = new Panel();
		name = new TextField(8);
		name.setText("��ȭ��");
		downPanel.add(name);
		input = new TextField(32);
		input.setEditable(false);
		input.addActionListener(this); // ����ڰ� ����Ű�� ������ �޽����� ���۵ǵ��� �̺�Ʈ ����
		downPanel.add(input);
		add("South", downPanel); // �������̾ƿ��� �Ʒ��ʿ� �г� ��ġ
	}

	// �������� ������ �޼����� �о memo�� append
	public void run() {
		try {
			while (!ThreadState) {
				String msg = in.readLine(); // ������ ���� �޽����� �о����
				if (!msg.equals("") && !msg.equals(null)) {
					if (msg.equals("exit")) { // ���� �޽����� exit��� ���¸� 2�� �ٲٰ� ����
						setN = 2;
						stop();
					}else {
						memo.append(msg + "\n"); // �� ȭ���� memo�� ���� �޽��� ���
					}
				}
			}
		} catch (IOException e) {
			memo.append("���� ����...\n");
		}
	}

	// ���� ������ ����
	public void actionPerformed(ActionEvent e) // connect ��ư�� ���� ���� input �ؽ�Ʈ�ʵ忡
	{ // ���Ͱ� ������ ��� ����
		if (e.getSource() == connect && setN == 0) { // Ŀ�ؼ��� ������ �߻��ϴµ� ���°� 1���� ���� ���¿�����
			try {
				setN = 1;
				mySocket = new Socket(serverIp.getText(), 50000); // ��Ʈ ������ Ŀ�ؼ�

				// ������ ������ �̿��� �������� ����� ��Ʈ���� ����
				out = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
				in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
				saveName = name.getText();
				// ��Ʈ�� ���� �� �ٷ� ���ø��� ��ȭ���� ������
				out.println(name.getText());
				out.flush();
				name.setEditable(false);
				// �ι�°�� ���� Ŭ���̾�Ʈ ������ �� �������� ���� ����� ����
				String msg = in.readLine();
				// ���� �޼����� ipBan�̶�� �������ּҿ� ���� �õ� �޼����� �ְ� stop �ϰ� ���� Ŭ���̾�Ʈ ���¸� 3����
				if (msg.equals("ipBan")) {
					out.println(mySocket.getInetAddress() + " ���� �õ�...");
					out.flush();
					setN = 3;
					stop();
				}
				// ���� �޼����� OverLap�̶�� Ŭ���̾�Ʈ���� �ߺ��ǹǷ� ���¸� �������� 0���� �ٲٰ� stop
				else if (msg.equals("OverLap")) {
					name.setText("��ȭ��");
					memo.append("��ȭ���� �ߺ��˴ϴ�.\n");
					setN = 0;
					stop();
				}
				// ���� �޼����� OK��� �α��� ������. ������ ����
				else if (msg.equals("OK")) {
					out.println("LOGIN|" + mySocket); // �ʱ� ������ ������ �� ���� �޽����� ����
					out.flush();
					input.setEditable(true);
					memo.append("[�����Ͽ����ϴ�]" + "\n"); // �� ȭ���� �ؽ�Ʈ����� memo�� ���Ӹ޽��� ���
					memo.setEditable(false);
					serverIp.setEditable(false);
					// �����带 ���۽�Ŵ
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
		// �� Ŀ�ؼ� ��ư�� �������� ���� ���°� 0 2 3 �̸� �ҿ� �����Ƿ� �������� ���� �޼��� ���
		else if (e.getSource() == disconnect) {
			if (setN == 0 || setN == 2 || setN == 3) {
				toolkit.beep();
				memo.append("����Ǿ� ���� �ʽ��ϴ�..\n");
			}
			// ���°� 1�̶�� ���� ����
			else {
				memo.setEditable(true);
				serverIp.setEditable(true);
				input.setEditable(false);
				stop();
			}
		}
		// input �ؽ�Ʈ�ʵ忡 ���Ͱ� �Էµ� ���
		else if (e.getSource() == input) {
			String data = input.getText(); // input �ؽ����ʵ��� ���� �о
			input.setText("");
			// �̸������� �Ǹ� �ȵǱ� ������ ����ؼ� ��������.
			name.setText(saveName);
			// ���Ŀ� ���� ������ �޽����� ���� <ȫ�浿> msg
			if (data.contains(">") && data.contains("<")) { // <����>
				out.println("WHISPER|" + "[" + name.getText() + "]" + " to " + data);
				out.flush(); // ���ۿ� �ִ� ��� �޽����� ���濡�� ������ ����
			} else {
				out.println("TALK|" + "[" + name.getText() + "]" + " : " + data);
				out.flush(); // ���ۿ� �ִ� ��� �޽����� ���濡�� ������ ����
			}
		}
		// �ߺ� Ŭ������ ���� ������ �ߺ��� �������� ���°� 1�� ��� �̹� �������� ����
		else if (setN == 1) {
			memo.append("�̹� ������ �Դϴ�.. \n");
			toolkit.beep();
			return;
		}
		// ���� ���°� 0 1 2 �� �ƴ� 3�̶�� ������������� ������ ����.
		else {
			memo.append("���� ������ �����ϴ�.. \n");
			toolkit.beep();
			return;
		}
	}

	public void stop() // �����带 �����Ű�� ���� �޽����� ������ �����ϰ� ��� ������ ����
	{
		name.setEditable(true);
		if ((clock != null) && (clock.isAlive())) {
			clock = null; // ������ ����
		}
		// ���� ���¿� �´� ��ȭ�� ������ ������. �Ǵ� ���� ���� �� ������ �� ��Ʈ�� ���� �ݳ�.
		if (setN == 0) {
			out.println("��ȭ�� �ߺ����� ���� Ŀ�ؼ� ����\n");
			out.flush();
			memo.append("����....\n");
			toolkit.beep();
		} else if (setN == 1) {
			out.println("LOGOUT|" + name.getText());
			out.flush();
			setN = 0;
		} else if (setN == 2) {
			memo.append("���忡 ���� ������Ͽ����ϴ�...\n");
			toolkit.beep();
		} else if (setN == 3) {
			memo.append("�ش� IP�� ����� �� ���� IP �Դϴ�..\n");
			toolkit.beep();
		}
		// ��� ��Ʈ���� ���� ������ ����
		try {
			mySocket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			memo.append(e.toString() + "\n");
		}
	}
}
