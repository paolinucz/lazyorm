package it.paolinucz.lazy.orm.exceptions;

public class LazyMapperException extends RuntimeException {
    public LazyMapperException(String message, Exception exc) {

        super(message + ": " + exc.getMessage(), exc);
    }


    public LazyMapperException(String message) {

        super(message);
    }
}
