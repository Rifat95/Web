package web.exception;

public final class ForbiddenException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ForbiddenException(String cause) {
    super(cause);
  }
}
