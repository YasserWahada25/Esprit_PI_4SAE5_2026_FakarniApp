package tn.SoftCare.User.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("This email is already registered. Sign in or use another email.");
    }

    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
