package bg.sofia.uni.fmi.mjt.cooking.models;

import java.math.BigDecimal;
import java.util.List;

public record Recipe(
	String label,
	List<String> dietLabels,
	List<String> healthLabels,
	BigDecimal totalWeight,
	List<String> cuisineType,
	List<String> mealType,
	List<String> dishType,
	List<String> ingredientLines
) {

}
