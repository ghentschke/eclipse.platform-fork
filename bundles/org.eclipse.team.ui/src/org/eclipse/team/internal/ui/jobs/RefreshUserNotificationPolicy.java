package org.eclipse.team.internal.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.IRefreshEvent;
import org.eclipse.team.internal.ui.synchronize.IRefreshSubscriberListener;
import org.eclipse.team.internal.ui.synchronize.RefreshCompleteDialog;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class RefreshUserNotificationPolicy implements IRefreshSubscriberListener {

	private SubscriberParticipant participant;
	private boolean addIfNeeded;

	public RefreshUserNotificationPolicy(SubscriberParticipant participant, boolean addIfNeeded) {
		this.participant = participant;
		this.addIfNeeded = addIfNeeded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.jobs.IRefreshSubscriberListener#refreshStarted(org.eclipse.team.internal.ui.jobs.IRefreshEvent)
	 */
	public void refreshStarted(IRefreshEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.jobs.IRefreshSubscriberListener#refreshDone(org.eclipse.team.internal.ui.jobs.IRefreshEvent)
	 */
	public void refreshDone(IRefreshEvent event) {
		if(event.getSubscriber() != participant.getSubscriberSyncInfoCollector().getSubscriber()) return;
		
		int type = event.getRefreshType();

		boolean promptWithChanges = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_PROMPT_WITH_CHANGES);
		boolean promptWhenNoChanges = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_PROMPT_WHEN_NO_CHANGES);
		boolean promptWithChangesBkg = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_BKG_PROMPT_WITH_CHANGES);
		boolean promptWhenNoChangesBkg = TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_VIEW_BKG_PROMPT_WHEN_NO_CHANGES);

		boolean shouldPrompt = false;
		SyncInfo[] infos = event.getChanges();

		if (type == IRefreshEvent.USER_REFRESH) {
			if (promptWhenNoChanges && infos.length == 0) {
				shouldPrompt = true;
			} else if (promptWithChanges && infos.length > 0) {
				shouldPrompt = true;
			}
		} else {
			if (promptWhenNoChangesBkg && infos.length == 0) {
				shouldPrompt = true;
			} else if (promptWithChangesBkg && infos.length > 0) {
				shouldPrompt = true;
			}
		}

		// If there are interesting changes, ensure the sync view is showing them
		// Also, add the participant to the sync view only if changes have been found.
		if (infos.length > 0) {
			participant.setMode(SubscriberParticipant.INCOMING_MODE);
			final ISynchronizeManager manager = TeamUI.getSynchronizeManager();
			if (addIfNeeded) {
				manager.addSynchronizeParticipants(new ISynchronizeParticipant[]{participant});
				TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						ISynchronizeView view = manager.showSynchronizeViewInActivePage(null);
						if (view != null) {
							view.display(participant);
						}
					}
				});
			}
		}
		
		// Prompt user if preferences are set for this type of refresh.
		if (shouldPrompt) {
			notifyIfNeededModal(event);
		}
		RefreshSubscriberJob.removeRefreshListener(this);
	}

	private void notifyIfNeededModal(final IRefreshEvent event) {
		TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				RefreshCompleteDialog d = new RefreshCompleteDialog(new Shell(TeamUIPlugin.getStandardDisplay()), event, participant);
				d.setBlockOnOpen(false);
				d.open();
			}
		});
	}

	private void notifyIfNeededNonModal(final IRefreshEvent event) {
		String message = Policy.bind("RefreshUserNotificationPolicy.0", event.getSubscriber().getName()); //$NON-NLS-1$
		PlatformUI.getWorkbench().getProgressService().requestInUI(new UIJob(message) {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				RefreshCompleteDialog d = new RefreshCompleteDialog(new Shell(TeamUIPlugin.getStandardDisplay()), event, participant);
				d.setBlockOnOpen(false);
				d.open();
				return Status.OK_STATUS;
			}
		}, message);
	}
}