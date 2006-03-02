/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.util.List;

/**
 * Search hit collector. The search engine adds hits to it.
 */
public interface ISearchHitCollector {
	
	/**
	 * Adds hits to the result.
	 * 
	 * @param hits the List of raw hits
	 */
	public void addHits(List hits, String wordsSearched);
}
