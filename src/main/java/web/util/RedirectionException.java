package web.util;

public final class RedirectionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RedirectionException(String location) {
		super(location);
	}
}
