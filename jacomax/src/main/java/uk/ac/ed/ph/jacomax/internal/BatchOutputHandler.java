/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.internal;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handler used when running Maxima in batch mode.
 *
 * @see MaximaBatchProcessImpl
 *
 * @author  David McKain
 * @version $Revision$
 */
public class BatchOutputHandler implements MaximaOutputHandler {

    private final OutputStream batchOutputStream;

    public BatchOutputHandler(final OutputStream outputStream) {
        this.batchOutputStream = outputStream;
    }

    public void callStarting() {
        /* (Nothing to do) */
    }

    public boolean handleOutput(final byte[] maximaOutputBuffer, final int bytesReadFromMaxima, final boolean outputFinished) throws IOException {
        batchOutputStream.write(maximaOutputBuffer, 0, bytesReadFromMaxima);
        return outputFinished;
    }

    public void callFinished() throws IOException {
        batchOutputStream.close();
    }
}