package com.divae.ageto.hybris.utils.maven;

/**
 * @author Klaus Hauschild
 */
class MavenExecutionException extends RuntimeException {

    /** Serial version */
    private static final long serialVersionUID = -5411124737349972605L;

    MavenExecutionException() {
        super();
    }

    MavenExecutionException(final Throwable cause) {
        super(cause);
    }

    MavenExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
