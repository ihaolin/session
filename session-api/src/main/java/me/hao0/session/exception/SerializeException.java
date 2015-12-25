package me.hao0.session.exception;

public class SerializeException extends RuntimeException {

    public SerializeException() {}

    public SerializeException(String s) {
        super(s);
    }

    public SerializeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SerializeException(Throwable throwable) {
        super(throwable);
    }
}
