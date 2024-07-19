package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.exception.CityNotKnownException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.NoPathToDestinationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SequencedCollection;

public class RideRight implements ItineraryPlanner{

    private final List<Journey> schedule;

    public RideRight(List<Journey> schedule) {
        this.schedule = schedule;
    }
    @Override
    public SequencedCollection<Journey> findCheapestPath(City start, City destination, boolean allowTransfer) throws CityNotKnownException, NoPathToDestinationException {

        assertExist(start, destination);

        if(!allowTransfer) {
            return List.of(findCheapestPathNoTransfer(start, destination));
        }

        return findPathAStar(start, destination);
    }

    private SequencedCollection<Journey> findPathAStar(City start, City destination) throws NoPathToDestinationException {

        Set<City> openSet = new HashSet<>();
        openSet.add(start);

        Map<City, Journey> cameFrom = new HashMap<>();

        Map<City, BigDecimal> gScore = new HashMap<>();
        gScore.put(start, BigDecimal.ZERO);

        Map<City, BigDecimal> fScore = new HashMap<>();
        fScore.put(start, BigDecimal.ZERO);

        while (!openSet.isEmpty()) {
            City current = lowestFScore(openSet, fScore);

            if(current.equals(destination)) {
                return reconstructPath(current, cameFrom);
            }
            openSet.remove(current);

            for (Journey journey : getNeighbours(current)) {
                City neighbour = journey.to();
                BigDecimal tentativeGScore = gScore.get(current).add(journey.computePrice());

                if(!gScore.containsKey(neighbour) || tentativeGScore.compareTo(gScore.get(neighbour)) < 0) {
                    cameFrom.put(neighbour, journey);
                    gScore.put(neighbour, tentativeGScore);
                    fScore.put(neighbour, tentativeGScore.add(BigDecimal.valueOf(heuristicCostEstimate(neighbour, destination))));
                    openSet.add(neighbour);
                }

            }

        }
        throw new NoPathToDestinationException("No path exists from start to destination, with transfer");
    }

    List<Journey> getNeighbours(City current) {
        List<Journey> neighbours = new ArrayList<>();

        for (Journey journey : schedule) {
            if(journey.from().equals(current)) {
                neighbours.add(journey);
            }
        }

        return neighbours;
    }

    private int heuristicCostEstimate(City start, City destination) {
        return manhattanDistance(start, destination) * 20;
    }
    private int manhattanDistance(City start, City destination) {
        return (Math.abs(start.location().x() - destination.location().x()) + Math.abs(start.location().y() - destination.location().y())) / 1000;
    }


    private SequencedCollection<Journey> reconstructPath(City current, Map<City, Journey> cameFrom) {
        List<Journey> path = new LinkedList<>();

        while (cameFrom.containsKey(current)) {
            Journey currentJourney = cameFrom.get(current);
            current = currentJourney.from();
            path.add(currentJourney);
        }

        return path.reversed();
    }

    private City lowestFScore(Set<City> openSet, Map<City, BigDecimal> fScore) {
        //TODO: fix implementation, so that this is O(logN) instead of O(n)
        BigDecimal lowest = fScore.getOrDefault(openSet.iterator().next(), BigDecimal.valueOf(Integer.MAX_VALUE));
        City lowestCity = openSet.iterator().next();

        for(City c : openSet) {
            if(fScore.get(c) == null) {
                fScore.put(c, BigDecimal.valueOf(Integer.MAX_VALUE));
            }

            if(fScore.get(c).compareTo(lowest) < 0) {
                lowest = fScore.get(c);
                lowestCity = c;
            }

            if(fScore.get(c).compareTo(lowest) == 0) {
                if(c.name().compareTo(lowestCity.name()) < 0) {
                    lowest = fScore.get(c);
                    lowestCity = c;
                }
            }
        }

        return lowestCity;
    }

    private Journey findCheapestPathNoTransfer(City start, City destination) throws  NoPathToDestinationException {

        boolean found = false;
        BigDecimal lowest = BigDecimal.valueOf(0);
        Journey lowestJourney = null;

        for (Journey journey: schedule) {

            if(journey.from().equals(start) && journey.to().equals(destination)) {

                if(!found) {
                    lowest = journey.computePrice();
                    lowestJourney = journey;
                    found = true;
                } else if(lowest.compareTo(journey.computePrice()) > 0) {
                    lowest = journey.computePrice();
                    lowestJourney = journey;
                }

            }

        }

        if(!found) {
            throw new NoPathToDestinationException("No path exists from start to destination, without transfer");
        }

        return lowestJourney;

    }

    private void assertExist(City start, City destination) throws  CityNotKnownException{

        boolean hasStart = false;
        boolean hasDestination = false;
        for (Journey journey : schedule) {
            if(journey.hasCity(start)) {
                hasStart = true;
            }
            if(journey.hasCity(destination)) {
                hasDestination = true;
            }
        }
        if(!hasStart) {
            throw new CityNotKnownException("Starting city is not in list of known cities");
        }
        if(!hasDestination) {
            throw new CityNotKnownException("Destination city is not int list of know cities");
        }
    }
}