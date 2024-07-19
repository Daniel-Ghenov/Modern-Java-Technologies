package bg.sofia.uni.fmi.mjt.itinerary.exception;

public class CityNotKnownException extends Exception {
    public CityNotKnownException() {
        super();
    }

    public CityNotKnownException(String message) {
        super(message);
    }
}