/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.patch;

import java.io.Reader;

import org.eclipse.core.runtime.CoreException;

/**
 * Abstract class for creating readers.
 * 
 * @since org.eclipse.compare.core 1.0.0
 */
public abstract class ReaderCreator {

	/**
	 * Creates new reader. The caller is responsible for closing the reader when
	 * finished.
	 * 
	 * @return a reader
	 * @exception CoreException
	 *                if the reader can't be created
	 */
	public abstract Reader createReader() throws CoreException;
}
