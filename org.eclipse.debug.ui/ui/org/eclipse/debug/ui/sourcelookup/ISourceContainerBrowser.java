/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.sourcelookup;

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.swt.widgets.Shell;


/**
 * Creates and edits source containers for a source lookup director.
 * Contributed via a source container presentation extension for
 * a specific type of source container.
 * <p>
 * Following is an example source container presentation definition
 * that contributes a source container browser via the
 * <code>browserClass</code> attribute.
 * <pre>
 * &lt;extension point=&quot;org.eclipse.debug.ui.sourceContainerPresentations&quot;&gt;
 *    &lt;sourceContainerPresentation
 *          browserClass=&quot;org.eclipse.debug.internal.ui.sourcelookup.browsers.ProjectSourceContainerBrowser&quot;
 *          containerTypeID=&quot;org.eclipse.debug.core.containerType.project&quot;
 *          icon=&quot;icons/full/obj16/prj_obj.gif&quot;
 *          id=&quot;org.eclipse.debug.ui.containerPresentation.project&quot;&gt;
 *    &lt;/sourceContainerPresentation&gt;
 * &lt;/extension&gt;
 * </pre>
 * </p>
 * @since 3.0
 */
public interface ISourceContainerBrowser {
	/**
	 * Creates and returns new source containers to add to the given
	 * source lookup director.
	 * 
	 * @param shell the shell to use to parent any dialogs
	 * @param director the director the new containers will be added to
	 * @return the new source containers to add
	 */
	public ISourceContainer[] addSourceContainers(Shell shell, ISourceLookupDirector director);
	
	/**
	 * Returns whether this browser can add any source containers to the
	 * given source lookup director.
	 * 
	 * @param director source lookup director to potentially add source
	 *  containers to
	 * @return whether this browser can add any source containers to the
	 * given source lookup director
	 */
	public boolean canAddSourceContainers(ISourceLookupDirector director);
	
	/**
	 * Edits and returns source containers to replace the given source
	 * containers.
	 * 
	 * @param shell the shell to use to parent any dialogs
	 * @param director the director the new containers will be added to
	 * @param containers the source containers to be edited
	 * @return the replacement source containers
	 */
	public ISourceContainer[] editSourceContainers(Shell shell, ISourceLookupDirector director, ISourceContainer[] containers);
	
	/**
	 * Returns whether this browser can edit the given source containers.
	 * 
	 * @param director source lookup director to potentially edit source
	 *  containers for
	 * @param containers the containers to edit
	 * @return whether this browser can edit the given source containers
	 */
	public boolean canEditSourceContainers(ISourceLookupDirector director, ISourceContainer[] containers);	
	
}
