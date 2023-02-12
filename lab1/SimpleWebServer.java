/***********************************************************************

   SimpleWebServer.java


   This toy web server is used to illustrate security vulnerabilities.
   This web server only supports extremely simple HTTP GET requests.

   This file is also available at http://www.learnsecurity.com/ntk
 
***********************************************************************/

package com.webserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleWebServer {

	/* Run the HTTP server on this TCP port. */
	private static final int PORT = 8080;
	private static final String USERNAME = "BOB";
	private static final String PASSWORD = "mypass";

	/*
	 * The socket used to process incoming connections from web clients
	 */
	private static ServerSocket dServerSocket;

	public SimpleWebServer() throws Exception {
		dServerSocket = new ServerSocket(PORT);
	}

	public void run() throws Exception {
		while (true) {
			/* wait for a connection from a client */
			Socket s = dServerSocket.accept();

			/* then process the client's request */
			processRequest(s);
		}
	}

	/*
	 * Reads the HTTP request from the client, and responds with the file the user
	 * requested or a HTTP error code.
	 */
	public void processRequest(Socket s) throws Exception {
		/* used to read data from the client */
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

		/* used to write data to the client */
		OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());

		/* read the HTTP request from the client */
		String header = br.readLine();
		String request = br.readLine();
		String authorization = br.readLine();
//		System.out.println(header);
//		int i = 0;
//		while (!request.equals("null")) {
//			System.out.println(i);
//			System.out.println("a" + request + "a");
//			request = br.readLine();
//			System.out.println("hi");
//			if (request == "\n") System.out.println("new lin");
//			if (request == null) System.out.println("nullishere");
//			i+=1;
//		}
//		br.close();
//		System.out.println("im out");
		String command = null;
		String pathname = null;

		/* parse the HTTP request */
		StringTokenizer st = new StringTokenizer(header, " ");
		StringTokenizer st2 = new StringTokenizer(authorization, " ");

		command = st.nextToken();
		pathname = st.nextToken();

		String auth = st2.nextToken();
		String basic = st2.nextToken();
		String base64 = st2.nextToken();
		
		System.out.println(auth);
		if (!auth.contains("Authorization:")) {
			osw.write ("HTTP/1.0 401 Unauthorized");
			osw.write ("WWW-Authenticate: Basic realm=BasicAuthWebServer");
			System.out.println("Unauthorized request");
			return;
		}
		byte[] decodedBytes = Base64.getDecoder().decode(base64);
		String decodedString = new String(decodedBytes);
		System.out.println(decodedString);
		
		StringTokenizer st3 = new StringTokenizer(decodedString, ":");
		String username = st3.nextToken();
		String pass = st3.nextToken();

		if (!(username.equals(USERNAME) && pass.equals(PASSWORD))) {
			osw.write ("HTTP/1.0 401 Unathenticated");
			System.out.println("Unathenticated request");
			return;
		}

		
		
		if (command.equals("PUT")) {
			/*
			 * if the request is a GET try to respond with the file the user is requesting
			 */
			storeFile(br, osw, pathname);
			System.out.println("out of function");
			logEntry(pathname, "TEST");
		}
		if (command.equals("GET")) {
			/*
			 * if the request is a GET try to respond with the file the user is requesting
			 */
			serveFile(osw, pathname);
			System.out.flush();
		} else {
			/*
			 * if the request is a NOT a GET, return an error saying this server does not
			 * implement the requested command
			 */
			osw.write("HTTP/1.0 501 Not Implemented\n\n");
		}

		/* close the connection to the client */
		osw.close();
	}
	public void storeFile(BufferedReader br, OutputStreamWriter osw, String pathname) throws Exception {
		if (pathname.charAt(0) == '/')
			pathname = '.' + pathname;
//			pathname = pathname.substring(1);
		System.out.println(pathname);
		FileWriter fw = null;
		try {
			fw = new FileWriter(pathname);
			String s = br.readLine();
			while (!s.equals("null")) {
				s = br.readLine();
				fw.write(s + "\n");
			}
			fw.close();
			osw.write("HTTP/1.0 201 Created");
		} catch (Exception e) {
			System.out.println("exception");
			osw.write("HTTP/1.0 500 Internal Server Error");
		}
	}
	public void serveFile(OutputStreamWriter osw, String pathname) throws Exception {
		if (pathname.charAt(0) == '/')
			pathname=pathname.substring(1);
		File file = new File(pathname);
		System.out.println(file.length());
		if((file.length() / (1024*1024*1024)) > 1) {
			System.out.println("File too large");
			osw.write("HTTP/1.0 404 File too lard\n\n");
			return;
		}
			
		System.out.flush();
		FileReader fr = null;
		int c = -1;
		StringBuffer sb = new StringBuffer();

		/*
		 * if there was no filename specified by the client, serve the "index.html" file
		 */
		if (pathname.equals(""))
			pathname = "index.html";

		/* try to open file specified by pathname */
		try {
			FileWriter fw = new FileWriter("./error_log.txt", true);
			fw.write(getTimestamp() + " Attempted to open large file");
			fw.close();
			fr = new FileReader(pathname);
			c = fr.read();
		} catch (Exception e) {
			/*
			 * j
			 * if the file is not found,return the appropriate HTTP response code
			 */
			System.out.println("exception thrown");
			System.out.flush();
			osw.write("HTTP/1.0 404 Not Found\n\n");
			return;
		}

		/*
		 * if the requested file can be successfully opened and read, then return an OK
		 * response code and send the contents of the file
		 */
		osw.write("HTTP/1.0 200 OK\n\n");
		while (c != -1) {
			sb.append((char) c);
			c = fr.read();
		}
		osw.write(sb.toString());
	}



	public void logEntry(String pathname, String record) throws IOException {
		if (pathname.charAt(0) == '/')
			pathname = '.' + pathname;
		FileWriter fw = new FileWriter(pathname, true);
		fw.write(getTimestamp() + " " + record);
		fw.close();
	}

	public String getTimestamp() {
		return (new Date()).toString();
	}


	/*
	 * This method is called when the program is run from the command line.
	 */
	public static void main(String argv[]) throws Exception {

		/* Create a SimpleWebServer object, and run it */
		SimpleWebServer sws = new SimpleWebServer();
		sws.run();
	}
}
