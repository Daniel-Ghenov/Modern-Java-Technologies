package bg.sofia.uni.fmi.mjt.cooking.client.model;

import java.net.URI;
import java.util.List;

public record RecipeSearchParams(
		List<String> keywords,
		List<String> mealType,
		List<String> healthRequest
) {

	public URI toUri(String baseUrl) {

		if (keywords == null || keywords.isEmpty()) {
			throw new IllegalArgumentException("Keywords cannot be empty");
		}

		StringBuilder uriStringBuilder = new StringBuilder(baseUrl)
				.append("&q=")
				.append(String.join(",", keywords));
		if (!mealType.isEmpty()) {
			uriStringBuilder
					.append("&mealType=")
					.append(String.join("&mealType=", mealType));
		}
		if (!healthRequest.isEmpty()) {
			uriStringBuilder
					.append("&health=")
					.append(String.join("&health=", healthRequest));
		}

		return URI.create(uriStringBuilder.toString());
	}

}
