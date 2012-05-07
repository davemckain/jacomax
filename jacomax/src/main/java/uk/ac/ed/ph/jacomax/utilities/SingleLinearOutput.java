/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.utilities;

import java.io.Serializable;

/**
 * Encapsulates all of the components from a single Maxima call generating linear output,
 * such as <tt>string(...);</tt>
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SingleLinearOutput implements Serializable {

    private static final long serialVersionUID = 710345629577430456L;

    private final String output;
    private final String outputPrompt;
    private final String result;

    public SingleLinearOutput(final String output, final String outputPrompt, final String result) {
        this.output = output;
        this.outputPrompt = outputPrompt;
        this.result = result;
    }

    public String getOutput() {
        return output;
    }

    public String getOutputPrompt() {
        return outputPrompt;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this))
            + "(output=" + output
            + ",outputPrompt=" + outputPrompt
            + ",result=" + result
            + ")";
    }
}
