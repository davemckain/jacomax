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
package uk.ac.ed.ph.jacomax.internal;

import uk.ac.ed.ph.jacomax.JacomaxLogicException;
import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessTerminatedException;
import uk.ac.ed.ph.jacomax.MaximaTimeoutException;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the internal implementation of {@link MaximaInteractiveProcess}.
 *
 * @author David McKain
 */
public final class MaximaInteractiveProcessImpl implements MaximaInteractiveProcess {

    private static final Logger logger = LoggerFactory.getLogger(MaximaInteractiveProcessImpl.class);

    private static final String CALL_TERMINATOR_OUTPUT = "JACOMAX-INTERACTIVE-CALL-OUTPUT-TERMINATOR";
    private static final String CALL_TERMINATOR_GENERATOR = "block(kill(1), print(\"" + CALL_TERMINATOR_OUTPUT + "\"))$" + System.getProperty("line.separator");

    private final MaximaProcessController maximaProcessController;
    private int defaultCallTimeout;
    private final Charset charset;
    private final CharsetDecoder maximaOutputDecoder;
    private final ByteBuffer decodingByteBuffer;
    private final CharBuffer decodingCharBuffer;

    public MaximaInteractiveProcessImpl(final MaximaProcessController maximaProcessController, final int defaultCallTimeout, final Charset charset) {
        this.maximaProcessController = maximaProcessController;
        this.defaultCallTimeout = defaultCallTimeout;
        this.charset = charset;
        this.decodingByteBuffer = ByteBuffer.allocate(MaximaProcessController.OUTPUT_BUFFER_SIZE);
        this.decodingCharBuffer = CharBuffer.allocate(MaximaProcessController.OUTPUT_BUFFER_SIZE);
        this.maximaOutputDecoder = charset.newDecoder();
        this.maximaOutputDecoder.onMalformedInput(CodingErrorAction.REPORT);
        this.maximaOutputDecoder.onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    public int getDefaultCallTimeout() {
        return defaultCallTimeout;
    }

    public void setDefaultCallTimeout(final int defaultCallTimeout) {
        this.defaultCallTimeout = defaultCallTimeout;
    }

    public void advanceToFirstInputPrompt() {
        logger.trace("Reading Maxima output and first input prompt");
        final InteractiveStartupOutputHandler outputHandler = new InteractiveStartupOutputHandler(decodingByteBuffer, decodingCharBuffer, maximaOutputDecoder);
        try {
            maximaProcessController.doMaximaCall(null, false, outputHandler, 0);
        }
        catch (final MaximaTimeoutException e) {
            throw new JacomaxLogicException("Unexpected Exception waiting for first input prompt", e);
        }
    }

    public String executeCall(final String callInput)
            throws MaximaTimeoutException {
        return executeCall(callInput, defaultCallTimeout);
    }

    public String executeCall(final String callInput, final int callTimeout)
            throws MaximaTimeoutException {
        logger.debug("executeCall(input={}, timeout={})", callInput, callTimeout);
        Assert.notNull(callInput, "maximaInput");
        ensureNotTerminated();

        /* Build Maxima input for this call, which includes some trickery to work out when to
         * stop reading call output.
         */
        final String maximaInput = createMaximaInput(callInput);
        logger.trace("Sending input '{}' to Maxima and reading output the prompt after terminator line '{}'", maximaInput, CALL_TERMINATOR_OUTPUT);
        final StringBuilder outputBuilder = new StringBuilder();
        final InteractiveCallOutputHandler outputHandler = new InteractiveCallOutputHandler(outputBuilder, CALL_TERMINATOR_OUTPUT, decodingByteBuffer, decodingCharBuffer, maximaOutputDecoder);
        maximaProcessController.doMaximaCall(encodeInput(maximaInput), false, outputHandler, callTimeout);
        final String rawOutput = outputBuilder.toString();

        logger.debug("executeCall() => {}", rawOutput);
        return rawOutput;
    }

    /**
     * Builds the actual Maxima input required to execute a particular call.
     * <p>
     * This includes some trickery to append a {@link #CALL_TERMINATOR_OUTPUT} String
     * so that we can work out when Maxima has finished evaluating the call. Some care
     * is required to get this correct, which will no doubt restrict exactly what calls
     * work.
     */
    private String createMaximaInput(final String callInput) {
        /* Trim off trailing whitespace so that we can work out what command terminator is being used */
        final String input = callInput.replaceFirst("\\s+$", "");
        final char lastChar = input.charAt(input.length() - 1);
        if (lastChar==';' || lastChar=='$' || (lastChar==')' && input.contains(":lisp"))) {
            /* Looks like a standard Maxima call, or a Lisp call.
             *
             * Note that in all cases, we append the CALL_TERMINATOR_GENERATOR on a separate line,
             * even though it would only seem necessary when doing Lisp commands. The reason for
             * this is the Windows/GCL Maxima will not execute the CALL_TERMINATOR_GENERATOR
             * if an earlier command on the same input line did not succeed, which results in a timeout
             * in these cases.
             */
            return input + System.getProperty("line.separator") + CALL_TERMINATOR_GENERATOR;
        }
        throw new IllegalArgumentException("The Maxima call input '" + callInput
                + "' does not end with ';' or '$', nor look like a Lisp call, so probably will not work");
    }

    public void executeCallDiscardOutput(final String callInput)
            throws MaximaTimeoutException {
        executeCallDiscardOutput(callInput, defaultCallTimeout);
    }

    public void executeCallDiscardOutput(final String callInput, final int callTimeout)
            throws MaximaTimeoutException {
        logger.debug("executeCallDiscardOutput(input={}, timeout={})", callInput, callTimeout);
        Assert.notNull(callInput, "maximaInput");
        ensureNotTerminated();

        /* (This is similar to executeCall(), but slightly simpler as we're not bothered with the output here */
        final String maximaInput = createMaximaInput(callInput);
        logger.trace("Sending input '{}' to Maxima and discarding output until the prompt after terminator line '{}'", maximaInput, CALL_TERMINATOR_OUTPUT);
        final InteractiveCallOutputHandler outputHandler = new InteractiveCallOutputHandler(null, CALL_TERMINATOR_OUTPUT, decodingByteBuffer, decodingCharBuffer, maximaOutputDecoder);
        maximaProcessController.doMaximaCall(encodeInput(maximaInput), false, outputHandler, callTimeout);
    }

    private ByteArrayInputStream encodeInput(final String maximaInput) {
        ByteArrayInputStream result;
        /* (For Java 1.5 compatibility, we have to go round the houses a bit) */
        try {
            result = new ByteArrayInputStream(maximaInput.getBytes(charset.name()));
        }
        catch (final UnsupportedEncodingException e) {
            /* Shouldn't happen as we have already verified charset */
            throw new JacomaxLogicException("Unexpected Exception - charset should have been verified already", e);
        }
        return result;
    }

    public void softReset() throws MaximaTimeoutException {
        executeCallDiscardOutput("[kill(all),reset()]$");
    }

    public int terminate() {
        return maximaProcessController.terminate();
    }

    public boolean isTerminated() {
        return maximaProcessController.isTerminated();
    }

    private void ensureNotTerminated() {
        if (isTerminated()) {
            throw new MaximaProcessTerminatedException();
        }
    }
}
