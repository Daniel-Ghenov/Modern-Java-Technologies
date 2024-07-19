package bg.sofia.uni.fmi.mjt.cooking.client.model;

public class ClientException extends RuntimeException {


	private String property;

	public ClientException(ClientExceptionParams params) {
		super(params.error());
		this.property = params.property();
	}

	public String getProperty() {
		return property;
	}

}
