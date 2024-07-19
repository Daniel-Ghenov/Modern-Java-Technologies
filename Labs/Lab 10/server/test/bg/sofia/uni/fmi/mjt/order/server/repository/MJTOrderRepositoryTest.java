package bg.sofia.uni.fmi.mjt.order.server.repository;

import bg.sofia.uni.fmi.mjt.order.server.Response;
import bg.sofia.uni.fmi.mjt.order.server.destination.Destination;
import bg.sofia.uni.fmi.mjt.order.server.order.Order;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.Color;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.Size;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.TShirt;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MJTOrderRepositoryTest
{

	private final static MJTOrderRepository repository = new MJTOrderRepository();

	@Test
	void testRequestWithInvalidSize()
	{
		Response response = repository.request("NotKnownSize", Color.RED.toString(), Destination.EUROPE.toString());
		assertAreEqual(Response.decline("invalid=size"), response);
	}

	@Test
	void testRequestWithInvalidColor()
	{
		Response response = repository.request(Size.L.toString(), "NotKnownColor", Destination.EUROPE.toString());
		assertAreEqual(Response.decline("invalid=color"), response);
	}

	@Test
	void testRequestWithInvalidDestination()
	{
		Response response = repository.request(Size.L.toString(), Color.RED.toString(), "NotKnownDestination");
		assertAreEqual(Response.decline("invalid=destination"), response);
	}

	@Test
	void testRequestAllInvalid()
	{
		Response response = repository.request("NotKnownSize", "NotKnownColor", "NotKnownDestination");
		assertAreEqual(Response.decline("invalid=size,color,destination"), response);
	}

	@Test
	void testRequestAllNullSizeAndColor()
	{
		assertThrowsExactly( IllegalArgumentException.class ,() -> repository.request(null, Color.RED.toString(), Destination.EUROPE.toString()));
	}

	@Test
	void testRequestAllValid()
	{
		MJTOrderRepository repository = new MJTOrderRepository();
		Response response = repository.request(Size.L.toString(), Color.RED.toString(), Destination.EUROPE.toString());
		assertAreEqual(Response.create(1), response);
	}

	@Test
	void getOrderByIdWhenThereIsOrder()
	{
		Order order = new Order(1, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		MJTOrderRepository repository = new MJTOrderRepository(Map.of(1, order));
		Response response = repository.getOrderById(1);
		assertAreEqual(Response.ok(List.of(order)), response);
	}

	@Test
	void getOrderByIdWhenThereIsNoOrder()
	{
		MJTOrderRepository repository = new MJTOrderRepository();
		Response response = repository.getOrderById(1);
		assertAreEqual(Response.notFound(1), response);
	}

	@Test
	void testGetAllOrders()
	{
		Order order = new Order(1, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		Order order2 = new Order(2, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		Order order3 = new Order(3, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		MJTOrderRepository repository = new MJTOrderRepository(Map.of(1, order, 2, order2, 3, order3));
		Response response = repository.getAllOrders();
		assertAreEqual(Response.ok(List.of(order, order2, order3)), response);

	}

	@Test
	void testGetAllOrdersWhenThereAreNoOrders()
	{
		MJTOrderRepository repository = new MJTOrderRepository();
		Response response = repository.getAllOrders();
		assertAreEqual(Response.ok(List.of()), response);
	}

	@Test
	void testGetAllSuccessfulOrdersWhenAllSuccessful()
	{
		Order order = new Order(1, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		Order order2 = new Order(2, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		Order order3 = new Order(3, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		MJTOrderRepository repository = new MJTOrderRepository(Map.of(1, order, 2, order2, 3, order3));
		Response response = repository.getAllSuccessfulOrders();
		assertAreEqual(Response.ok(List.of(order, order2, order3)), response);

	}

	@Test
	void testGetAllSuccessfulOrdersWhenThereAreUnsuccessfulOrders()
	{
		Order order = new Order(0, new TShirt(Size.L, Color.RED), Destination.UNKNOWN);
		Order order2 = new Order(1, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		Order order3 = new Order(2, new TShirt(Size.L, Color.RED), Destination.EUROPE);
		MJTOrderRepository repository = new MJTOrderRepository(Map.of(1, order, 2, order2, 3, order3));
		Response response = repository.getAllSuccessfulOrders();
		assertAreEqual(Response.ok(List.of(order2, order3)), response);
	}

	void assertAreEqual(Response expected, Response actual)
	{
		assertEquals(expected.status(), actual.status());
		assertEquals(expected.additionalInfo(), actual.additionalInfo());
		assertOrdersEqual(expected.orders(), actual.orders());
	}

	void assertOrdersEqual(Collection<Order> expected, Collection<Order> actual)
	{
		if(expected == null)
		{
			assertNull(actual);
			return;
		}
		assertEquals(expected.size(), actual.size());
		assertTrue(expected.containsAll(actual));
		assertTrue(actual.containsAll(expected));
	}

}