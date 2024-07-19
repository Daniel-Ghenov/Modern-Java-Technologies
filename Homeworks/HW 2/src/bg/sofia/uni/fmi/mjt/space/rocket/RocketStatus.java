package bg.sofia.uni.fmi.mjt.space.rocket;

public enum RocketStatus {
	STATUS_RETIRED("StatusRetired"),
	STATUS_ACTIVE("StatusActive");

	private final String value;

	RocketStatus(String value) {
		this.value = value;
	}

	public String toString() {
		return value;
	}

	public static RocketStatus fromString(String text) {
		for (RocketStatus status : RocketStatus.values()) {
			if (status.value.equalsIgnoreCase(text)) {
				return status;
			}
		}
		throw new IllegalArgumentException("No constant with text " + text + " found");
	}
}