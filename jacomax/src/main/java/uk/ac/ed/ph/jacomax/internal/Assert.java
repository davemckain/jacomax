/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.internal;

/**
 * Simple utility methods for enforcing various types of data constraints.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class Assert {

    /**
     * Checks that the given object is non-null, throwing an
     * IllegalArgumentException if the check fails. If the check succeeds then
     * nothing happens.
     *
     * @param value object to test
     * @param objectName name to give to supplied Object when constructing Exception message.
     *
     * @throws IllegalArgumentException if <tt>value</tt> is null.
     */
    public static void notNull(final Object value, final String objectName) {
        if (value==null) {
            throw new IllegalArgumentException(objectName + " must not be null");
        }
    }
}
