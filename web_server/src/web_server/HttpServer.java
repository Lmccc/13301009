package web_server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class HttpServer extends Thread { 
	//�̳���Thread����HttpServerʵ�ֵ�Run()������ΪHttp�������������߳�
	// HTTP�������Ķ˿�
	private final int HTTP_PORT;
	// ServerSocket, ������ͨ�����ServerSocket��������Web�ͻ�����������
	private ServerSocket listen = null;
	//����������ָʾ�Ƿ�Ҫ��������
	private boolean running = true;
	//�ͻ�������Ͷ���Ӧ�����Ӧ���б�
	private Hashtable knownRequests = new Hashtable(); 
    //���캯������һ������port��ָʾHttp���������ڵĶ˿�
	public HttpServer(int port) {
		this.HTTP_PORT = port;
		//�������˿ں�Ӧ���Ǵ����������
		if (this.HTTP_PORT <= 0) {
			System.err.println("HttpServer not started, as -Port is " +
             this.HTTP_PORT);
			return;
		}
		/*�˴�����Ϣ�����System.err�У�������Ŀ��Ŀ�ģ���ĳЩ����̨�Ͽ��Կ���
             ��ɫ�����*/
		System.err.println("Creating new HttpServer on Port = " +
            this.HTTP_PORT);
		//�������߳�Ϊ�����߳�
		setDaemon(true);
		//��ʼ�����߳����У��˺���Խ������Կͻ������󲢴���
		start();
	}
 
	public void removeRequest(String urlPath) {
		knownRequests.remove(urlPath.trim());
	}
	//ʵ�ָ���Thread��run����
	public void run() {
		try {
			//����ServerSocket��ʵ����ServerSocket�󶨵�HTTP_PORT�˿�
			this.listen = new ServerSocket(HTTP_PORT);
			//���running��־Ϊtrue����ô�������У������������߳��˳����������
			while (running) {
				//�������Կͻ��˵�TCP����
				Socket accept = this.listen.accept();
				System.out.println("New incoming request on Port=" +
               HTTP_PORT + " ...");
				if (!running) {
					System.out.println("Closing http server Port=" +
               HTTP_PORT + ".");
					break;
				}
			/*����һ�������̣߳�Ϊ������ӷ���Ϊ��ͬʱΪ���Web�ͻ����񣬲�������
              ���Ϊ�ͻ����񣬴�����HandlerRequest��ʵ����HandlerRequestҲ��
            Thread������࣬HandlerRequsetΪaccept������Ŀͻ����뱾�ص�TCP
              ���ӷ��񣬵�HandlerRequest��ͻ��˷�����Ӧ�����������߳�Ҳ������*/
				HandleRequest hh = new HandleRequest
				(accept, knownRequests);
			}
		// �����ڷ�������з������쳣
		} catch (java.net.BindException e) {
			System.out.println(	"HTTP server problem, Port "	+
					listen.getInetAddress().toString()	+
					  ":" + HTTP_PORT+" is not available: "	+
					   e.toString());
		} catch (java.net.SocketException e) {
			System.out.println(	"Socket "	+ listen.getInetAddress()
              .toString()+ ":" + HTTP_PORT+ " closed successfully: "
         					+ e.toString());
		} catch (IOException e) {
			System.out.println("HTTP server problem on port : " +
             HTTP_PORT + ": " + e.toString());
		}
		// ��ѭ���������߳̽�Ҫ�˳�������ɨ����
		if (this.listen != null) {
			try {
				//�ر��׽���
				this.listen.close();
			} catch (java.io.IOException e) {
				System.out.println("this.listen.close()" + e.toString());
			}
			this.listen = null;
		}
	}
	// ���Ե����������ֹͣHttpServer�ķ���
	public void shutdown()
	{
		System.out.println("Entering shutdown");
		//���ñ�־��֪ͨ�������̲߳���Ϊ�����ӷ���
		running = false;
		try {
			// �ر��׽���
			if (this.listen != null) {
				this.listen.close();
				this.listen = null;
			}
		} catch (java.io.IOException e) {
			System.out.println("shutdown problem: " + e.toString());
		}
	}
    //����������
	public static void main(String[] args) {
		//����һ��HttpServer��ʵ����ʹ��3333�˿�
		HttpServer server = new HttpServer(3333);
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
