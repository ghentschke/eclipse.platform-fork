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
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.views.memory.renderings.ErrorRendering;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabItem;

/**
 * Represents a tab in the Memory View.  This is where memory renderings
 * are hosted in the Memory View.
 * @since 3.1
 */
public class MemoryViewTab implements IMemoryViewTab, IPropertyChangeListener, Listener {

	private IMemoryRendering fRendering;
	private TabItem fTabItem;
	private DisposeListener fDisposeListener;
	private boolean fEnabled;
	private boolean fIsDisposed = false;
	private Control fControl;
	private RenderingViewPane fContainer;
	
	
	public MemoryViewTab(TabItem tabItem, IMemoryRendering rendering, RenderingViewPane container)
	{
		fTabItem = tabItem;
		fRendering = rendering;
		fContainer = container;
		
		// set the rendering as the synchronization provider
		// as the new rendering should be in focus and have control 
		// after it's created
		
		container.getMemoryRenderingSite().setSynchronizationProvider(rendering);
		Control control = createViewTab();
		
		control.addListener(SWT.Activate, this);
		control.addListener(SWT.Deactivate, this);
		
		// activate rendering upon creations
		fRendering.activated();
		
		fTabItem.setControl(control);
		fTabItem.setData(this);
		fTabItem.setText(getLabel());
		fTabItem.setImage(getImage());
		
		fTabItem.addDisposeListener(fDisposeListener = new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				fTabItem.removeDisposeListener(fDisposeListener);
				dispose();
			}});
	}
	
	private Control createViewTab()
	{   
		ISafeRunnable safeRunnable = new ISafeRunnable() {

			public void handleException(Throwable exception) {
				// create an error rendering to fill the view tab
				ErrorRendering rendering = new ErrorRendering(fRendering.getRenderingId(), exception);
				rendering.init(fContainer, fRendering.getMemoryBlock());

				// dispose the rendering
				fRendering.dispose();
				
				fRendering = rendering;
				fControl = rendering.createControl(fTabItem.getParent());
			}

			public void run() throws Exception {
				fControl = fRendering.createControl(fTabItem.getParent());
				fRendering.addPropertyChangeListener(getInstance());
			}};
			
		Platform.run(safeRunnable);
		return fControl;
	}
	
	private String getLabel()
	{
		return fRendering.getLabel();
	}
	
	private Image getImage()
	{
		return fRendering.getImage();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#dispose()
	 */
	public void dispose() {
		fIsDisposed = true;
		
		fRendering.removePropertyChangeListener(this);
		fControl.removeListener(SWT.Activate, this);
		fControl.removeListener(SWT.Deactivate, this);
		
		// always deactivate rendering before disposing it.
		fRendering.deactivated();
		
		fRendering.dispose();
	}
	
	public boolean isDisposed()
	{
		return fIsDisposed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#isEnabled()
	 */
	public boolean isEnabled() {
		return fEnabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
		
		if (fEnabled)
			fRendering.becomesVisible();
		else
			fRendering.becomesHidden();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#setTabLabel(java.lang.String)
	 */
	public void setTabLabel(String label) {
		fTabItem.setText(label);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getTabLabel()
	 */
	public String getTabLabel() {
		return fTabItem.getText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getRendering()
	 */
	public IMemoryRendering getRendering() {
		return fRendering;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == fRendering)
		{
			if (event.getProperty().equals(IBasicPropertyConstants.P_TEXT))
			{
				Object value = event.getNewValue();
				if (value != null && value instanceof String)
				{
					String label = (String)value;
					setTabLabel(label);
				}
				else
				{
					setTabLabel(fRendering.getLabel());
				}
			}
			
			if (event.getProperty().equals(IBasicPropertyConstants.P_IMAGE))
			{
				Object value = event.getNewValue();
				if (value != null && value instanceof Image)
				{
					Image image = (Image)value;
					fTabItem.setImage(image);
				}
				else
				{
					fTabItem.setImage(fRendering.getImage());
				}
			}
		}
	}
	
	private MemoryViewTab getInstance()
	{
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		if (event.type == SWT.Activate)
		{
			fRendering.activated();
			fContainer.setRenderingSelection(fRendering);
		}
		if (event.type == SWT.Deactivate)
		{
			fRendering.deactivated();
		}
	}
}
