/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

 
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * Terminates all launches.
 */
public class TerminateAllAction extends AbstractListenerActionDelegate {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction(Object element) throws DebugException {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		MultiStatus ms = new MultiStatus(DebugPlugin.getUniqueIdentifier(), 
			DebugException.REQUEST_FAILED, ActionMessages.getString("TerminateAllAction.Terminate_all_failed_3"), null); //$NON-NLS-1$
		for (int i= 0; i < launches.length; i++) {
			ILaunch launch= launches[i];
			if (!launch.isTerminated()) {
				try {
					launch.terminate();
				} catch (DebugException de) {
					ms.merge(de.getStatus());
				}
			}
		}
		if (!ms.isOK()) {
			throw new DebugException(ms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#isRunInBackground()
	 */
	protected boolean isRunInBackground() {
		return true;
	}
	
	/**
	 * Update the action enablement based on the launches present in
	 * the launch manager. selection is unused and can be <code>null</code>.
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(IAction action, ISelection selection) {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		for (int i= 0; i< launches.length; i++) {
			ILaunch launch= launches[i];
			if (!launch.isTerminated()) {
				action.setEnabled(true);
				return;
			}
		}
		action.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.AbstractListenerActionDelegate#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		switch (event.getKind()) {
			case DebugEvent.TERMINATE :
				update(getAction(), null);
				break;
			case DebugEvent.CREATE :
				update(getAction(), null);
				break;
		}
	}		
}
