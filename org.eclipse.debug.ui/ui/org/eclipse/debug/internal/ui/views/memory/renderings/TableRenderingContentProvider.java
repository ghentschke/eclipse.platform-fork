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

package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for MemoryViewTab
 * 
 * @since 3.0
 */
public class TableRenderingContentProvider extends BasicDebugViewContentProvider {
	
	private static final String PREFIX = "MemoryViewContentProvider."; //$NON-NLS-1$
	private static final String UNABLE_TO_RETRIEVE_CONTENT = PREFIX + "Unable_to_retrieve_content"; //$NON-NLS-1$
		
	// cached information
	protected Vector lineCache;
	
	// keeps track of all memory line ever retrieved
	// allow us to compare and compute deltas
	protected Hashtable contentCache;
	
	private BigInteger fBufferTopAddress;
	
	private TableRenderingContentInput fInput;
	
	/**
	 * @param memoryBlock
	 * @param newTab
	 */
	public TableRenderingContentProvider()
	{
		lineCache = new Vector();
		contentCache = new Hashtable();
			
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * @param viewer
	 */
	public void setViewer(StructuredViewer viewer)
	{
		fViewer = viewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		try {
			if (newInput instanceof TableRenderingContentInput)
			{
				fInput = (TableRenderingContentInput)newInput;
				if (fInput.getMemoryBlock() instanceof IMemoryBlockExtension)
					loadContentForExtendedMemoryBlock();
				else
					loadContentForSimpleMemoryBlock();
				
				// tell rendering to display table if the loading is successful
				fInput.getMemoryRendering().displayTable();
			}
		} catch (DebugException e) {
			DebugUIPlugin.log(e.getStatus());
			fInput.getMemoryRendering().displayError(e);
		}
	}

	public void dispose() {

		// fTabItem disposed by view tab
		
		DebugPlugin.getDefault().removeDebugEventListener(this);		
		
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {

		// if cache is empty, get memory
		if (lineCache.isEmpty()) { 
		
			try {
				IMemoryBlock memoryBlock = fInput.getMemoryBlock();
				if (memoryBlock instanceof IMemoryBlockExtension)
				{
					loadContentForExtendedMemoryBlock();
					fInput.getMemoryRendering().displayTable();
				}
				else
				{
					loadContentForSimpleMemoryBlock();
					fInput.getMemoryRendering().displayTable();
				}
			} catch (DebugException e) {
				DebugUIPlugin.log(e.getStatus());
				fInput.getMemoryRendering().displayError(e);
				return lineCache.toArray();
			}
		}
		return lineCache.toArray();
	}

	/**
	 * @throws DebugException
	 */
	private void loadContentForSimpleMemoryBlock() throws DebugException {
		// get as much memory as the memory block can handle
		fInput.setPreBuffer(0);
		fInput.setPostBuffer(0);
		fInput.setDefaultBufferSize(0);
		long startAddress = fInput.getMemoryBlock().getStartAddress();
		BigInteger address = BigInteger.valueOf(startAddress);
		long length = fInput.getMemoryBlock().getLength();
		long numLines = length / fInput.getMemoryRendering().getBytesPerLine();
		getMemoryToFitTable(address, numLines, fInput.isUpdateDelta());
	}

	/**
	 * @throws DebugException
	 */
	private void loadContentForExtendedMemoryBlock() throws DebugException {
		// calculate top buffered address
		BigInteger address = fInput.getStartingAddress();
		if (address == null)
		{
			address = new BigInteger("0"); //$NON-NLS-1$
		}
		BigInteger bigInt = address;
		if (bigInt.compareTo(BigInteger.valueOf(32)) <= 0) {
			fInput.setPreBuffer(0);
		} else {
			fInput.setPreBuffer(bigInt.divide(BigInteger.valueOf(32)).min(BigInteger.valueOf(fInput.getDefaultBufferSize())).intValue());
		}
		int addressibleUnit = fInput.getMemoryRendering().getAddressibleUnitPerLine();
		address = bigInt.subtract(BigInteger.valueOf(addressibleUnit*fInput.getPostBuffer()));
		
		if (address.compareTo(BigInteger.valueOf(0)) < 0)
			address = BigInteger.valueOf(0);
		
		int numLines = fInput.getNumVisibleLines()+fInput.getPostBuffer()+fInput.getPostBuffer();
		
		// get stoarage to fit the memory view tab size
		getMemoryToFitTable(address, numLines, fInput.isUpdateDelta());
	}
	
	/**
	 * @return the memroy block
	 */
	public IMemoryBlock getMemoryBlock() {
		return fInput.getMemoryBlock();
	}
	
	/**
	 * Get memory to fit table
	 * @param startingAddress
	 * @param numberOfLines
	 * @param updateDelta
	 * @throws DebugException
	 */
	public void getMemoryToFitTable(BigInteger startingAddress, long numberOfLines, boolean updateDelta) throws DebugException
	{
		// do not ask for memory from memory block if the debug target
		// is already terminated
		IDebugTarget target = fInput.getMemoryBlock().getDebugTarget();
		
		if (target.isDisconnected() || target.isTerminated())
			return;
		
		boolean error = false;
		DebugException dbgEvt = null;
		
		// calculate address size
		String adjustedAddress = startingAddress.toString(16);
		
		int addressSize = getAddressSize(startingAddress);
		
		int addressLength = addressSize * IInternalDebugUIConstants.CHAR_PER_BYTE;

		// align starting address with double word boundary
		if (fInput.getMemoryBlock() instanceof IMemoryBlockExtension)
		{
			if (!adjustedAddress.endsWith("0")) //$NON-NLS-1$
			{
				adjustedAddress = adjustedAddress.substring(0, adjustedAddress.length() - 1);
				adjustedAddress += "0"; //$NON-NLS-1$
				startingAddress = new BigInteger(adjustedAddress, 16);
			}
		}

		IMemoryBlockExtension extMemoryBlock = null;
		MemoryByte[] memoryBuffer = null;
		
		String paddedString = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugPreferenceConstants.PREF_PADDED_STR);
		
		long reqNumBytes = 0;
		try
		{
			if (fInput.getMemoryBlock() instanceof IMemoryBlockExtension)
			{
				reqNumBytes = fInput.getMemoryRendering().getBytesPerLine() * numberOfLines;
				// get memory from memory block
				extMemoryBlock = (IMemoryBlockExtension) fInput.getMemoryBlock();
				
				long reqNumberOfUnits = fInput.getMemoryRendering().getAddressibleUnitPerLine() * numberOfLines;
				
				memoryBuffer =	extMemoryBlock.getBytesFromAddress(startingAddress,	reqNumberOfUnits);
				
				if(memoryBuffer == null)
				{
					DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_RETRIEVE_CONTENT), null));
					throw e;
				}
			}
			else
			{
				// get memory from memory block
				byte[] memory = fInput.getMemoryBlock().getBytes();
				
				if (memory == null)
				{
					DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_RETRIEVE_CONTENT), null));	
					throw e;					
				}
				
				int prefillNumBytes = 0;
				
				// number of bytes need to prefill
				if (!startingAddress.toString(16).endsWith("0")) //$NON-NLS-1$
				{
					adjustedAddress = startingAddress.toString(16).substring(0, adjustedAddress.length() - 1);
					adjustedAddress += "0"; //$NON-NLS-1$
					BigInteger adjustedStart = new BigInteger(adjustedAddress, 16);
					prefillNumBytes = startingAddress.subtract(adjustedStart).intValue();
					startingAddress = adjustedStart;
				}
				reqNumBytes = memory.length + prefillNumBytes;
				
				// figure out number of dummy bytes to append
				while (reqNumBytes % fInput.getMemoryRendering().getBytesPerLine() != 0)
				{
					reqNumBytes ++;
				}
				
				numberOfLines = reqNumBytes / fInput.getMemoryRendering().getBytesPerLine();
				
				// create memory byte for IMemoryBlock
				memoryBuffer = new MemoryByte[(int)reqNumBytes];
				
				// prefill buffer to ensure double-word alignment
				for (int i=0; i<prefillNumBytes; i++)
				{
					MemoryByte tmp = new MemoryByte();
					tmp.setValue((byte)0);
					tmp.setReadonly(true);
					tmp.setValid(false);
					memoryBuffer[i] = tmp;
				}
				
				// fill buffer with memory returned by debug adapter
				int j = prefillNumBytes; 							// counter for memoryBuffer
				for (int i=0; i<memory.length; i++)
				{
					MemoryByte tmp = new MemoryByte();
					tmp.setValue(memory[i]);
					tmp.setValid(true);
					memoryBuffer[j] = tmp;
					j++;
				}
				
				// append to buffer to fill up the entire line
				for (int i=j; i<memoryBuffer.length; i++)
				{
					MemoryByte tmp = new MemoryByte();
					tmp.setValue((byte)0);
					tmp.setReadonly(true);
					tmp.setValid(false);
					memoryBuffer[i] = tmp;
				}
			}
		}
		catch (DebugException e)
		{
			memoryBuffer = makeDummyContent(numberOfLines);
			
			// finish creating the content provider before throwing an event
			error = true; 
			dbgEvt = e;
		}
		catch (Throwable e)
		{
			// catch all errors from this process just to be safe
			memoryBuffer = makeDummyContent(numberOfLines);
			
			// finish creating the content provider before throwing an event
			error = true; 
			dbgEvt = new DebugException(DebugUIPlugin.newErrorStatus(e.getMessage(), e));
			DebugUIPlugin.log(e);
		}
		
		// if debug adapter did not return enough memory, create dummy memory
		if (memoryBuffer.length < reqNumBytes)
		{
			ArrayList newBuffer = new ArrayList();
			
			for (int i=0; i<memoryBuffer.length; i++)
			{
				newBuffer.add(memoryBuffer[i]);
			}
			
			for (int i=memoryBuffer.length; i<reqNumBytes; i++)
			{
				byte value = 0;
				byte flags = 0;
				flags |= MemoryByte.READONLY;
				newBuffer.add(new MemoryByte(value, flags));
			}
			
			memoryBuffer = (MemoryByte[])newBuffer.toArray(new MemoryByte[newBuffer.size()]);
			
		}
		
		// clear line cacheit'
		if (!lineCache.isEmpty())
		{
			lineCache.clear();
		}
		String address = startingAddress.toString(16);
		// save address of the top of buffer
		fBufferTopAddress = startingAddress;
		
		boolean manageDelta = true;
		
		// If change information is not managed by the memory block
		// The view tab will manage it and calculate delta information
		// for its content cache.
		if (fInput.getMemoryBlock() instanceof IMemoryBlockExtension)
		{
			manageDelta = !((IMemoryBlockExtension)fInput.getMemoryBlock()).supportsChangeManagement();
		}
			
		// put memory information into MemoryViewLine
		for (int i = 0; i < numberOfLines; i++)
		{ //chop the raw memory up 
			String tmpAddress = address.toUpperCase();
			if (tmpAddress.length() < addressLength)
			{
				for (int j = 0; tmpAddress.length() < addressLength; j++)
				{
					tmpAddress = "0" + tmpAddress; //$NON-NLS-1$
				}
			}
			MemoryByte[] memory = new MemoryByte[fInput.getMemoryRendering().getBytesPerLine()];
			boolean isMonitored = true;
			
			// counter for memory, starts from 0 to number of bytes per line
			int k = 0;
			// j is the counter for memArray, memory returned by debug adapter
			for (int j = i * fInput.getMemoryRendering().getBytesPerLine();
				j < i * fInput.getMemoryRendering().getBytesPerLine() + fInput.getMemoryRendering().getBytesPerLine();
				j++)
			{
				
				byte changeFlag = memoryBuffer[j].getFlags();
				if (manageDelta)
				{
					// turn off both change and known bits to make sure that
					// the change bits returned by debug adapters do not take
					// any effect
					
					changeFlag |= MemoryByte.KNOWN;
					changeFlag ^= MemoryByte.KNOWN;
					
					changeFlag |= MemoryByte.CHANGED;
					changeFlag ^= MemoryByte.CHANGED;
				}
				
				MemoryByte newByteObj = new MemoryByte(memoryBuffer[j].getValue(), changeFlag);
				memory[k] =  newByteObj;
				k++;
				
				
				if (!manageDelta)
				{
					// If the byte is marked as unknown, the line is not monitored
					if (!memoryBuffer[j].isKnown())
					{
						isMonitored = false;
					}
				}
			}
			
			TableRenderingLine newLine = new TableRenderingLine(tmpAddress, memory, lineCache.size(), paddedString);
			
			TableRenderingLine oldLine = (TableRenderingLine)contentCache.get(newLine.getAddress());
			
			if (manageDelta)
			{
				if (oldLine != null)
					newLine.isMonitored = true;
				else
					newLine.isMonitored = false;
			}
			else
			{
				// check the byte for information
				newLine.isMonitored = isMonitored;
			}
			
			// calculate delta info for the memory view line
			if (manageDelta && !fInput.getMemoryRendering().isDisplayingError())
			{
				if (updateDelta)
				{
					if (oldLine != null)
					{
						newLine.markDeltas(oldLine);
					}
				}
				else
				{
					if (oldLine != null)
					{
						// deltas can only be reused if the line has not been changed
						// otherwise, force a refresh
						if (newLine.isLineChanged(oldLine))
						{
							newLine.markDeltas(oldLine);
						}
						else
						{
							newLine.copyDeltas(oldLine);
						}
					}
				}
			}
			else if (manageDelta && fInput.getMemoryRendering().isDisplayingError())
			{
				// show as unmonitored if the view tab is previoulsy displaying error
				newLine.isMonitored = false;
			}
			lineCache.add(newLine);
			
			// increment row address
			BigInteger bigInt = new BigInteger(address, 16);
			int addressibleUnit = fInput.getMemoryRendering().getBytesPerLine()/fInput.getMemoryRendering().getAddressibleSize();
			address = bigInt.add(BigInteger.valueOf(addressibleUnit)).toString(16);
		}
		
		if (error){
			throw dbgEvt;
		}
	}
	
	/**
	 * @param numberOfLines
	 * @return an array of dummy MemoryByte
	 */
	private MemoryByte[] makeDummyContent(long numberOfLines) {
		MemoryByte[] memoryBuffer;
		// make up dummy memory, needed for recovery in case the debug adapter
		// is capable of retrieving memory again

		int numBytes = (int)(fInput.getMemoryRendering().getBytesPerLine() * numberOfLines);
		memoryBuffer = new MemoryByte[numBytes];
		
		for (int i=0; i<memoryBuffer.length; i++){
			memoryBuffer[i] = new MemoryByte();
			memoryBuffer[i].setValue((byte)0);
			memoryBuffer[i].setReadonly(true);
		}
		return memoryBuffer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.internal.views.BasicDebugViewContentProvider#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		
		// do nothing if the debug event did not come from a debug element comes from non-debug element
		if (!(event.getSource() instanceof IDebugElement))
			return;
		
		IDebugElement src = (IDebugElement)event.getSource();
		
		// if a debug event happens from the memory block
		// invoke contentChanged to get content of the memory block updated
		if (event.getKind() == DebugEvent.CHANGE && event.getSource() == fInput.getMemoryBlock())
		{
			if (event.getDetail() == DebugEvent.STATE){
				fInput.getMemoryRendering().updateLabels();
			}
			else
			{	
				updateContent();
			}
		}
		
		// if the suspend evnet happens from the debug target that the blocked
		// memory block belongs to
		if (event.getKind() == DebugEvent.SUSPEND && src.getDebugTarget() == fInput.getMemoryBlock().getDebugTarget())
		{	
			updateContent();
		}

	}
	
	/**
	 * Update content of the view tab if the content of the memory block has changed
	 * or if its base address has changed
	 * Update will not be performed if the memory block has not been changed or
	 * if the view tab is disabled.
	 */
	public void updateContent()
	{
		IDebugTarget dt = fInput.getMemoryBlock().getDebugTarget();
		
		// no need to update if debug target is disconnected or terminated
		if (dt.isDisconnected() || dt.isTerminated())
		{
			return;
		}
		
		// cache content before getting new ones
		TableRenderingLine[] lines =(TableRenderingLine[]) lineCache.toArray(new TableRenderingLine[lineCache.size()]);
		if (contentCache != null)
		{
			contentCache.clear();
		}
		
		//do not handle event if the rendering is not visible
		if (!fInput.getMemoryRendering().isVisible())
			 return;
		
		// use existing lines as cache is the rendering is not currently displaying
		// error.  Otherwise, leave contentCache empty as we do not have updated
		// content.
		if (!fInput.getMemoryRendering().isDisplayingError())
		{
			for (int i=0; i<lines.length; i++)
			{
				contentCache.put(lines[i].getAddress(), lines[i]);
				lines[i].isMonitored = true;
			}
		}

		// reset all the deltas currently stored in contentCache
		// This will ensure that changes will be recomputed when user scrolls
		// up or down the memory view.		
		resetDeltas();
		fInput.getMemoryRendering().refresh();
		
	}

	/**
	 * @return buffer's top address
	 */
	public BigInteger getBufferTopAddress()
	{
		return fBufferTopAddress;
	}
	
	/**
	 * Calculate address size of the given address
	 * @param address
	 * @return size of address from the debuggee
	 */
	public int getAddressSize(BigInteger address)
	{
		// calculate address size
		 String adjustedAddress = address.toString(16);
		
		 int addressSize = 0;
		 if (fInput.getMemoryBlock() instanceof IMemoryBlockExtension)
		 {
			 addressSize = ((IMemoryBlockExtension)fInput.getMemoryBlock()).getAddressSize();
		 }
		
		 // handle IMemoryBlock and invalid address size returned by IMemoryBlockExtension
		 if (addressSize <= 0)
		 {
			 if (adjustedAddress.length() > 8)
			 {
				 addressSize = 8;
			 }
			 else
			 {
				 addressSize = 4;
			 }			
		 }		
		 
		 return addressSize;
	}
	
	/**
	 * @return base address of memory block
	 */
	public BigInteger getContentBaseAddress()
	{
		return fInput.getContentBaseAddress(); 
	}
	
	/**
	 * Clear all delta information in the lines
	 */
	public void resetDeltas()
	{
		Enumeration enumeration = contentCache.elements();
		
		while (enumeration.hasMoreElements())
		{
			TableRenderingLine line = (TableRenderingLine)enumeration.nextElement();
			line.unmarkDeltas();
		}
	}
	
	/**
	 * Check if address is out of buffered range
	 * @param address
	 * @return true if address is out of bufferred range, false otherwise
	 */
	public boolean isAddressOutOfRange(BigInteger address)
	{
		if (lineCache != null)
		{
			TableRenderingLine first = (TableRenderingLine)lineCache.firstElement();
			TableRenderingLine last = (TableRenderingLine) lineCache.lastElement();
			
			if (first == null ||last == null)
				return true;
			
			BigInteger startAddress = new BigInteger(first.getAddress(), 16);
			BigInteger lastAddress = new BigInteger(last.getAddress(), 16);
			int addressibleUnit = fInput.getMemoryRendering().getAddressibleUnitPerLine();
			lastAddress = lastAddress.add(BigInteger.valueOf(addressibleUnit));
			
			if (startAddress.compareTo(address) <= 0 &&
				lastAddress.compareTo(address) >= 0)
			{
				return false;
			}
			return true;
		}
		return true;
	}
	
	public void clearContentCache()
	{
		contentCache.clear();
	}
}
