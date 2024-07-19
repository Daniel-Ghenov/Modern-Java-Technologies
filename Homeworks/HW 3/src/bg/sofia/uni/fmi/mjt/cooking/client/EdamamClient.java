package bg.sofia.uni.fmi.mjt.cooking.client;

import bg.sofia.uni.fmi.mjt.cooking.client.model.ClientException;
import bg.sofia.uni.fmi.mjt.cooking.client.model.ClientExceptionParams;
import bg.sofia.uni.fmi.mjt.cooking.client.model.EdamamClientResponseDTO;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeSearchParams;
import bg.sofia.uni.fmi.mjt.cooking.client.model.RecipeClientResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EdamamClient implements RecipeClient {

	private final HttpClient httpClient;

	private static final String APP_ID = "app_id=" + System.getenv("APP_ID");

	private static final String APP_KEY = "app_key=" + System.getenv("APP_KEY");

	private static final String BASE_URL = "https://api.edamam.com/api/recipes/v2?type=public&"
										   + APP_ID + "&" + APP_KEY;

	private static final int MIN_SUCCESSFUL_STATUS_CODE = 200;

	private static final int MAX_SUCCESSFUL_STATUS_CODE = 299;

	public EdamamClient() {
		this(HttpClient.newHttpClient());
	}

	public EdamamClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public RecipeClientResponse getRecipes(RecipeSearchParams recepieSearchParams) {
		URI uri = recepieSearchParams.toUri(BASE_URL);
		HttpRequest request = getHttpRequest(uri);

		return getRecipeClientResponse(request);
	}

	private static HttpRequest getHttpRequest(URI uri) {
		return HttpRequest.newBuilder()
						  .uri(uri)
						  .GET()
						  .build();
	}

	@Override
	public RecipeClientResponse getRecipes(String nextPageToken) {
		HttpRequest request = getHttpRequest(URI.create(nextPageToken));

		return getRecipeClientResponse(request);
	}

	private RecipeClientResponse getRecipeClientResponse(HttpRequest request) {
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (!isSuccessful(response.statusCode())) {
				throw new ClientException(parseError(response.body()));
			}
			return parseResponse(response.body());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private ClientExceptionParams parseError(String body) {
		Gson gson = new Gson();
		JsonElement bodyJsonElement = JsonParser.parseString(body);
		JsonObject bodyJson = bodyJsonElement.getAsJsonObject();
		String error = bodyJson.get("status").toString();
		String message = bodyJson.get("message").toString();
		return new ClientExceptionParams(error, message);
	}

	private static boolean isSuccessful(int statusCode) {
		return statusCode >= MIN_SUCCESSFUL_STATUS_CODE && statusCode <= MAX_SUCCESSFUL_STATUS_CODE;
	}

	private RecipeClientResponse parseResponse(String body) {
		Gson gson = new Gson();
		EdamamClientResponseDTO responseDTO = gson.fromJson(body, EdamamClientResponseDTO.class);
		return responseDTO.toRecipeClientResponse();
	}

}
