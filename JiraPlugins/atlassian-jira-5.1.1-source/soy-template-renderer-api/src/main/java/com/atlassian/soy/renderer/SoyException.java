package com.atlassian.soy.renderer;

/**
 *
 * @since v1.0
 */
public class SoyException extends Exception {
    public SoyException(String message) {
        super(message);
    }

    public SoyException(String message, Throwable cause) {
        super(message, cause);
    }

    public SoyException(Throwable cause) {
        super(cause);
    }
}
