package com.olegpro.sc;

public class ScanException extends Exception {
    public ScanException() {
        super();
    }

    public ScanException(String message) {
        super(message);
    }

    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScanException(Throwable cause) {
        super(cause);
    }

    public ScanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
