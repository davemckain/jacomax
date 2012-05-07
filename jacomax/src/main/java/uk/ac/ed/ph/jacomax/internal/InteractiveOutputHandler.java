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
import java.nio.charset.CoderResult;

/**
 * Base handler used by {@link MaximaInteractiveProcessImpl}
 *
 * @author David McKain
 */
public abstract class InteractiveOutputHandler implements MaximaOutputHandler {

    private final CharsetDecoder maximaOutputDecoder;
    private final ByteBuffer decodingByteBuffer;
    private final CharBuffer decodingCharBuffer;

    public InteractiveOutputHandler(final ByteBuffer decodingByteBuffer,
            final CharBuffer decodingCharBuffer, final CharsetDecoder charsetDecoder) {
        this.decodingByteBuffer = decodingByteBuffer;
        this.decodingCharBuffer = decodingCharBuffer;
        this.maximaOutputDecoder = charsetDecoder;
    }

    public void callStarting() {
        decodingByteBuffer.clear();
        decodingCharBuffer.clear();
        maximaOutputDecoder.reset();
    }

    public boolean handleOutput(final byte[] maximaOutputBuffer, final int bytesReadFromMaxima, final boolean isMaximaOutputEof)
            throws IOException {
        int stdoutBufferPos = 0;
        int stdoutBufferRemaining = bytesReadFromMaxima;
        int outputChunkSize;

        /* Iterate over input, filling lineByteBuffer as much as possible each time */
        while (stdoutBufferPos < bytesReadFromMaxima) {
            outputChunkSize = Math.min(decodingByteBuffer.remaining(), stdoutBufferRemaining);
            decodingByteBuffer.put(maximaOutputBuffer, stdoutBufferPos, outputChunkSize);
            stdoutBufferPos += outputChunkSize;
            stdoutBufferRemaining -= outputChunkSize;

            decodeByteBuffer(false);
        }
        final boolean inputPromptReached = isNextInputPromptReached();
        if (isMaximaOutputEof && !inputPromptReached) {
            throw new IllegalStateException("Maxima output ended before next input prompt");
        }
        return inputPromptReached;
    }

    private void decodeByteBuffer(final boolean endOfInput) throws IOException {
        CoderResult coderResult;
        while (true) {
            decodingByteBuffer.flip();
            coderResult = maximaOutputDecoder.decode(decodingByteBuffer, decodingCharBuffer, endOfInput);
            if (coderResult.isError()) {
                coderResult.throwException();
            }
            /* Handle decoded characters */
            decodingCharBuffer.flip();
            handleDecodedOutputChunk(decodingCharBuffer);
            decodingCharBuffer.clear();

            /* Compact any unencoded bytes from end of buffer */
            decodingByteBuffer.compact();
            if (coderResult.isUnderflow()) {
                /* We need more bytes */
                break;
            }
        }
    }

    public void callFinished() throws IOException {
        finishDecoding();
    }

    private void finishDecoding() throws IOException {
        decodeByteBuffer(true);
    }

    protected abstract void handleDecodedOutputChunk(CharBuffer buffer) throws IOException;

    protected abstract boolean isNextInputPromptReached();
}