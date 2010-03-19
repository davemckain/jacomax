/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.internal;

import uk.ac.ed.ph.jacomax.JacomaxLogicException;
import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;
import uk.ac.ed.ph.jacomax.MaximaProcessTerminatedException;
import uk.ac.ed.ph.jacomax.MaximaTimeoutException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
 * @author  David McKain
 * @version $Revision$
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
    
    public MaximaInteractiveProcessImpl(MaximaProcessController maximaProcessController, int defaultCallTimeout, Charset charset) {
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
    
    public void setDefaultCallTimeout(int defaultCallTimeout) {
        this.defaultCallTimeout = defaultCallTimeout;
    }
    
    public void advanceToFirstInputPrompt() {
        logger.debug("Reading Maxima output and first input prompt");
        InteractiveStartupOutputHandler outputHandler = new InteractiveStartupOutputHandler(decodingByteBuffer, decodingCharBuffer, maximaOutputDecoder);
        try {
            maximaProcessController.doMaximaCall(null, false, outputHandler, 0);
        }
        catch (MaximaTimeoutException e) {
            throw new JacomaxLogicException("Unexpected Exception waiting for first input prompt", e);
        }
    }
    
    public String executeCall(String callInput)
            throws MaximaTimeoutException {
        return executeCall(callInput, defaultCallTimeout);
    }
    
    public String executeCall(String callInput, int callTimeout)
            throws MaximaTimeoutException {
        logger.info("executeCall(input={}, timeout={})", callInput, callTimeout);
        ConstraintUtilities.ensureNotNull(callInput, "maximaInput");
        ensureNotTerminated();

        /* Build Maxima input for this call, which includes some trickery to work out when to
         * stop reading call output.
         */
        String maximaInput = createMaximaInput(callInput);
        logger.debug("Sending input '{}' to Maxima and reading output the prompt after terminator line '{}'", maximaInput, CALL_TERMINATOR_OUTPUT);
        StringBuilder outputBuilder = new StringBuilder();
        InteractiveCallOutputHandler outputHandler = new InteractiveCallOutputHandler(outputBuilder, CALL_TERMINATOR_OUTPUT, decodingByteBuffer, decodingCharBuffer, maximaOutputDecoder);
        InputStream inputStream = new ByteArrayInputStream(maximaInput.getBytes(charset));
        
        maximaProcessController.doMaximaCall(inputStream, false, outputHandler, callTimeout);
        String rawOutput = outputBuilder.toString();
        
        logger.info("executeCall() => {}", rawOutput);
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
    private String createMaximaInput(String callInput) {
        /* Trim off trailing whitespace so that we can work out what command terminator is being used */
        String input = callInput.replaceFirst("\\s+$", "");
        char lastChar = input.charAt(input.length() - 1);
        if (lastChar==';' || lastChar=='$') {
            /* Looks like a standard Maxima call, so we just append the generator code */
            return input + " " + CALL_TERMINATOR_GENERATOR;
        }
        else if (lastChar==')' && input.contains(":lisp")) {
            /* Looks like we went into Lisp mode, so terminator code needs to go on a new line */
            return input + "\n" + CALL_TERMINATOR_GENERATOR;
        }
        else {
            throw new IllegalArgumentException("The Maxima call input '" + callInput 
                    + "' does not end with ';' or '$', nor look like a Lisp call, so probably will not work");
        }
    }
    
    public void executeCallDiscardOutput(String callInput)
            throws MaximaTimeoutException {
        executeCallDiscardOutput(callInput, defaultCallTimeout);
    }
    
    public void executeCallDiscardOutput(String callInput, int callTimeout)
            throws MaximaTimeoutException {
        logger.info("executeCallDiscardOutput(input={}, timeout={})", callInput, callTimeout);
        ConstraintUtilities.ensureNotNull(callInput, "maximaInput");
        ensureNotTerminated();

        /* (This is similar to executeCall(), but slightly simpler as we're not bothered with the output here */
        String maximaInput = createMaximaInput(callInput);
        logger.debug("Sending input '{}' to Maxima and discarding output until the prompt after terminator line '{}'", maximaInput, CALL_TERMINATOR_OUTPUT);
        InteractiveCallOutputHandler outputHandler = new InteractiveCallOutputHandler(null, CALL_TERMINATOR_OUTPUT, decodingByteBuffer, decodingCharBuffer, maximaOutputDecoder);
        InputStream inputStream = new ByteArrayInputStream(maximaInput.getBytes(charset));
        maximaProcessController.doMaximaCall(inputStream, false, outputHandler, callTimeout);
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
