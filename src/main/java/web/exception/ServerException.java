package web.exception;

public final class ServerException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ServerException(Exception e) {
    super(e.getMessage());
    setStackTrace(e.getStackTrace());
  }
}
