/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.memory;

import org.eclipse.ui.IViewSite;

/**
 * A workbench site that hosts memory renderings and provides synchronization services
 * for memory renderings.
 * <p>
 * A rendering site has an optional synchronization provider at any one time. If a
 * rendering provides synchronization information it should set itself as the synchronization
 * prodiver for its memory rendering site when it is activated. 
 * </p>
 * 
 * @since 3.1
 */
public interface IMemoryRenderingSite {

    /**
     * Returns the view site hosting memory renderings for this rendering site.
     * 
     * @return the view site hosting memory renderings for this rendering site
     */
    public IViewSite getViewSite();
        
    /**
     * Returns the syncrhonization serivce for this rendering site.
     * 
     * @return the syncrhonization serivce for this rendering site
     */
    public IMemoryRenderingSynchronizationService getSynchronizationService();
    
    /**
     * Sets the rendering currently providing sychronization information for
     * this rendering site, or <code>null</code> if none.
     * 
     * @param rendering active rendering providing synchronization information or
     *  <code>null</code>
     */
    public void setSynchronizationProvider(IMemoryRendering rendering);
    
    /**
     * Returns the rendering currengly providing synchronization information for
     * this rendering site, or <code>null</code if none.
     * @return rendering providing synchronization information or <code>null</null>
     */
    public IMemoryRendering getSynchronizationProvider();
    
    /**
     * @return all the memory rendering containers within this rendering site.
     */
    public IMemoryRenderingContainer[] getMemoryRenderingContainers();
    
    /**
     * Returns the rendering container with teh given id.
     *
     * @param id
     * @return the rendering container with the given id.  Returns null
     * if the container cannot be found.
     */
    public IMemoryRenderingContainer getContainer(String id);
    
    
}
