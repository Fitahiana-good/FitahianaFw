package Fitahianafw.err;

public class ParameterBindingException extends IllegalArgumentException {
    public ParameterBindingException(String message) {
        super(message);
    }

    public ParameterBindingException(String message, Throwable cause) {
        super(message, cause);
    }
}
