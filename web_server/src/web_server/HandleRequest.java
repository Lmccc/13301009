package web_server;

import java.net.*;
import java.util.*;
import java.io.*;
class HandleRequest extends Thread {
	// socket��������accept���׽���
	private final Socket sock;
	private final Hashtable knownRequests;
	private final String CRLF = "\r\n";
	public HandleRequest(Socket sock, Hashtable knownRequests) {
		this.sock = sock;
		this.knownRequests = knownRequests;
		//��ʼ�̣߳�Ϊ���Կͻ��˵��������
		start();
	}
	// �̳���Thread���run()����
	public void run() {
		System.out.println("Handling client request,...");
		BufferedReader iStream = null;
		DataOutputStream oStream = null;
		String clientRequest = "";
		boolean first = true;
		try {
			//��socket�л�ȡ���롢�����
			iStream = new BufferedReader(new 
             InputStreamReader(sock.getInputStream()));
			oStream = new DataOutputStream(sock.getOutputStream());
			//�ӿͻ��˶�ȡ������Ϣ
			clientRequest = iStream.readLine();
			String headerLine;
	  	while (true /*!sock.isClosed() JDK 1.4 only*/) {
				//��ȡHTTP������ײ���Ϣ
				headerLine = iStream.readLine();
				System.out.println("Receiving header '" +
						headerLine + "'");
				if (headerLine == null || headerLine.trim()
						.length() < 1) {
					break;
				}
			}
			System.out.println("Request from client"+getSocketInfo());
			if (clientRequest == null) {
				String info = "Empty request ignored " + 
				getSocketInfo();
				errorResponse(oStream, "HTTP/1.1 400 Bad Request",
						null, true, info);
				System.out.println(info);
				return;
			}
			first = false;
			System.out.println("Handling client request '" +
					clientRequest+ "' ...");
			StringTokenizer toks = new StringTokenizer(clientRequest);
			//һ�������ַ�������������ɣ����硰GET /Hello HTTP/1.0��
			if (toks.countTokens() != 3) {
				String info = "Wrong syntax in client request: '"+
              clientRequest	+ "', closing "+getSocketInfo()+ 
              " connection.";
				errorResponse(oStream, "HTTP/1.1 400 Bad Request",
						null, true,info);
				System.out.println(info);
				return;
			}
			String method = toks.nextToken(); // "GET"
			String resource = toks.nextToken(); // "/Hello"
			String version = toks.nextToken(); // "HTTP/1.0"
			//��Ϊ���׵�HTTP��������ֻ����GET��HEAD
			if (!method.equalsIgnoreCase("GET")
              && !method.equalsIgnoreCase("HEAD")) {
				String info =	"Invalid method in client "	+ 
				getSocketInfo()+ " request: '"+ clientRequest+ "'";
				errorResponse(oStream,"HTTP/1.1 501 Method Not Implemented",
					"Allow: GET",	true, info);
				System.out.println(info);
				return;
			}
			//��Ӧ�ַ���
			String responseStr = reverse(resource);
			
			//�������ɹ���״̬
			errorResponse(oStream, "HTTP/1.1 200 OK", null, false, null);
			//�����ײ���Content-Length
			String length = "Content-Length: " + responseStr.length();
			oStream.write((length + CRLF).getBytes());
			//����Content-Type�ײ���
			oStream.write(("Content-Type: text/plain; charset=iso-8859-1"
             + CRLF).getBytes());
			if (!method.equalsIgnoreCase("HEAD")) {
				oStream.write(CRLF.getBytes());
				oStream.write(responseStr.getBytes());
			}
			oStream.flush();
		//�����쳣
		} catch (IOException e) {
			if (clientRequest == null && first) {
				System.out.println("Ignoring connect/disconnect attempt");
			} else {
				System.out.println(	"Problems with sending response for '"
					+ clientRequest	+ "' to client "+ getSocketInfo()
					+ ": " + e.toString());
			}
		} 
		finally {
			try {
				if (iStream != null)
					iStream.close();
			} catch (IOException e) {}
			try {
				if (oStream != null)
					oStream.close();
			} catch (IOException e) {}
			try {
				sock.close();
			} catch (IOException e) {}
		}
	}

	/*���ô˷�����oStreamд����Ӧ��Ϣ������bodyָʾ�Ƿ�����Ӧ��Ϣ�м������
            ��������ʾ�ĳ��ı�ҳ��������ʾ��Ϊ����*/
	private void errorResponse(DataOutputStream oStream, String code,
	          String extra, boolean body, String info)
		throws IOException {
		oStream.write((code + CRLF).getBytes());
		oStream.write(("Server: MiniHttpServer/" + CRLF).getBytes());
		if (extra != null)
			oStream.write((extra + CRLF).getBytes());
		oStream.write(("Connection: close" + CRLF).getBytes());
		if (body) {
			//д����������������ʾ�ĳ��ı�������Ϣ
			oStream.write((CRLF	+ "<html><head><title>"	+ code
					+ "</title></head><body>"	+ "<h2>MiniHTTP server "		+ "</h2>"
					+ "<p>"	+ code + "</p>"	+ "<p>"	+ info	+ "</p>"
	             + "</body></html>")	.getBytes());
		}
	}
	//���ع���Socket����Ϣ
	private String getSocketInfo() {
		StringBuffer sb = new StringBuffer(196);
		if (sock == null)
			return "";
		sb.append(sock.getInetAddress().getHostAddress());
		sb.append(":").append(sock.getPort());
		sb.append(" -> ");
		sb.append(sock.getLocalAddress().getHostAddress());
		sb.append(":").append(sock.getLocalPort());
		return sb.toString();
	}
	
	public static String reverse(String s) {
		  return new StringBuffer(s).reverse().toString();
		 }
}
