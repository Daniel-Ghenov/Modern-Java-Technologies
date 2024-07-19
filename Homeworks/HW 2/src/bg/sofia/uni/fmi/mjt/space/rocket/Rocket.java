package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {

	private static final int COLUMNS_COUNT = 4;

	public static Rocket fromString(String rocketString) {
		String[] parts = rocketString.split(",");
		int counter = 0;
		String id = parts[counter++];
		String name = parts[counter++];
		Optional<String> wiki = Optional.ofNullable(parts[counter++].isEmpty() ? null : parts[counter - 1]);
		Optional<Double> height;
		if (parts.length == COLUMNS_COUNT) {
			height = Optional.of(Double.valueOf(parts[counter].substring(0, parts[counter].length() - 2)));
		} else {
			height = Optional.empty();
		}
		return new Rocket(id, name, wiki, height);
	}

}
