/* Copyright (c) 2010 - 2012, The University of Edinburgh.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 * 
 * * Neither the name of the University of Edinburgh nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.ed.ph.jacomax.utilities;

import uk.ac.ed.ph.jacomax.internal.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides some basic utility methods for handling Maxima outputs.
 * <p>
 * This might become more fully-featured in the future...
 *
 * @author David McKain
 */
public final class MaximaOutputUtilities {

    public static final String DEFAULT_INCHAR = "%i";
    public static final String DEFAULT_OUTCHAR = "%o";

    public static String stripIntermediateInputPrompts(final String rawOutput) {
        Assert.notNull(rawOutput, "rawOutput");
        return stripIntermediateInputPrompts(rawOutput, "%i");
    }

    public static String stripIntermediateInputPrompts(final String rawOutput, final String inchar) {
        Assert.notNull(rawOutput, "rawOutput");
        Assert.notNull(inchar, "inchar");
        return rawOutput.replaceAll("\\(\\Q" + inchar + "\\E\\d+\\)", "");
    }

    /**
     * NOTE: This will go wrong if something gets output that looks like a prompt! Not very
     * much I can do about this...
     *
     * FIXME: Need to test this on Windows and on older versions of Maxima.
     *
     * @param rawOutput
     */
    public static SingleLinearOutput parseSingleLinearOutput(final String rawOutput) {
        Assert.notNull(rawOutput, "rawOutput");
        return parseSingleLinearOutput(rawOutput, DEFAULT_INCHAR, DEFAULT_OUTCHAR);
    }

    public static SingleLinearOutput parseSingleLinearOutput(final String rawOutput, final String inchar, final String outchar) {
        Assert.notNull(rawOutput, "rawOutput");
        Assert.notNull(inchar, "inchar");
        Assert.notNull(outchar, "outchar");

        /* Strip out any intermediate input prompts.
         * (These appear in certain Lisp/OS combinations.) */
        final String withoutInputPrompts = stripIntermediateInputPrompts(rawOutput, inchar);

        /* Now split on output prompt */
        final Pattern extractPattern = Pattern.compile("(?sm)(.*?)(\\(\\Q" + outchar + "\\E\\d+\\))\\s*(.*?)\\s*");
        final Matcher matcher = extractPattern.matcher(withoutInputPrompts);
        if (!matcher.matches()) {
            return null;
        }
        final String output = matcher.group(1);
        final String outputPrompt = matcher.group(2);
        final String result = parseLinearResult(matcher.group(3));
        return new SingleLinearOutput(output, outputPrompt, result);
    }

    public static String parseSingleLinearOutputResult(final String rawOutput) {
        Assert.notNull(rawOutput, "rawOutput");
        final SingleLinearOutput extracted = parseSingleLinearOutput(rawOutput);
        return extracted!=null ? extracted.getResult() : null;
    }

    public static String parseLinearResult(final String rawResult) {
        Assert.notNull(rawResult, "rawResult");
        final StringBuilder resultBuilder = new StringBuilder();
        boolean isInString = false;
        boolean isCompletingBackslash = false;
        char c;
        for (int i=0; i<rawResult.length(); i++) {
            c = rawResult.charAt(i);
            if (isCompletingBackslash) {
                /* Only things I'd expect Maxima to backslash are line terminators and '"' */

                /* (NB: I've only ever seen Maxima output single newlines as line terminators,
                 * even on Windows/GCL, and the following simplistic logic reflects this.
                 * I'm not sure if this behaviour is explicit or an underlying Lisp platform thing,
                 * so there's a chance we may have to fix this on more esoteric platforms.)
                 */
                if (c=='\n') {
                    if (isInString) {
                        /* Literal newline */
                        resultBuilder.append(c);
                    }
                    else {
                        /* Newline used to split lines, so ignore */
                    }
                }
                else {
                    resultBuilder.append(c);
                }
                isCompletingBackslash = false;
            }
            else {
                if (c=='\\') {
                    isCompletingBackslash = true;
                }
                else {
                    resultBuilder.append(c);
                    if (c=='"') {
                        isInString ^= true;
                    }
                }
            }
        }
        return resultBuilder.toString();
    }
}
