package com.jonnymatts.jzonbie.persistence;

public class JzonbiePersistenceException extends RuntimeException{

    public JzonbiePersistenceException(String s) {
        super(s);
    }

    public JzonbiePersistenceException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
