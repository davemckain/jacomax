/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the outputs of each call made with {@link MaximaInteractiveProcessImpl}
 *
 * @author  David McKain
 * @version $Revision$
 */
public class InteractiveCallOutputHandler extends InteractiveOutputHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(InteractiveCallOutputHandler.class);
    
    private final Appendable outputBuilder;
    
    private final String terminator;
    
    private final StringBuilder lastOutputLineBuilder;
    
    /** This flag gets set at the end of the line containing the required terminator */
    private boolean lineContainingTerminatorEnded;
    
    public InteractiveCallOutputHandler(final Appendable outputBuilder,
            final String terminator, final ByteBuffer decodingByteBuffer,
            final CharBuffer decodingCharBuffer, final CharsetDecoder charsetDecoder) {
        super(decodingByteBuffer, decodingCharBuffer, charsetDecoder);
        this.lastOutputLineBuilder = new StringBuilder();
        this.outputBuilder = outputBuilder;
        this.terminator = terminator;
    }
    
    @Override
    public void callStarting() {
        super.callStarting();
        lastOutputLineBuilder.setLength(0);
        lineContainingTerminatorEnded = false;
    }
    
    @Override
    protected void handleDecodedOutputChunk(CharBuffer charBuffer) throws IOException {
        /* Build up current line so we can check when we're at the required terminator */
        while (charBuffer.hasRemaining()) {
            char c = charBuffer.get();
            if (c=='\n' || c=='\r') {
                /* See if we have received the required terminator on this line */
                int terminatorPosition = lastOutputLineBuilder.indexOf(terminator);
                if (terminatorPosition != -1) {
                    /* Found required input prompt, so terminate */
                    logger.trace("Found terminator; will stop reading on next line, which will be input prompt");
                    lineContainingTerminatorEnded = true;
                    
                    /* (Record anything that came just before the terminator) */
                    if (outputBuilder!=null && terminatorPosition > 0) {
                        outputBuilder.append(lastOutputLineBuilder, 0, terminatorPosition);
                    }
                }
                else if (outputBuilder!=null) {
                    /* Add line just read to output (if being built) */
                    outputBuilder.append(lastOutputLineBuilder).append(c);
                }
                /* Reset for reading next line in */
                lastOutputLineBuilder.setLength(0);
            }
            else {
                lastOutputLineBuilder.append(c);
            }
        }
    }
    
    @Override
    public boolean isNextInputPromptReached() {
        return lineContainingTerminatorEnded && lastOutputLineBuilder.toString().endsWith(") ");
    }
}