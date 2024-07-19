package bg.sofia.uni.fmi.mjt.order.server;

import bg.sofia.uni.fmi.mjt.order.server.repository.MJTOrderRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientRequestHandler implements Runnable {

	private final Socket socket;

	private final MJTOrderRepository orderRepository;

	public ClientRequestHandler(Socket socket, MJTOrderRepository orderRepository) {
		this.socket = socket;
		this.orderRepository = orderRepository;
	}

	public void run() {
		Thread.currentThread().setName("Client Request Handler for " + socket.getRemoteSocketAddress());

		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				Response response = processRequest(inputLine);
				if (response == null) {
					break;
				}
				out.println(response);
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private Response processRequest(String request) {
		String[] arguments = request.split(" ");
		switch (arguments[0]) {
			case "request" -> {
				int argCounter = 1;
				return orderRepository.request(removeEquals(arguments[argCounter++])
						, removeEquals(arguments[argCounter++])
						, removeEquals(arguments[argCounter]));
			}
			case "get" -> {
				switch (arguments[1]) {
					case "all" ->  {
						return orderRepository.getAllOrders();
					} case "all-successful" -> {
						return orderRepository.getAllSuccessfulOrders();
					} case "my-order" -> {
						return orderRepository.getOrderById(Integer.parseInt(
								removeEquals(arguments[2])));
					}
				}
			}
			case "disconnect" -> {
				return null;
			}
			default -> {
				return Response.decline("Unknown command");
			}
		}
		return null;
	}

	private static String removeEquals(String string) {
		int index = string.indexOf('=');
		if (index != -1) {
			return string.substring(index + 1);
		}
		return string;
	}
}
