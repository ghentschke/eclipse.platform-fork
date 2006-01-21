/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.ui.history;

import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.ui.part.Page;

/**
 * Interface to an object which is capable of supplying a history page for display
 * by the history view or other views, dialogs or editors that wish to display
 * the history of an object. 
 *  
 * TODO: There are two ways a history page source is obtained...
 *  
 * <p>
 * This interface is not intended to be implemented by clients.
 * Clients can instead subclass {@link HistoryPage}.
 *  
 * @see IFileHistoryProvider
 * @since 3.2
 */
public interface IHistoryPageSource {
	
	/**
	 * Called by the historyview to create the page for this IFileHistoryProvider. The
	 * page must implement {@link IHistoryPage}.
	 * @param object TODO
	 * 
	 * @see IHistoryPage
	 * @return a Page that implements IHistoryPage (should return either an IPage, IPageBookViewPage or an IHistoryPage
	 */
	public Page createPage(Object object);
}
