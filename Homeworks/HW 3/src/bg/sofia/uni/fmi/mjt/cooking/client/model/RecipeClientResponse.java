package bg.sofia.uni.fmi.mjt.cooking.client.model;

import bg.sofia.uni.fmi.mjt.cooking.models.Recipe;

import java.util.List;

public record RecipeClientResponse(
		List<Recipe> recipes,
		String nextPageToken
) {

}
