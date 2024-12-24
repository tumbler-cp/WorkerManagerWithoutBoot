package lab.arahnik.exception;

public class InsufficientEditingRightsException extends RuntimeException {
  public InsufficientEditingRightsException(String message) {
    super(message);
  }
}
