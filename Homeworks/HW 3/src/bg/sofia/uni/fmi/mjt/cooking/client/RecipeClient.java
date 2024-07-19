package bg.sofia.uni.fmi.mjt.cooking.client;

import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParams;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeClientResponse;

public interface RecipeClient {

	RecipeClientResponse getRecipes(RecipeSearchParams recepieSearchParams);

	RecipeClientResponse getRecipes(String nextPageToken);

}
