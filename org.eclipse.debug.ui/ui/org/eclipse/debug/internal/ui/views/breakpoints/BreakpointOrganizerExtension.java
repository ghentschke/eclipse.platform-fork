/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.IBreakpointOrganizerDelegate;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.osgi.framework.Bundle;

/**
 * A contributed breakpoint organizer.
 */
public class BreakpointOrganizerExtension implements IBreakpointOrganizer {
	
	private IConfigurationElement fElement;
	private IBreakpointOrganizerDelegate fDelegate;
	private ImageDescriptor fDescriptor;
	
	// attributes
	public static final String ATTR_LABEL = "label"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	public static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_ICON = "icon"; //$NON-NLS-1$
    public static final String ATTR_OTHERS_LABEL = "othersLabel"; //$NON-NLS-1$
	
	public BreakpointOrganizerExtension(IConfigurationElement element) {
		fElement = element;
	}
	
	/**
	 * Returns the image descriptor for this organizer.
	 * 
	 * @return image descriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		if (fDescriptor == null) {
			String iconPath = fElement.getAttribute(ATTR_ICON);
			// iconPath may be null because icon is optional
			if (iconPath != null) {
				try {
					Bundle bundle = Platform.getBundle(fElement.getNamespace());
					URL iconURL = bundle.getEntry("/"); //$NON-NLS-1$
					iconURL = new URL(iconURL, iconPath);
					fDescriptor = ImageDescriptor.createFromURL(iconURL);
				} catch (MalformedURLException e) {
					DebugUIPlugin.log(e);
				}
			}
		}
		return fDescriptor;		
	}
	
	/**
	 * Returns this organizer's label.
	 * 
	 * @return this organizer's label
	 */
	public String getLabel() {
		return fElement.getAttribute(ATTR_LABEL);
	}
    
    /**
     * Returns this organizer's identifier.
     * 
     * @return this organizer's identifier
     */
    public String getIdentifier() {
        return fElement.getAttribute(ATTR_ID);
    }
	
	/**
	 * Returns this organizer's delegate, instantiating it if required.
	 * 
	 * @return this organizer's delegate
	 */
	protected IBreakpointOrganizerDelegate getOrganizer() {
		if (fDelegate == null) {
			try {
				fDelegate = (IBreakpointOrganizerDelegate) fElement.createExecutableExtension(ATTR_CLASS);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return fDelegate;
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
        return getOrganizer().getCategories(breakpoint);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        getOrganizer().addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        getOrganizer().removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        getOrganizer().addBreakpoint(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removeBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        getOrganizer().removeBreakpoint(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canAdd(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canAdd(IBreakpoint breakpoint, IAdaptable category) {
        return getOrganizer().canAdd(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canRemove(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canRemove(IBreakpoint breakpoint, IAdaptable category) {
        return getOrganizer().canRemove(breakpoint, category);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#dispose()
     */
    public void dispose() {
        getOrganizer().dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizer#getOthersLabel()
     */
    public String getOthersLabel() {
        String attribute = fElement.getAttribute(ATTR_OTHERS_LABEL);
        if (attribute == null) {
            return DebugUIViewsMessages.getString("OtherBreakpointOrganizer.0"); //$NON-NLS-1$
        }
        return attribute;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories()
     */
    public IAdaptable[] getCategories() {
        return getOrganizer().getCategories();
    }
}
