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

/**
 * Internal interface for handling chunks of data returned via Maxima's STDOUT.
 *
 * @see InteractiveCallOutputHandler
 * @see InteractiveStartupOutputHandler
 * @see BatchOutputHandler
 *
 * @author David McKain
 */
public interface MaximaOutputHandler {

    /** This is called when a Maxima call is about to commence */
    void callStarting() throws IOException;

    /**
     * This is called when a chunk of output has been read from the Maxima process.
     * Implementations should do whatever is required with this and return true
     * if no more Maxima output is expected, false otherwise.
     *
     * @param maximaOutputBuffer buffer containing bytes read from Maxima. The contents of
     *   this buffer will change once this method has completed. The buffer will
     *   be filled from index zero until index bytesReadFromMaxima
     * @param bytesReadFromMaxima number of bytes read from Maxima, which will be positive
     *   and less than or equal to the size of maximaOutputBuffer.
     * @param isMaximaOutputEof true if this is the EOF of Maxima output, false otherwise.
     */
    boolean handleOutput(byte[] maximaOutputBuffer, int bytesReadFromMaxima, boolean isMaximaOutputEof)
        throws IOException;

    /** This is called when a Maxima call has completed (or failed) */
    void callFinished() throws IOException;

}