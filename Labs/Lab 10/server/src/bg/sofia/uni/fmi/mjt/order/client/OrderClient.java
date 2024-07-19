package bg.sofia.uni.fmi.mjt.order.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class OrderClient {

	private static final int SERVER_PORT = 8080;

	public static void main(String[] args) {

		try (Socket socket = new Socket("localhost", SERVER_PORT);
			 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 Scanner scanner = new Scanner(System.in)) {

			Thread.currentThread().setName("TShirt client thread " + socket.getLocalPort());
			while (true) {
				System.out.print("Enter message: ");
				String message = scanner.nextLine();

				if (message.equals("disconnect")) {
					break;
				}

				System.out.println("Sending message <" + message + "> to the server...");

				writer.println(message);

				String reply = reader.readLine();
				System.out.println("The server replied <" + reply + ">");
			}

		} catch (IOException e) {
			throw new RuntimeException("There is a problem with the network communication", e);
		}
	}

}
