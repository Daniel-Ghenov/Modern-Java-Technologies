package bg.sofia.uni.fmi.mjt.space.mission;

public record Detail(String rocketName, String payload) {

	public static Detail fromString(String str) {
		String[] parts = str.split("\\|");

		String rocketName = parts[0].trim();
		String payload = parts[1].trim();

		return new Detail(rocketName, payload);
	}

}
