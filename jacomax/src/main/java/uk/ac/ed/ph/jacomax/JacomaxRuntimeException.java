/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

/**
 * Generic runtime Exception thrown to indicate an unexpected problem
 * encountered when communicating with Maxima.
 * <p>
 * This Exception is unchecked as there's nothing that can reasonably be done
 * to recover from this so ought to bubble right up to a handler near the "top"
 * of your application.
 *
 * @see JacomaxConfigurationException
 *
 * @author  David McKain
 * @version $Revision$
 */
public class JacomaxRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 7100573731627419599L;

    public JacomaxRuntimeException(final String message) {
        super(message);
    }

    public JacomaxRuntimeException(final Throwable cause) {
        super(cause);
    }

    public JacomaxRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
