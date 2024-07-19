package bg.sofia.uni.fmi.mjt.cooking.models;

public enum MealType {
	BREAKFAST,
	DINNER,
	LUNCH,
	SNACK,
	TEATIME;

	static MealType fromString(String mealType) {
		return switch (mealType) {
			case "Breakfast" -> BREAKFAST;
			case "Dinner" -> DINNER;
			case "Lunch" -> LUNCH;
			case "Snack" -> SNACK;
			case "Teatime" -> TEATIME;
			default -> throw new IllegalArgumentException("Invalid meal type: " + mealType);
		};
	}

}
