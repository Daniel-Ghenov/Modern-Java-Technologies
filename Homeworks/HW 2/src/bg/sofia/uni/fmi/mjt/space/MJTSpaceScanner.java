package bg.sofia.uni.fmi.mjt.space;

import bg.sofia.uni.fmi.mjt.space.algorithm.Rijndael;
import bg.sofia.uni.fmi.mjt.space.algorithm.SymmetricBlockCipher;
import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import bg.sofia.uni.fmi.mjt.space.exception.TimeFrameMismatchException;
import bg.sofia.uni.fmi.mjt.space.mission.Mission;
import bg.sofia.uni.fmi.mjt.space.mission.MissionStatus;
import bg.sofia.uni.fmi.mjt.space.rocket.Rocket;
import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MJTSpaceScanner implements SpaceScannerAPI {

	private final List<Mission> missions;

	private final List<Rocket> rockets;

	private final SymmetricBlockCipher cipher;

	public MJTSpaceScanner(List<Mission> missions, List<Rocket> rockets, SymmetricBlockCipher cipher) {
		this.missions = missions;
		this.rockets = rockets;
		this.cipher = cipher;
	}

	public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SymmetricBlockCipher cipher) {
		this.cipher = cipher;
		this.missions = new ArrayList<>();
		this.rockets = new ArrayList<>();
		parseMissions(missionsReader);
		parseRockets(rocketsReader);
	}

	public MJTSpaceScanner(Reader missionsReader, Reader rocketsReader, SecretKey secretKey) {
		this(missionsReader, rocketsReader, new Rijndael(secretKey));
	}

	@Override public Collection<Mission> getAllMissions() {
		return List.copyOf(missions);
	}

	@Override public Collection<Mission> getAllMissions(MissionStatus missionStatus) {
		if (missionStatus == null)
			throw new IllegalArgumentException("missionStatus cannot be null");

		return missions.stream()
				.filter(mission -> mission.missionStatus().equals(missionStatus))
				.toList();
	}

	@Override public String getCompanyWithMostSuccessfulMissions(LocalDate from, LocalDate to) {
		if (from == null || to == null)
			throw new IllegalArgumentException("from and to cannot be null");
		if (to.isBefore(from))
			throw new TimeFrameMismatchException("to cannot be before from");

		return missions.stream()
				.filter(mission -> mission.date().isAfter(from) && mission.date().isBefore(to))
				.collect(Collectors.groupingBy(Mission::company, Collectors.counting()))
				.entrySet()
				.stream()
				.max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.orElse("");
	}

	@Override
	public Map<String, Collection<Mission>> getMissionsPerCountry() {
		return missions.stream()
				.collect(Collectors.groupingBy(Mission::getCountry,
							   Collectors.toCollection(ArrayList::new)));
	}

	@Override
	public List<Mission> getTopNLeastExpensiveMissions(int n,
									   MissionStatus missionStatus,
									   RocketStatus rocketStatus) {
		if (n <= 0) {
			throw new IllegalArgumentException("N cannot be <= 0");
		}

		if (missionStatus == null || rocketStatus == null) {
			throw new IllegalArgumentException("Mission and rocket statuses cannot be null");
		}

		return missions.stream()
				.filter(mission -> mission.missionStatus().equals(missionStatus)
								&& mission.rocketStatus().equals(rocketStatus)
				                && mission.cost().isPresent()
				).sorted(Comparator.comparingDouble(mission -> mission.cost().get()))
				.limit(n)
				.toList();

	}

	@Override public Map<String, String> getMostDesiredLocationForMissionsPerCompany() {
		return missions.stream()
				.collect(Collectors.groupingBy(
						Mission::company,
						Collectors.collectingAndThen(
						Collectors.groupingBy(Mission::location, Collectors.counting()),
								missions -> missions.entrySet()
										.stream()
										.max(Map.Entry.comparingByValue())
										.map(Map.Entry::getKey)
										.get())
				));
	}

	@Override
	public Map<String, String> getLocationWithMostSuccessfulMissionsPerCompany(LocalDate from, LocalDate to) {
		if (from == null || to == null) {
			throw new IllegalArgumentException("From and to cannot be null");
		}

		if (to.isBefore(from)) {
			throw new TimeFrameMismatchException("To cannot be before from");
		}

		return missions.stream()
				.filter(mission -> mission.date().isAfter(from) && mission.date().isBefore(to)
								&& mission.missionStatus().equals(MissionStatus.SUCCESS)
				)
				.collect(Collectors.groupingBy(
						Mission::company,
						Collectors.collectingAndThen(
							Collectors.groupingBy(Mission::location, Collectors.counting()),
							missions -> missions.entrySet()
									.stream()
									.max(Map.Entry.comparingByValue())
									.map(Map.Entry::getKey)
									.get())
				));
	}

	@Override public Collection<Rocket> getAllRockets() {
		return Collections.unmodifiableCollection(rockets);
	}

	@Override public List<Rocket> getTopNTallestRockets(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("N cannot be <= 0");
		}

		return rockets.stream()
				.filter(rocket -> rocket.height().isPresent())
				.sorted(HEIGHT_COMPARATOR.reversed())
				.limit(n)
				.toList();
	}

	@Override
	public Map<String, Optional<String>> getWikiPageForRocket() {
		return rockets.stream()
				.collect(Collectors.toMap(Rocket::name, Rocket::wiki));
	}

	@Override
	public List<String> getWikiPagesForRocketsUsedInMostExpensiveMissions(int n,
											  MissionStatus missionStatus,
										 	 RocketStatus rocketStatus) {
		if (n <= 0) {
			throw new IllegalArgumentException("N cannot be <= 0");
		}
		if (missionStatus == null || rocketStatus == null) {
			throw new IllegalArgumentException("Mission and rocket statuses cannot be null");
		}

		Map<Mission, String> rocketsByMission = missions.stream()
				.collect(Collectors.toMap(Function.identity(),
								  mission -> mission.detail().rocketName()));

		Map<String, Rocket> rocketsByName = rockets.stream()
				.collect(Collectors.toMap(Rocket::name, Function.identity()));

		return missions.stream()
				.filter(mission -> mission.missionStatus().equals(missionStatus) &&
									mission.rocketStatus().equals(rocketStatus) &&
									mission.cost().isPresent())
				.sorted(COST_COMPARATOR.reversed())
				.map(mission -> getRocketOrNull(rocketsByMission, rocketsByName, mission))
				.filter(Objects::nonNull)
			    .limit(n)
				.toList();
	}

	private String getRocketOrNull(Map<Mission, String> rocketsByMission,
								   Map<String, Rocket> rocketsByName,
								   Mission mission) {
		Rocket rocket = rocketsByName.get(rocketsByMission.get(mission));
		if (rocket != null) {
			rocketsByName.remove(rocket.name());
			return rocket.wiki().orElse("");
		}
		return null;
	}

	@Override
	public void saveMostReliableRocket(OutputStream outputStream,
									   LocalDate from,
									   LocalDate to) throws CipherException {

		if (outputStream == null || from == null || to == null) {
			throw new IllegalArgumentException("Output stream, from and to cannot be null");
		}
		if (to.isBefore(from)) {
			throw new TimeFrameMismatchException("To cannot be before from");
		}
		Map<String, Rocket> rocketsByName = rockets.stream()
							   .collect(Collectors.toMap(Rocket::name,
												 Function.identity()));

		Map<Rocket, Long> successfulMissions = getSuccessfulMissionsByRocket(from, to, rocketsByName);

		Map<Rocket, Long> unsuccessfulMissions = getUnsuccessfulMissionsByRocket(from, to, rocketsByName);

		Rocket mostReliable = rockets.stream()
						.max(Comparator.comparing(
								r -> getReliability(successfulMissions.get(r),
										  unsuccessfulMissions.get(r)))
							).get();
		saveRocket(outputStream, mostReliable);
	}

	private Map<Rocket, Long> getUnsuccessfulMissionsByRocket(LocalDate from,
													  LocalDate to,
										  Map<String, Rocket> rocketsByName) {
		return missions.stream()
					   .filter(mission -> !mission.missionStatus().equals(MissionStatus.SUCCESS)
							  && mission.date().isAfter(from) && mission.date().isBefore(to)
							  && rocketsByName.containsKey(mission.detail().rocketName()))
					   .collect(Collectors.groupingBy(
							   mission -> rocketsByName.get(mission.detail().rocketName())
							   , Collectors.counting()));
	}

	private Map<Rocket, Long> getSuccessfulMissionsByRocket(LocalDate from,
													LocalDate to,
										Map<String, Rocket> rocketsByName) {
		return missions.stream()
					   .filter(mission -> mission.missionStatus().equals(MissionStatus.SUCCESS)
							  && mission.date().isAfter(from) && mission.date().isBefore(to)
							  && rocketsByName.containsKey(mission.detail().rocketName()))
					   .collect(Collectors.groupingBy(mission ->
								  rocketsByName.get(mission.detail().rocketName())
							   , Collectors.counting()));
	}

	private void saveRocket(OutputStream outputStream, Rocket rocket) throws CipherException {
		InputStream is = new ByteArrayInputStream(rocket.toString().getBytes());
		cipher.encrypt(is, outputStream);
	}

	private static BigDecimal getReliability(Long successfulMissions, Long unsuccessfulMissions) {
		if (successfulMissions == null) {
			successfulMissions = 0L;
		}
		if (unsuccessfulMissions == null) {
			unsuccessfulMissions = 0L;
		}
		if (successfulMissions + unsuccessfulMissions == 0L) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(2L * successfulMissions + unsuccessfulMissions)
			 .divide(BigDecimal.valueOf(2L * (successfulMissions + unsuccessfulMissions))
			 , MathContext.DECIMAL64);
	}

	private void parseMissions(Reader missionsReader) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream is = new ByteArrayInputStream(parseFromReader(missionsReader));
		try {
			cipher.decrypt(is, os);
			String[] lines = os.toString().split("\n");
			for (int i = 1; i < lines.length; i++) {
				missions.add(Mission.fromString(lines[i]));
			}
		} catch (CipherException e) {
			throw new RuntimeException(e);
		}
	}

	private void parseRockets(Reader rocketsReader) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream is = new ByteArrayInputStream(parseFromReader(rocketsReader));
		try {
			cipher.decrypt(is, os);
			String[] lines = os.toString().split("\n");
			for (int i = 1; i < lines.length; i++) {
				rockets.add(Rocket.fromString(lines[i]));
			}
		} catch (CipherException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] parseFromReader(Reader reader) {

		Reader bufferedReader = new BufferedReader(reader);
		StringBuilder sb = new StringBuilder();
		int c;
		try {
			while ((c = bufferedReader.read()) != -1) {
				sb.append((char) c);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString().getBytes();
	}

	private static final Comparator<Mission> COST_COMPARATOR = (o1, o2) -> {
		return o1.cost().get().compareTo(o2.cost().get());
	};

	private static final Comparator<Rocket> HEIGHT_COMPARATOR = (o1, o2) -> {
		return o1.height().get().compareTo(o2.height().get());
	};

}
