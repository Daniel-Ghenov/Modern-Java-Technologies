package bg.sofia.uni.fmi.mjt.cooking.client.service;

import bg.sofia.uni.fmi.mjt.cooking.client.RecipeClient;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeClientResponse;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParams;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParamsBuilder;
import bg.sofia.uni.fmi.mjt.cooking.models.Recipe;
import bg.sofia.uni.fmi.mjt.cooking.service.RecipeService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecipeServiceTest
{

	@Test
	void testWhenSearchWithNullRecipeCountThenThrowException() {
		RecipeService recipeService = new RecipeService();
		 assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipes(List.of(), List.of(), List.of(), null));
	}

	@Test
	void testWhenSearchWithNegativeRecipeCountThenThrowException() {
		RecipeService recipeService = new RecipeService();
		 assertThrows(IllegalArgumentException.class, () -> recipeService.getRecipes(List.of(), List.of(), List.of(), -1));
	}

	@Test
	void testWhenRecipeLocallyAvailableThenReturnLocalRecepies() {
		RecipeClient recipeClient = mock(RecipeClient.class);
		Recipe recipe = new Recipe("chicken", List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of());
		RecipeService recipeService = new RecipeService(recipeClient, Map.of("chicken", recipe));

		List<Recipe> returned = recipeService.getRecipes(List.of("chicken"), List.of(), List.of(), 1);
		assertIterableEquals(List.of(recipe), returned);
		verify(recipeClient, never()).getRecipes((RecipeSearchParams) any());
	}

	@Test
	void testWhenNoLocalRecipeThenShouldCallClient() {
		Map<String, Recipe> toSaveIn = new HashMap<>();
		RecipeClient recipeClient = mock(RecipeClient.class);
		RecipeService recipeService = new RecipeService(recipeClient, toSaveIn);
		RecipeSearchParams params = RecipeSearchParamsBuilder.withKeywords(List.of("beef"))
															 .withMealType(List.of())
															 .withHealthRequest(List.of())
															 .build();
		Recipe recipe = new Recipe("beef", List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of());
		when(recipeClient.getRecipes(params)).thenReturn(new RecipeClientResponse(List.of(recipe), "a"));


		recipeService.getRecipes(List.of("beef"), List.of(), List.of(), 1);

		verify(recipeClient).getRecipes(params);
		assertIterableEquals(List.of(recipe), toSaveIn.values());
	}

	@Test
	void testWhenShouldCallClientMultipleTimes() {
		Map<String, Recipe> toSaveIn = new HashMap<>();
		RecipeClient recipeClient = mock(RecipeClient.class);
		RecipeService recipeService = new RecipeService(recipeClient, toSaveIn);
		RecipeSearchParams params = RecipeSearchParamsBuilder.withKeywords(List.of("beef"))
															 .withMealType(List.of())
															 .withHealthRequest(List.of())
															 .build();
		Recipe recipe = new Recipe("beef", List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of());
		Recipe recipe2 = new Recipe("chicken", List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of());
		Recipe recipe3 = new Recipe("pork", List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of());

		when(recipeClient.getRecipes(params)).thenReturn(new RecipeClientResponse(List.of(recipe), "a"));
		when(recipeClient.getRecipes("a")).thenReturn(new RecipeClientResponse(List.of(recipe2), "b"));
		when(recipeClient.getRecipes("b")).thenReturn(new RecipeClientResponse(List.of(recipe3), null));

		recipeService.getRecipes(List.of("beef"), List.of(), List.of(), 3);

		verify(recipeClient).getRecipes(params);
		verify(recipeClient).getRecipes("a");
		verify(recipeClient).getRecipes("b");
		assertTrue(List.of(recipe, recipe2, recipe3).containsAll(toSaveIn.values()), "More recipes were saved than expected");
		assertTrue(toSaveIn.values().containsAll(List.of(recipe, recipe2, recipe3)), "Not all recipes were saved");
	}

}
