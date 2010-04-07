/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.jacomax.pooling;

import uk.ac.ed.ph.jacomax.MaximaInteractiveProcess;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface MaximaInteractiveProcessManager {
    
    MaximaInteractiveProcess obtainProcess();
    
    void returnProcess(MaximaInteractiveProcess process);

}
