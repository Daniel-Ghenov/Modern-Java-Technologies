package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public record Mission(String id,
					  String company,
					  String location,
					  LocalDate date,
					  Detail detail,
					  RocketStatus rocketStatus,
					  Optional<Double> cost,
					  MissionStatus missionStatus) {

	public static Mission fromString(String str) {
		String[] parts = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
		int counter = 0;

		String id = parts[counter++];
		String company = parts[counter++];
		String location = parts[counter++].substring(1, parts[counter - 1].length() - 1);
		LocalDate date = LocalDate.parse(parts[counter++].substring(1, parts[counter - 1].length() - 1)
				, DateTimeFormatter.ofPattern("EEE MMM dd, yyyy", Locale.ENGLISH));
		Detail detail = Detail.fromString(parts[counter++]);
		RocketStatus rocketStatus = RocketStatus.fromString(parts[counter++]);
		String costStr = parts[counter++];
		if (!costStr.isEmpty()) {
			costStr = costStr.substring(1, costStr.length() - 2);
		}
		Optional<Double> cost = costStr.isEmpty() ? Optional.empty() : Optional.of(Double.parseDouble(costStr));
		MissionStatus missionStatus = MissionStatus.fromString(parts[counter]);

		return new Mission(id, company, location, date, detail, rocketStatus, cost, missionStatus);
	}

	public String getCountry() {
		String[] parts = location.split(",");
		return parts[parts.length - 1].trim();
	}

}
