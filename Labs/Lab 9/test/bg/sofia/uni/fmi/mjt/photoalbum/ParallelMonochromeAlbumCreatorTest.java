package bg.sofia.uni.fmi.mjt.photoalbum;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class ParallelMonochromeAlbumCreatorTest
{

	private final ParallelMonochromeAlbumCreator creator = new ParallelMonochromeAlbumCreator(3);

	@Test
	public void testConvertToBlackAndWhite() {

		Path source = Path.of("C:\\Users\\PC-Admin\\Pictures\\Screenshots");
		Path destination = Path.of("C:\\Users\\PC-Admin\\Pictures\\Screenshots\\bw");

		creator.processImages(source.toString(), destination.toString());

	}

}
