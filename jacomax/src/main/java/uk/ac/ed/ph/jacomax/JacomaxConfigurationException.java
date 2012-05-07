/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

/**
 * Runtime Exception thrown to indicate a problem with the configuration of
 * Maxima, such as a bad path or environment.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class JacomaxConfigurationException extends JacomaxRuntimeException {

    private static final long serialVersionUID = 7100573731627419599L;

    public JacomaxConfigurationException(final String message) {
        super(message);
    }

    public JacomaxConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
