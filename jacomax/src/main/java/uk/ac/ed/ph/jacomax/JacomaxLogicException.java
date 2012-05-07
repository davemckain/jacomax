/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

/**
 * Runtime Exception thrown if a logic problem occurs in the Maxima Connector code, indicating
 * a bug within this code that will need looked at and fixed!
 *
 * @author  David McKain
 * @version $Revision$
 */
public class JacomaxLogicException extends JacomaxRuntimeException {

    private static final long serialVersionUID = 7100573731627419599L;

    public JacomaxLogicException(final String message) {
        super(message);
    }

    public JacomaxLogicException(final Throwable cause) {
        super(cause);
    }

    public JacomaxLogicException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static JacomaxLogicException unexpectedException(final Throwable cause) {
        return new JacomaxLogicException("Unexpected Exception", cause);
    }
}
