package bg.sofia.uni.fmi.mjt.cooking.client.model;

import java.util.List;

public class RecipeSearchParamsBuilder {
	private List<String> keywords;

	private List<String> mealType;

	private List<String> healthRequest;

	private RecipeSearchParamsBuilder() {
	}

	public static RecipeSearchParamsBuilder withKeywords(List<String> keywords) {
		RecipeSearchParamsBuilder builder = new RecipeSearchParamsBuilder();
		builder.keywords = keywords;
		return builder;
	}

	public RecipeSearchParamsBuilder withMealType(List<String> mealType) {
		this.mealType = mealType;
		return this;
	}

	public RecipeSearchParamsBuilder withHealthRequest(List<String> healthRequest) {
		this.healthRequest = healthRequest;
		return this;
	}

	public RecipeSearchParams build() {
		return new RecipeSearchParams(keywords, mealType, healthRequest);
	}

}
