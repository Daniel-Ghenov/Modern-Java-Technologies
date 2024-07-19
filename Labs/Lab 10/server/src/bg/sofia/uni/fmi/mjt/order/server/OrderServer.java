package bg.sofia.uni.fmi.mjt.order.server;

import bg.sofia.uni.fmi.mjt.order.server.repository.MJTOrderRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderServer {
	private static final int SERVER_PORT = 8080;
	private final MJTOrderRepository orderRepository;

	public OrderServer(MJTOrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public static void main(String[] args) {
		OrderServer server = new OrderServer(new MJTOrderRepository());
		server.run();
	}

	public void run() {

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

			Thread.currentThread().setName("Echo Server Thread");

			try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

				System.out.println("Server started and listening for connect requests");

				Socket clientSocket;

				while (true) {
					clientSocket = serverSocket.accept();
					ClientRequestHandler clientHandler = new ClientRequestHandler(clientSocket,
												  orderRepository);
					executor.execute(clientHandler);
				}
			} catch (IOException e) {
				throw new RuntimeException("There is a problem with the server socket", e);
			}
		}
	}

}
