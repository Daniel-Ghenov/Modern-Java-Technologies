package bg.sofia.uni.fmi.mjt.football;

import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FootballPlayerAnalyzerTest {



    private static final String messi = "Messi;Lionel Messi;06/24/1987;33;170.0;72.0;CF;Argentina;93;93;67500000;560000;LEFT";

    private static final String ronaldo = "Ronaldo;Cristiano Ronaldo;02/05/1985;35;187.0;83.0;ST,LW;Portugal;92;92;46000000;220000;RIGHT";

    private static final String lewandowski = "Lewandowski;Robert Lewandowski;08/21/1988;32;184.0;80.0;ST;Poland;91;91;80000000;240000;RIGHT";

    private static final String messiRonaldoLewandowski ='\n' +  messi + '\n' + ronaldo + '\n' + lewandowski;

    @Test
    void testGetAllPlayers() {
        Player player = Player.of(messi);
        Reader reader = new StringReader('\n' +  messi);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertIterableEquals(List.of(player), analyzer.getAllPlayers());

    }

    @Test
    void testGetAllNationalities() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertEquals(Set.of("Argentina", "Portugal", "Poland"), analyzer.getAllNationalities());
    }

    @Test
    void testGetHighestPaidPlayerByNationalityWhenNoSuchElement() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertThrows(NoSuchElementException.class, () -> analyzer.getHighestPaidPlayerByNationality("Bulgaria"));
    }

    @Test
    void testGetHighestPaidPlayerByNationality() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertEquals(Player.of(messi), analyzer.getHighestPaidPlayerByNationality("Argentina"));
    }

    @Test
    void testGetHighestPaidPlayerByNationalityWhenNull() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertThrows(IllegalArgumentException.class, () -> analyzer.getHighestPaidPlayerByNationality(null));
    }

    @Test
    void testGroupByPosition() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertEquals(Map.of(
                Position.CF, Set.of(Player.of(messi)),
                Position.ST, Set.of(Player.of(ronaldo), Player.of(lewandowski)),
                Position.LW, Set.of(Player.of(ronaldo)))
        , analyzer.groupByPosition());
    }

    @Test
    void testGetTopProspectPlayerForPositionInBudgetWhenNegative() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertThrows(IllegalArgumentException.class, () -> analyzer.getTopProspectPlayerForPositionInBudget(Position.CF, -1));
    }

    @Test
    void testGetTopProspectPlayerForPositionInBudgetWhenPositionNull() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertThrows(IllegalArgumentException.class, () -> analyzer.getTopProspectPlayerForPositionInBudget(null, 1));
    }

    @Test
    void testGetTopProspectPlayerForPositionInBudget() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertEquals(Optional.of(Player.of(lewandowski)), analyzer.getTopProspectPlayerForPositionInBudget(Position.ST, 80000000));
    }

    @Test
    void testGetSimilarPlayersWhenNoSimilarPlayers() {
        Reader reader = new StringReader('\n' +  messi + '\n' + ronaldo);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertIterableEquals(List.of(), analyzer.getSimilarPlayers(Player.of(messi)));
    }

    @Test
    void testGetSimilarPlayers() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertIterableEquals(List.of(Player.of(ronaldo)), analyzer.getSimilarPlayers(Player.of(lewandowski)));
    }


    @Test
    void testGetPlayersByFullNameKeywordWhenKeywordNull() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertThrows(IllegalArgumentException.class, () -> analyzer.getPlayersByFullNameKeyword(null));
    }

    @Test
    void testGetPlayersByFullNameKeywordWhenNoPlayers() {
        Reader reader = new StringReader(messiRonaldoLewandowski);

        FootballPlayerAnalyzer analyzer = new FootballPlayerAnalyzer(reader);

        assertIterableEquals(List.of(), analyzer.getPlayersByFullNameKeyword("Neymar"));
    }
}
