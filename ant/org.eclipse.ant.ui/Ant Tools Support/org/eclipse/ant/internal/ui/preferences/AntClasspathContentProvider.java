/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.preferences;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Content provider that maintains a list of URLs which are shown in a table
 * viewer.
 */
public class AntClasspathContentProvider extends AntContentProvider {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AntContentProvider#add(java.lang.Object)
	 */
	public void add(Object o) {
		if (o instanceof URL) {
			URL newURL = (URL) o;
			File newFile= new File(newURL.getFile());
			Iterator itr = elements.iterator();
			File existingFile;
			while (itr.hasNext()) {
				URL url = (URL) itr.next();
				existingFile= new File(url.getFile());
				if (existingFile.equals(newFile)) {
					return;
				}
			}
			elements.add(o);
			tableViewer.add(o);
			tableViewer.setSelection(new StructuredSelection(o), true);
		} else {
			super.add(o);
		}
	}

	public void removeAll() {
		if (tableViewer != null) {
			tableViewer.remove(elements.toArray());
		}
		elements = new ArrayList(5);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.AntContentProvider#getSorter()
	 */
	protected ViewerSorter getSorter() {
		return null;
	}
}
