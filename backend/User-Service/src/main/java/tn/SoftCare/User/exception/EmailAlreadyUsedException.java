package tn.SoftCare.User.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("Email déjà utilisé");
    }

    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
