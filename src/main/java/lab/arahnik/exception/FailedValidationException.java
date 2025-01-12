package lab.arahnik.exception;

public class FailedValidationException extends RuntimeException {
  public FailedValidationException(String message) {
    super(message);
  }
}
