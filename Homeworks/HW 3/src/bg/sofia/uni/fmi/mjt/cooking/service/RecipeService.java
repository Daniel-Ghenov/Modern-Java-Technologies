package bg.sofia.uni.fmi.mjt.cooking.service;

import bg.sofia.uni.fmi.mjt.cooking.client.EdamamClient;
import bg.sofia.uni.fmi.mjt.cooking.client.RecipeClient;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParams;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParamsBuilder;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeClientResponse;
import bg.sofia.uni.fmi.mjt.cooking.models.Recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeService {

	private final RecipeClient recipeClient;

	private final Map<String, Recipe> recipesByLabel;

	public RecipeService() {
		this(new EdamamClient());
	}

	public RecipeService(RecipeClient recipeClient) {
		this(recipeClient, new ConcurrentHashMap<>());
	}

	public RecipeService(RecipeClient recipeClient, Map<String, Recipe> recipes) {
		this.recipeClient = recipeClient;
		this.recipesByLabel = recipes;
	}

	public List<Recipe> getRecipes(List<String> keywords,
								   List<String> mealType,
								   List<String> healthRequest,
								   Integer recipeCount) {
		if (recipeCount == null || recipeCount < 0) {
			throw new IllegalArgumentException("Recipe count must be positive");
		}

		RecipeSearchParams params = RecipeSearchParamsBuilder.withKeywords(keywords)
										 .withMealType(mealType)
										 .withHealthRequest(healthRequest)
										 .build();

		List<Recipe> localRecipes = getLocalRecipes(params, recipeCount);
		if (localRecipes.size() == recipeCount) {
			return localRecipes;
		}
		return getRecipesFromClient(keywords, mealType, healthRequest, recipeCount);
	}

	public List<Recipe> getRecipesFromClient(List<String> keywords,
											 List<String> mealType,
											 List<String> healthRequest,
											 Integer recipeCount) {

		RecipeSearchParams params = RecipeSearchParamsBuilder.withKeywords(keywords)
									 .withMealType(mealType)
									 .withHealthRequest(healthRequest)
									 .build();

		RecipeClientResponse response = recipeClient.getRecipes(params);
		List<Recipe> recipes = new ArrayList<>(response.recipes());
		String nextPageToken = response.nextPageToken();
		while (recipes.size() < recipeCount) {
			if (nextPageToken == null) {
				break;
			}
			response = recipeClient.getRecipes(nextPageToken);
			recipes.addAll(response.recipes());
			nextPageToken = response.nextPageToken();
		}
		saveRecipes(recipes);
		return recipes;
	}

	public List<Recipe> getLocalRecipes(RecipeSearchParams params, Integer recipeCount) {
		return recipesByLabel.values().stream()
				.filter(recipe -> matchesParams(recipe, params))
				.limit(recipeCount)
				.toList();
	}

	private void saveRecipes(List<Recipe> recipes) {
		for (Recipe recipe : recipes) {
			this.recipesByLabel.put(recipe.label(), recipe);
		}
	}
	
	private boolean matchesParams(Recipe recipe, RecipeSearchParams params) {
		return matchesKeywords(recipe, params.keywords()) &&
			   new HashSet<>(recipe.mealType()).containsAll(params.mealType()) &&
			   new HashSet<>(recipe.healthLabels()).containsAll(params.healthRequest());
	}

	private boolean matchesKeywords(Recipe recipe, List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return false;
		}
		return keywords.stream()
				.allMatch(str -> recipe.label().contains(str));
	}

}
