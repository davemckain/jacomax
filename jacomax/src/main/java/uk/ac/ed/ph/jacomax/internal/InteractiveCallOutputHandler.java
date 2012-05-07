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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the outputs of each call made with {@link MaximaInteractiveProcessImpl}
 *
 * @author David McKain
 */
public final class InteractiveCallOutputHandler extends InteractiveOutputHandler {

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
    protected void handleDecodedOutputChunk(final CharBuffer charBuffer) throws IOException {
        /* Build up current line so we can check when we're at the required terminator */
        while (charBuffer.hasRemaining()) {
            final char c = charBuffer.get();

            /* NB: On the Windows/GCL platform that I've tested, Maxima terminates lines
             * with a single newline, rather than the platform default.
             *
             * So, the logic follows basically ignores carriage returns (which I've never
             * actually seen output) and uses newlines to indicate the end of a line.
             */
            if (c=='\n') {
                /* See if we have received the required terminator on this line */
                final int terminatorPosition = lastOutputLineBuilder.indexOf(terminator);
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
            else if (c=='\r') {
                continue;
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