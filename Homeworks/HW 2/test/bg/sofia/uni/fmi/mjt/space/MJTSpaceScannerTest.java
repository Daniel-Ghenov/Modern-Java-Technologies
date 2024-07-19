package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.algorithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.fail;


public class MJTSpaceScannerTest
{

	private MJTSpaceScanner scanner;

	private static final int KEY_SIZE_IN_BITS = 128;

	private static final String ENCRYPTION_ALGORITHM = "AES";

	private static final List<Mission> missions;

	private static final List<Rocket> rockets;

	@BeforeEach
	public void setUp() {
		this.scanner = new MJTSpaceScanner(missions, rockets, createCipher());
	}

	@Test
	public void testParsing () {
		SymmetricBlockCipher cipher = createCipher();

		InputStream missionsStream = new ByteArrayInputStream(MISSIONS_CSV.getBytes());
		InputStream rocketsStream = new ByteArrayInputStream(ROCKETS_CSV.getBytes());
		ByteArrayOutputStream missionsOs = new ByteArrayOutputStream();
		ByteArrayOutputStream rocketsOs = new ByteArrayOutputStream();

		Reader missionsReader;
		Reader rocketsReader;
		try
		{
			cipher.encrypt(missionsStream, missionsOs);
			cipher.encrypt(rocketsStream, rocketsOs);

			missionsReader = new StringReader(missionsOs.toString());
			rocketsReader = new StringReader(rocketsOs.toString());
		}
		catch (CipherException e)
		{
			throw new RuntimeException(e);
		}

		MJTSpaceScanner scanner = new MJTSpaceScanner(missionsReader, rocketsReader, cipher);

		assertIterableEquals(missions, scanner.getAllMissions());
		assertIterableEquals(rockets, scanner.getAllRockets());

	}

	@Test
	public void testParseMissionsWhenCipherKeyIsNull()
	{
		SymmetricBlockCipher cipherToTest = new Rijndael(null);
		SymmetricBlockCipher cipher = createCipher();

		InputStream missionsStream = new ByteArrayInputStream(MISSIONS_CSV.getBytes());
		InputStream rocketsStream = new ByteArrayInputStream(ROCKETS_CSV.getBytes());
		ByteArrayOutputStream missionsOs = new ByteArrayOutputStream();
		ByteArrayOutputStream rocketsOs = new ByteArrayOutputStream();

		Reader missionsReader;
		Reader rocketsReader;
		try
		{
			cipher.encrypt(missionsStream, missionsOs);
			cipher.encrypt(rocketsStream, rocketsOs);

			missionsReader = new StringReader(missionsOs.toString());
			rocketsReader = new StringReader(rocketsOs.toString());
		}
		catch (CipherException e)
		{
			throw new RuntimeException(e);
		}

		assertThrows(RuntimeException.class, () -> new MJTSpaceScanner(missionsReader, rocketsReader, cipherToTest));
	}


	@Test
	public void testGetAllMissions()
	{
		Collection<Mission> allMissions = scanner.getAllMissions();
		assertIterableEquals(missions, allMissions);
	}

	@Test
	public void testGetAllMissionsByMissionStatusWhenNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getAllMissions(null));
	}

	@Test
	public void testGetAllMissionsByMissionStatusWhenStatusPartiallyFailed()
	{
		Collection<Mission> allMissions = scanner.getAllMissions(MissionStatus.PARTIAL_FAILURE);
		assertIterableEquals(List.of(missions.get(5)), allMissions);
	}

	@Test
	public void testGetAllMissionsByMissionStatusWhenStatusSuccess()
	{
		Collection<Mission> allMissions = scanner.getAllMissions(MissionStatus.SUCCESS);
		assertIterableEquals(List.of(missions.get(0), missions.get(1), missions.get(2), missions.get(3), missions.get(4),
									 missions.get(6)), allMissions);
	}

	@Test
	public void testGetCompanyWithMostSuccessfulMissionsWhenFromIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getCompanyWithMostSuccessfulMissions(null, LocalDate.now()));
	}

	@Test
	public void testGetCompanyWithMostSuccessfulMissionsWhenToIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getCompanyWithMostSuccessfulMissions(LocalDate.now(), null));
	}

	@Test
	public void testGetCompanyWithMostSuccessfulMissionsWhenToIsBeforeFrom()
	{
		assertThrowsExactly(TimeFrameMismatchException.class, () -> scanner.getCompanyWithMostSuccessfulMissions(LocalDate.now(), LocalDate.now().minusDays(1)));
	}

	@Test
	public void testGetCompanyWithMostSuccessfulMissions()
	{
		String companyWithMostSuccessfulMissions = scanner.getCompanyWithMostSuccessfulMissions(LocalDate.of(2020, 8, 3), LocalDate.of(2020, 8, 10));
		assertEquals("SpaceX", companyWithMostSuccessfulMissions);
	}

	@Test
	public void testGetMissionsPerCountry()
	{
		Map<String, Collection<Mission>> missionsPerCountry = scanner.getMissionsPerCountry();
		Map<String, Collection<Mission>> expected = new HashMap<>();
		expected.put("USA", List.of(missions.get(0), missions.get(2), missions.get(4)));
		expected.put("China", List.of(missions.get(1), missions.get(5)));
		expected.put("Kazakhstan", List.of(missions.get(3), missions.get(6)));
		assertIterableEquals(expected.entrySet(), missionsPerCountry.entrySet());
	}

	@Test
	public void testGetTopNLeastExpensiveMissionsWhenNIsZero()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getTopNLeastExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE));
	}

	@Test
	public void testGetTopNLeastExpensiveMissionsWhenMissionStatusIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getTopNLeastExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE));
	}

	@Test
	public void testGetTopNLeastExpensiveMissionsWhenRocketStatusIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getTopNLeastExpensiveMissions(1, MissionStatus.SUCCESS, null));
	}

	@Test
	public void testGetTopNLeastExpensiveMissions()
	{
		List<Mission> topNLeastExpensiveMissions = scanner.getTopNLeastExpensiveMissions(3, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE);
		assertIterableEquals(List.of(missions.get(1), missions.get(6), missions.get(0)), topNLeastExpensiveMissions);
	}

	@Test
	public void testGetMostDesiredLocationForMissionsPerCompany()
	{
		Map<String, String> mostDesiredLocationForMissionsPerCompany = scanner.getMostDesiredLocationForMissionsPerCompany();
		Map<String, String> expected = new HashMap<>();
		expected.put("SpaceX", "LC-39A, Kennedy Space Center, Florida, USA");
		expected.put("CASC", "Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China");
		expected.put("Roscosmos", "Site 31/6, Baikonur Cosmodrome, Kazakhstan");
		expected.put("ULA", "SLC-41, Cape Canaveral AFS, Florida, USA");
		assertIterableEquals(expected.entrySet(), mostDesiredLocationForMissionsPerCompany.entrySet());
	}

	@Test
	public void testGetLocationWithMostSuccessfulMissionsPerCompanyWhenFromIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getLocationWithMostSuccessfulMissionsPerCompany(null, LocalDate.now()));
	}

	@Test
	public void testGetLocationWithMostSuccessfulMissionsPerCompanyWhenToIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getLocationWithMostSuccessfulMissionsPerCompany(LocalDate.now(), null));
	}

	@Test
	public void testGetLocationWithMostSuccessfulMissionsPerCompanyWhenToIsBeforeFrom()
	{
		assertThrowsExactly(TimeFrameMismatchException.class, () -> scanner.getLocationWithMostSuccessfulMissionsPerCompany(LocalDate.now(), LocalDate.now().minusDays(1)));
	}

	@Test
	public void testGetLocationWithMostSuccessfulMissionsPerCompany()
	{
		Map<String, String> locationWithMostSuccessfulMissionsPerCompany = scanner.getLocationWithMostSuccessfulMissionsPerCompany(LocalDate.of(2020, 6, 3), LocalDate.of(2020, 8, 10));
		Map<String, String> expected = new HashMap<>();
		expected.put("SpaceX", "LC-39A, Kennedy Space Center, Florida, USA");
		expected.put("CASC", "Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China");
		expected.put("Roscosmos", "Site 31/6, Baikonur Cosmodrome, Kazakhstan");
		expected.put("ULA", "SLC-41, Cape Canaveral AFS, Florida, USA");
		assertEquals(expected, locationWithMostSuccessfulMissionsPerCompany);
	}

	@Test
	public void testGetAllRockets()
	{
		Collection<Rocket> allRockets = scanner.getAllRockets();
		assertIterableEquals(rockets, allRockets);
	}

	@Test
	public void testgetTopNTallestRocketsWhenNIsZero()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getTopNTallestRockets(0));
	}

	@Test
	public void testgetTopNTallestRockets()
	{
		List<Rocket> topNTallestRockets = scanner.getTopNTallestRockets(3);
		assertIterableEquals(List.of(rockets.get(5), rockets.get(6), rockets.get(7)), topNTallestRockets);
	}

	@Test
	public void testGetWikiPageForRocket()
	{
		Map<String, Optional<String>> wikiPageForRocket = scanner.getWikiPageForRocket();
		Map<String, Optional<String>> expected = new HashMap<>();
		expected.put("Tsyklon-3", Optional.of("https://en.wikipedia.org/wiki/Tsyklon-3"));
		expected.put("Tsyklon-4M", Optional.of("https://en.wikipedia.org/wiki/Cyclone-4M"));
		expected.put("Unha-2", Optional.of("https://en.wikipedia.org/wiki/Unha"));
		expected.put("Unha-3", Optional.of("https://en.wikipedia.org/wiki/Unha"));
		expected.put("Vanguard", Optional.of("https://en.wikipedia.org/wiki/Vanguard_(rocket)"));
		expected.put("Falcon 9 Block 5", Optional.of("https://en.wikipedia.org/wiki/Falcon_9"));
		expected.put("Atlas V 541", Optional.of("https://en.wikipedia.org/wiki/Atlas_V"));
		expected.put("Proton-M/Briz-M", Optional.of("https://en.wikipedia.org/wiki/Proton-M"));
		expected.put("Long March 4B", Optional.of("https://en.wikipedia.org/wiki/Long_March_4B"));
		expected.put("Long March 2D", Optional.of("https://en.wikipedia.org/wiki/Long_March_2D"));


		assertIterableEquals(expected.entrySet(), wikiPageForRocket.entrySet());
	}

	@Test
	public void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWhenNIsZero()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(0, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE));
	}

	@Test
	public void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWhenMissionStatusIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, null, RocketStatus.STATUS_ACTIVE));
	}

	@Test
	public void testGetWikiPagesForRocketsUsedInMostExpensiveMissionsWhenRocketStatusIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(1, MissionStatus.SUCCESS, null));
	}

	@Test
	public void testGetWikiPagesForRocketsUsedInMostExpensiveMissions()
	{
		List<String> wikiPagesForRocketsUsedInMostExpensiveMissions = scanner.getWikiPagesForRocketsUsedInMostExpensiveMissions(3, MissionStatus.SUCCESS, RocketStatus.STATUS_ACTIVE);
		assertIterableEquals(List.of("https://en.wikipedia.org/wiki/Atlas_V", "https://en.wikipedia.org/wiki/Proton-M", "https://en.wikipedia.org/wiki/Falcon_9"), wikiPagesForRocketsUsedInMostExpensiveMissions);
	}


	@Test
	public void testSaveMostReliableRocketWhenFromIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.saveMostReliableRocket( new ByteArrayOutputStream(), null, LocalDate.now()));
	}

	@Test
	public void testSaveMostReliableRocketWhenToIsNull()
	{
		assertThrowsExactly(IllegalArgumentException.class, () -> scanner.saveMostReliableRocket( new ByteArrayOutputStream(), LocalDate.now(), null));
	}

	@Test
	public void testSaveMostReliableRocketWhenToIsBeforeFrom()
	{
		assertThrowsExactly(TimeFrameMismatchException.class, () -> scanner.saveMostReliableRocket( new ByteArrayOutputStream(), LocalDate.now(), LocalDate.now().minusDays(1)));
	}

	@Test
	public void testSaveMostReliableRocket()
	{
		SymmetricBlockCipher cipher = createCipher();
		MJTSpaceScanner scanner = new MJTSpaceScanner(parseMissions(MISSIONS_CSV), parseRockets(ROCKETS_CSV), cipher);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try
		{
			scanner.saveMostReliableRocket(outputStream, LocalDate.of(2020, 8, 3), LocalDate.of(2020, 8, 10));
		}
		catch (CipherException e)
		{
			fail("CipherException thrown");
		}
		byte[] actual = outputStream.toByteArray();
		ByteArrayOutputStream expectedOutputStream = new ByteArrayOutputStream();

		try
		{
			cipher.encrypt(new ByteArrayInputStream(rockets.get(5).toString().getBytes()), expectedOutputStream);
		}
		catch (CipherException e)
		{
			fail();
		}

		byte[] expected = expectedOutputStream.toByteArray();
		assertArrayEquals(expected, actual);
	}


	private static List<Rocket> parseRockets(String rockets)
	{
		return Arrays.stream(rockets.split("\n"))
					 .skip(1)
					 .map(Rocket::fromString)
					 .toList();
	}

	private static List<Mission> parseMissions(String missions)
	{
		List<String> rows = Arrays.stream(missions.split("\n"))
					 .skip(1)
					.toList();
		return rows.stream()
					 .map(Mission::fromString)
					 .toList();
	}

	private static SymmetricBlockCipher createCipher()
	{
		KeyGenerator keyGenerator = null;
		try
		{
			keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
			keyGenerator.init(KEY_SIZE_IN_BITS);
			SecretKey secretKey = keyGenerator.generateKey();
			return new Rijndael(secretKey);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}

	}

	private static final String MISSIONS_CSV = """
								Unnamed: 0,Company Name,Location,Datum,Detail,Status Rocket," Rocket",Status Mission
								0,SpaceX,"LC-39A, Kennedy Space Center, Florida, USA","Fri Aug 07, 2020",Falcon 9 Block 5 | Starlink V1 L9 & BlackSky,StatusActive,"50.0 ",Success
								1,CASC,"Site 9401 (SLS-2), Jiuquan Satellite Launch Center, China","Thu Aug 06, 2020",Long March 2D | Gaofen-9 04 & Q-SAT,StatusActive,"29.75 ",Success
								2,SpaceX,"Pad A, Boca Chica, Texas, USA","Tue Aug 04, 2020",Starship Prototype | 150 Meter Hop,StatusActive,,Success
								3,Roscosmos,"Site 200/39, Baikonur Cosmodrome, Kazakhstan","Thu Jul 30, 2020",Proton-M/Briz-M | Ekspress-80 & Ekspress-103,StatusActive,"65.0 ",Success
								4,ULA,"SLC-41, Cape Canaveral AFS, Florida, USA","Thu Jul 30, 2020",Atlas V 541 | Perseverance,StatusActive,"145.0 ",Success
								5,CASC,"LC-9, Taiyuan Satellite Launch Center, China","Sat Jul 25, 2020","Long March 4B | Ziyuan-3 03, Apocalypse-10 & NJU-HKU 1",StatusActive,"64.68 ",Partial Failure
								6,Roscosmos,"Site 31/6, Baikonur Cosmodrome, Kazakhstan","Thu Jul 23, 2020",Soyuz 2.1a | Progress MS-15,StatusActive,"48.5 ",Success""";

	private static final String ROCKETS_CSV = """
				"",Name,Wiki,Rocket Height
				0,Tsyklon-3,https://en.wikipedia.org/wiki/Tsyklon-3,39.0 m
				1,Tsyklon-4M,https://en.wikipedia.org/wiki/Cyclone-4M,38.7 m
				2,Unha-2,https://en.wikipedia.org/wiki/Unha,28.0 m
				3,Unha-3,https://en.wikipedia.org/wiki/Unha,32.0 m
				4,Vanguard,https://en.wikipedia.org/wiki/Vanguard_(rocket),23.0 m
				169,Falcon 9 Block 5,https://en.wikipedia.org/wiki/Falcon_9,70.0 m
				103,Atlas V 541,https://en.wikipedia.org/wiki/Atlas_V,62.2 m
				294,Proton-M/Briz-M,https://en.wikipedia.org/wiki/Proton-M,58.2 m
				228,Long March 4B,https://en.wikipedia.org/wiki/Long_March_4B,44.1 m
				213,Long March 2D,https://en.wikipedia.org/wiki/Long_March_2D,41.06 m
				""";

	static {
		missions = parseMissions(MISSIONS_CSV);
		rockets = parseRockets(ROCKETS_CSV);
	}

}
