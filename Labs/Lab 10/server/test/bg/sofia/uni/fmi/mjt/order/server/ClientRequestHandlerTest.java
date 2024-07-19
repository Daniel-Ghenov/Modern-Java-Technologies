package bg.sofia.uni.fmi.mjt.order.server;

import bg.sofia.uni.fmi.mjt.order.server.repository.MJTOrderRepository;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientRequestHandlerTest {

	@Test
	void testDisconnect() throws IOException
	{
		Socket socket = mockSocket("disconnect");
		MJTOrderRepository repository = mock(MJTOrderRepository.class);
		ClientRequestHandler handler = new ClientRequestHandler(socket, repository);

		handler.run();
		assertEquals("", socket.getOutputStream().toString().trim());
	}

	@Test
	void testRequest() throws IOException
	{
		String REQUEST_ONE = """
			request size=L color=BLACK shipTo=EUROPE
			disconnect
			""";

		Socket socket = mockSocket(REQUEST_ONE);
		MJTOrderRepository repository = mock(MJTOrderRepository.class);
		when(repository.request("L", "BLACK", "EUROPE")).thenReturn(Response.create(1));

		ClientRequestHandler handler = new ClientRequestHandler(socket, repository);

		handler.run();
		assertEquals(Response.create(1).toString(), socket.getOutputStream().toString().trim());
		verify(repository).request("L", "BLACK", "EUROPE");
	}


	@Test
	void testGetOrderById() throws IOException
	{
		String REQUEST_ONE = """
			get my-order id=1
			disconnect
			""";

		Socket socket = mockSocket(REQUEST_ONE);
		MJTOrderRepository repository = mock(MJTOrderRepository.class);
		when(repository.getOrderById(1)).thenReturn(Response.ok(null));

		ClientRequestHandler handler = new ClientRequestHandler(socket, repository);

		handler.run();
		assertEquals(Response.ok(null).toString(), socket.getOutputStream().toString().trim());
		verify(repository).getOrderById(1);
	}

	@Test
	void testGetAllOrders () throws IOException
	{
		String REQUEST_ONE = """
			get all
			disconnect
			""";

		Socket socket = mockSocket(REQUEST_ONE);
		MJTOrderRepository repository = mock(MJTOrderRepository.class);
		when(repository.getAllOrders()).thenReturn(Response.ok(null));

		ClientRequestHandler handler = new ClientRequestHandler(socket, repository);

		handler.run();
		assertEquals(Response.ok(null).toString(), socket.getOutputStream().toString().trim());
		verify(repository).getAllOrders();
	}

	@Test
	void testGetAllSuccessfulOrders () throws IOException
	{
		String REQUEST_ONE = """
			get all-successful
			disconnect
			""";

		Socket socket = mockSocket(REQUEST_ONE);
		MJTOrderRepository repository = mock(MJTOrderRepository.class);
		when(repository.getAllSuccessfulOrders()).thenReturn(Response.ok(null));

		ClientRequestHandler handler = new ClientRequestHandler(socket, repository);

		handler.run();
		assertEquals(Response.ok(null).toString(), socket.getOutputStream().toString().trim());
		verify(repository).getAllSuccessfulOrders();
	}

	@Test
	void testUnknownCommand() throws IOException
	{
		String REQUEST_ONE = """
			unknown-command
			disconnect
			""";

		Socket socket = mockSocket(REQUEST_ONE);
		MJTOrderRepository repository = mock(MJTOrderRepository.class);

		ClientRequestHandler handler = new ClientRequestHandler(socket, repository);

		handler.run();
		assertEquals(Response.decline("Unknown command").toString(), socket.getOutputStream().toString().trim());
	}


	private static Socket mockSocket(String request) throws IOException
	{
		Socket socket = mock(Socket.class);
		InputStream is = new ByteArrayInputStream(request.getBytes());
		OutputStream os = new ByteArrayOutputStream();
		when(socket.getOutputStream()).thenReturn(os);
		when(socket.getInputStream()).thenReturn(is);
		return socket;
	}

}