/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax;

/**
 * This (unchecked) Exception is thrown if you attempt to execute a Maxima call using
 * {@link MaximaInteractiveProcess} after the process has been terminated.
 * <p>
 * You should normally ensure this never happens by using your {@link MaximaInteractiveProcess}
 * correctly.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MaximaProcessTerminatedException extends IllegalStateException {

    private static final long serialVersionUID = -3471501531982835288L;

    public MaximaProcessTerminatedException() {
        super("Maxima process has been terminated");
    }
}
