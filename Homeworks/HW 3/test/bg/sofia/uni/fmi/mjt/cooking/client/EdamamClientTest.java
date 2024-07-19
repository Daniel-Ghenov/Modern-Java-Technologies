package bg.sofia.uni.fmi.mjt.cooking.client;

import bg.sofia.uni.fmi.mjt.cooking.client.model.ClientException;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeClientResponse;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParams;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParamsBuilder;
import bg.sofia.uni.fmi.mjt.cooking.client.util.HttpResponseStub;
import bg.sofia.uni.fmi.mjt.cooking.models.Recipe;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EdamamClientTest
{
	String RESPONSE = """
			{"_links":{"next":{"href":"https://api.edamam.com/api/recipes/v2?q=pizza%2Cpesto&app_key=0d36941fbc68cf2b1760d8dcc477cc90&mealType=Dinner&_cont=CHcVQBtNNQphDmgVQ3tAEX4BYlZtAQADRWRFB2Iaa1ByBAQDUXlSAWIbMldwAQIPEWUUC2Eaa1V7BgJWQWAWUGNBZlF2BAIVLnlSVSBMPkd5BgNK&health=vegan&type=public&app_id=97452fee","title":"Next page"}},   "hits":[
			      {
			         "recipe":{
			            "label":"Spinach Pizza Pesto Pasta recipes",
			            "dietLabels":[
			               "High-Fiber"
			            ],
			            "healthLabels":[
			               "Vegan"
				],
			            "ingredientLines":[
			               "13.25 oz package whole grain pasta"
			            ],
			            "totalWeight":1051,
			            "cuisineType":[
			               "italian"
			            ],
			            "mealType":[
			               "lunch"
			            ],
			            "dishType":[
			               "main course"
			            ]}
			      }]}""";
	@Test
	void testGetRecipesWithoutKeywords()
	{
		RecipeClient client = new EdamamClient();
		RecipeSearchParams params = RecipeSearchParamsBuilder.withKeywords(List.of())
														     .withHealthRequest(null)
														     .withMealType(List.of()).build();
		assertThrowsExactly(IllegalArgumentException.class, () -> client.getRecipes(params));

	}

	@Test
	void testGetRecepiesWithOnlyKeywords()
	{
		HttpClient client = mock(HttpClient.class);
		RecipeClientResponse response = getRecipeClientResponse();

		try
		{
			when(client.send(any(), any())).thenReturn(new HttpResponseStub<>(200, RESPONSE));
			RecipeSearchParams params = RecipeSearchParamsBuilder.withKeywords(List.of("chicken"))
																 .withHealthRequest(List.of("Vegan"))
																 .withMealType(List.of("lunch")).build();

			assertEquals(response, new EdamamClient(client).getRecipes(params));
		}
		catch (IOException | InterruptedException e)
		{
			fail("Unexpected exception occured" + e.getMessage());
		}
	}

	@Test
	void testGetRecipesWhenClientException()
	{
		HttpClient client = mock(HttpClient.class);
		String responseString = "{\"status\":\"error\",\"message\":\"Unauthorized app_id = APP_ID\"}";

		try
		{
			when(client.send(any(), any())).thenReturn(new HttpResponseStub<>(400, responseString));
			RecipeSearchParams params = RecipeSearchParamsBuilder.withKeywords(List.of("chicken"))
																 .withHealthRequest(List.of("Vegan"))
																 .withMealType(List.of("lunch")).build();

			assertThrowsExactly(ClientException.class, () -> new EdamamClient(client).getRecipes(params));
		}
		catch (IOException | InterruptedException e)
		{
			fail("Unexpected exception occured" + e.getMessage());
		}
	}

	private static RecipeClientResponse getRecipeClientResponse()
	{
		Recipe clientRecipe = new Recipe("Spinach Pizza Pesto Pasta recipes", List.of("High-Fiber"), List.of("Vegan"), new BigDecimal(1051), List.of("italian"), List.of("lunch"), List.of("main course"), List.of("13.25 oz package whole grain pasta"));
		String nextPageToken = "https://api.edamam.com/api/recipes/v2?q=pizza%2Cpesto&app_key=0d36941fbc68cf2b1760d8dcc477cc90&mealType=Dinner&_cont=CHcVQBtNNQphDmgVQ3tAEX4BYlZtAQADRWRFB2Iaa1ByBAQDUXlSAWIbMldwAQIPEWUUC2Eaa1V7BgJWQWAWUGNBZlF2BAIVLnlSVSBMPkd5BgNK&health=vegan&type=public&app_id=97452fee";
		return new RecipeClientResponse(List.of(clientRecipe), nextPageToken);
	}
}
