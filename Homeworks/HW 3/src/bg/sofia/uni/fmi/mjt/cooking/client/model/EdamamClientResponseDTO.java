package bg.sofia.uni.fmi.mjt.cooking.client.model;

import bg.sofia.uni.fmi.mjt.cooking.models.Recipe;

import java.util.List;
import java.util.Optional;

public record EdamamClientResponseDTO(
		 LinksDTO _links,
		 List<HitDTO> hits

) {
	private record LinksDTO(
			Link next

	) { }

	private record Link(
			String href

	) { }

	public record HitDTO(
			Recipe recipe
	) { }

	public RecipeClientResponse toRecipeClientResponse() {
		String link = Optional.ofNullable(_links.next())
				.map(Link::href)
				.orElse(null);
		return new RecipeClientResponse(
				hits.stream().map(HitDTO::recipe).toList(),
				link
		);
	}

}
