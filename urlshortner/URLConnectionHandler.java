package urlshortner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdbc.DBHandler;

class URLConnectionHandler extends Thread {

	final boolean VERBOSE = true;

	Socket clientSocket;
	URLDataAccessObject urlDAO;
	URLResponseInit resFiles;
	private static String contentMimeType = "text/html";

	public URLConnectionHandler(Socket clientSocket, DBHandler db, URLResponseInit resFiles) {
		this.clientSocket = clientSocket;
		this.urlDAO = new URLDataAccessObject(db);
		this.resFiles = resFiles;
	}

	@Override
	public void run() {
		Socket connect = this.clientSocket;
		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String input = in.readLine();

			if (VERBOSE) {
				System.out.println("first line: " + input);
			}
			Pattern pIsPut = Pattern.compile("^PUT.*$");
			Matcher mIsPut = pIsPut.matcher(input);
			Pattern pputGoodFormat = Pattern.compile("^PUT\\s+/\\?short=(\\S+)&long=(\\S+)\\s+(\\S+)$");
			Matcher mputGoodFormat = pputGoodFormat.matcher(input);
			Pattern pget = Pattern.compile("^(\\S+)\\s+/(\\S+)\\s+(\\S+)$");
			Matcher mget = pget.matcher(input);

			// is a put request
			if (mIsPut.matches()) {
				if (mputGoodFormat.matches()) {
					putHandler(mputGoodFormat, connect);
				} else {
					badRequestHandler(connect);
				}
			// otherwise treat as a get request
			} else if(mget.matches()) {
				getHandler(mget, connect);
			} else {
				badRequestHandler(connect);
			}
		} catch (Exception e) {
			System.err.println("Server error");
		} finally {
			try {
				in.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}

			if (VERBOSE) {
				System.out.println("Connection closed.\n");
			}
		}

	}

	private void putHandler(Matcher mput, Socket connect) {
		String shortResource = mput.group(1);
		String longResource = mput.group(2);
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			this.urlDAO.save(shortResource, longResource);

			out.println("HTTP/1.1 200 OK");
			out.println("Server: Java HTTP Server/Shortner : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + contentMimeType);
			out.println("Content-length: " + resFiles.getRedirectRecordedPageLength());
			out.println();
			out.flush();

			dataOut.write(resFiles.getRedirectRecordedPage(), 0, resFiles.getRedirectRecordedPageLength());
			dataOut.flush();
		} catch (Exception e) {
			System.err.println("Server error");
		} finally {
			try {
				out.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}
			if (VERBOSE) {
				System.out.println("Connection closed.\n");
			}
		}

	}

	private void getHandler(Matcher mget, Socket connect) {
		System.out.println("Invoked get request handler");
		String shortResource = mget.group(2);
		String longResource = this.urlDAO.find(shortResource);

		if (longResource == null) {
			notFoundHandler(connect);
			return;
		}

		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {

			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			// out.println("HTTP/1.1 301 Moved Permanently");
			out.println("HTTP/1.1 307 Temporary Redirect");
			out.println("Location: " + longResource);
			out.println("Server: Java HTTP Server/Shortner : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + contentMimeType);
			out.println("Content-length: " + resFiles.getRedirectPageLength());
			out.println();
			out.flush();

			dataOut.write(resFiles.getRedirectPage(), 0, resFiles.getRedirectPageLength());
			dataOut.flush();
		} catch (Exception e) {
			System.err.println("Server error");
			out.close();
		}
	}

	private void notFoundHandler(Socket connect) {
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		try {
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			out.println("HTTP/1.1 404 File Not Found");
			out.println("Server: Java HTTP Server/Shortner : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + contentMimeType);
			out.println("Content-length: " + resFiles.getNotFoundPageLength());
			out.println();
			out.flush();

			dataOut.write(resFiles.getNotFoundPage(), 0, resFiles.getNotFoundPageLength());
			dataOut.flush();
		} catch (Exception e) {
			System.err.println("Server error");
			out.close();
		}
	}

	private void badRequestHandler(Socket connect) {
		System.out.println("Invoked bad request handler");
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		try {
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			out.println("HTTP/1.1 400 Bad Request");
			out.println("Server: Java HTTP Server/Shortner : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + contentMimeType);
			out.println("Content-length: " + resFiles.getBadRequestPageLength());
			out.println();
			out.flush();

			dataOut.write(resFiles.getBadRequestPage(), 0, resFiles.getBadRequestPageLength());
			dataOut.flush();
		} catch (Exception e) {
			System.err.println("Server error");
			out.close();
		}
	}

	// private String longToShort(String longURL) {
	// CRC32 crc = new CRC32();
	// crc.update(longURL.getBytes());
	// String shortURL = Long.toHexString(crc.getValue());
	// return shortURL;
	// }

}
