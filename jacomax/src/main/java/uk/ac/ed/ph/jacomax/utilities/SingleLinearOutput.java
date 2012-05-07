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
public class SingleLinearOutput implements Serializable {

    private static final long serialVersionUID = 710345629577430456L;

    private String output;
    private String outputPrompt;
    private String result;


    public String getOutput() {
        return output;
    }

    public void setOutput(final String output) {
        this.output = output;
    }


    public String getOutputPrompt() {
        return outputPrompt;
    }

    public void setOutputPrompt(final String outputPrompt) {
        this.outputPrompt = outputPrompt;
    }


    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "(output=" + output
            + ",outputPrompt=" + outputPrompt
            + ",result=" + result
            + ")";
    }
}
