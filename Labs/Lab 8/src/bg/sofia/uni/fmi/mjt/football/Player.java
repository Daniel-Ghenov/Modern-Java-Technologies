package bg.sofia.uni.fmi.mjt.football;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public record Player(String name,
                     String fullName,
                     LocalDate birthDate,
                     int age,
                     double heightCm,
                     double weightKg,
                     List<Position> positions,
                     String nationality,
                     int overallRating,
                     int potential,
                     long valueEuro,
                     long wageEuro,
                     Foot preferredFoot) {

    static Player of(String line) {
        String[] arguments = line.split(";");
        int iterator = 0;
        String name = arguments[iterator++];
        String fullName = arguments[iterator++];

        String[] date = arguments[iterator++].split("/");
        int month = Integer.parseInt(date[0]);
        int day = Integer.parseInt(date[1]);
        int year = Integer.parseInt(date[2]);
        LocalDate birthDate = LocalDate.of(year, month, day);

        int age = Integer.parseInt(arguments[iterator++]);
        double heightCm = Double.parseDouble(arguments[iterator++]);
        double weightKg = Double.parseDouble(arguments[iterator++]);
        List<Position> positions = Arrays.stream(arguments[iterator++].split(",")).map(Position::valueOf).toList();
        String nationality = arguments[iterator++];
        int overallRating = Integer.parseInt(arguments[iterator++]);
        int potential = Integer.parseInt(arguments[iterator++]);
        long valueEuro = Long.parseLong(arguments[iterator++]);
        long wageEuro = Long.parseLong(arguments[iterator++]);
        Foot preferredFoot = (Foot.valueOf(arguments[iterator].toUpperCase()));

        return new Player(
                name, fullName, birthDate, age, heightCm, weightKg, positions,
                nationality, overallRating, potential, valueEuro, wageEuro, preferredFoot
        );
    }

}
