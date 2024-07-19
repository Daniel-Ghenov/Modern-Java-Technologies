package bg.sofia.uni.fmi.mjt.order.server.repository;

import bg.sofia.uni.fmi.mjt.order.server.Response;
import bg.sofia.uni.fmi.mjt.order.server.destination.Destination;
import bg.sofia.uni.fmi.mjt.order.server.order.Order;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.Color;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.Size;
import bg.sofia.uni.fmi.mjt.order.server.tshirt.TShirt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MJTOrderRepository implements OrderRepository {
	private static final int INVALID_ORDER_ID = -1;

	private final Map<Integer, Order> orders;

	private final AtomicInteger orderId = new AtomicInteger(1);

	private final AtomicInteger unsuccessfulOrderId = new AtomicInteger(INVALID_ORDER_ID);

	public MJTOrderRepository(Map<Integer, Order> orders) {
		this.orders = orders;
	}

	public MJTOrderRepository() {
		this(new ConcurrentHashMap<>());
	}

	@Override
	public Response request(String size, String color, String destination) {
		Response response = validateRequest(size, color, destination);
		if (response != null) {
			return response;
		}
		return createOrder(orderId.getAndIncrement() , size, color, destination);
	}

	private Response createOrder(int orderId , String size, String color, String destination) {
		TShirt tShirt = new TShirt(Size.valueOf(size), Color.valueOf(color));
		Order order = new Order(orderId, tShirt, Destination.valueOf(destination));
		orders.put(orderId, order);
		return Response.create(orderId);
	}

	private void createUnsuccessfulOrder(Size size, Color color, Destination destination) {
		TShirt tShirt = new TShirt(size, color);
		Order order = new Order(INVALID_ORDER_ID, tShirt, destination);
		orders.put(unsuccessfulOrderId.getAndDecrement(), order);
	}

	private Response validateRequest(String size, String color, String destination) {
		List<String> invalidArguments = new ArrayList<>();

		Size sizeEnum = validateEnum(size, Size.class);
		if (sizeEnum == null) {
			invalidArguments.add("size");
			sizeEnum = Size.UNKNOWN;
		}

		Color colorEnum = validateEnum(color, Color.class);
		if (colorEnum == null) {
			invalidArguments.add("color");
			colorEnum = Color.UNKNOWN;
		}

		Destination destinationEnum = validateEnum(destination, Destination.class);
		if (destinationEnum == null) {
			invalidArguments.add("destination");
			destinationEnum = Destination.UNKNOWN;
		}

		if (!invalidArguments.isEmpty()) {
			createUnsuccessfulOrder(sizeEnum, colorEnum, destinationEnum);
			return Response.decline("invalid=" + String.join(",", invalidArguments));
		}
		return null;
	}

	@Override public Response getOrderById(int id) {
		Order order = orders.get(id);
		if (order == null) {
			return Response.notFound(id);
		}
		return Response.ok(List.of(order));
	}

	@Override
	public Response getAllOrders() {
		return Response.ok(orders.values());
	}

	@Override
	public Response getAllSuccessfulOrders() {
		List<Order> successfulOrders = orders.values()
				.stream()
				.filter(this::isSuccessful)
				.toList();

		return Response.ok(successfulOrders);
	}

	private boolean isSuccessful(Order order) {
		return order.id() != INVALID_ORDER_ID &&
				order.tShirt().size() != Size.UNKNOWN &&
				order.tShirt().color() != Color.UNKNOWN &&
				order.destination() != Destination.UNKNOWN;
	}

	private <T extends Enum<?>> T validateEnum(String enumName, Class<T> enumClass) {
		if (enumName == null) {
			throw new IllegalArgumentException(enumClass.getName() +  "cannot be null");
		}
		return Arrays.stream(enumClass.getEnumConstants())
				.filter(enumConstant -> enumConstant.name().equals(enumName))
				.findFirst()
				.orElse(null);
	}
}
