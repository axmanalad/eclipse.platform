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
package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.viewers.DefaultTableUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.ui.progress.UIJob;

/**
 * This update policy updates immediately after a model changed event.  The update policy will
 * only update if the rendering is visible.  Cache from the content manager is cleared
 * when the memory block has changed when the rendering is not visible
 *
 */
class AsyncTableRenderingUpdatePolicy extends DefaultTableUpdatePolicy
{
	public void modelChanged(IModelDelta node) {
		
		// clear current cache as it becomes invalid when the memory block is changed
		AbstractVirtualContentTableModel model = getTableViewer().getVirtualContentModel();
		
		if (model != null)
		{
			IContentChangeComputer computer = null;
			if (model instanceof IContentChangeComputer)
				computer = (IContentChangeComputer)model;
			
			clearCache(computer);
			
			if (!containsEvent(node))
			{
				return;
			}
			
			if (node.getElement() instanceof IMemoryBlock && (node.getFlags() & IModelDelta.CONTENT) != 0)
			{
				if (computer != null && getTableViewer() != null)
				{
					// only cache if the rendering is not currently displaying error
					if (!getTableViewer().getRendering().isDisplayingError())
					{
						// cache visible elelements
						computer.cache(model.getElements());
					}
				}
				
				// override handling of content node
				// let the super class deals with the rest of the changes
				if (node.getElement() instanceof IMemoryBlock)
				{
					// update policy figured out what's changed in the memory block
					// and will tell rendering to update accordinly.
					// Updating the rendering indirectly update the table viewer
					notifyRendering(node);
					handleMemoryBlockChanged((IMemoryBlock)node.getElement(), node);
					return;
				}
			}
		}
		
		super.modelChanged(node);
	}

	/**
	 * @param computer
	 */
	protected void clearCache(IContentChangeComputer computer) {
		if (computer != null)
			computer.clearCache();
	}

	private void notifyRendering(IModelDelta node) {
		if (getTableViewer() != null)
		{
			IModelChangedListener listener = (IModelChangedListener)getTableViewer().getRendering().getAdapter(IModelChangedListener.class);
			if (listener != null)
				listener.modelChanged(node);
		}
	}
	
	protected void handleMemoryBlockChanged(IMemoryBlock mb, IModelDelta delta)
	{
		try {
			if (getViewer().getPresentationContext() instanceof TableRenderingPresentationContext)
			{
				TableRenderingPresentationContext context = (TableRenderingPresentationContext)getViewer().getPresentationContext();
				TableRenderingContentDescriptor descriptor = context.getContentDescriptor();

				final AbstractAsyncTableRendering rendering = context.getTableRendering();
				
				if (rendering != null)
				{
					final BigInteger address = getMemoryBlockBaseAddress(mb);
					if (!address.equals(descriptor.getContentBaseAddress()))
					{
						descriptor.updateContentBaseAddress();
						UIJob job = new UIJob("go to address"){ //$NON-NLS-1$
	
							public IStatus runInUIThread(IProgressMonitor monitor) {
								try {
									rendering.goToAddress(address);
								} catch (DebugException e) {
									if (getTableViewer() != null)
										getTableViewer().handlePresentationFailure(null, e.getStatus());
								}
								return Status.OK_STATUS;
							}};
						job.setSystem(true);
						job.schedule();
					}
					else
					{
						rendering.refresh();
					}
				}
			}
		} catch (DebugException e) {
			if (getTableViewer() != null)
				getTableViewer().handlePresentationFailure(null, e.getStatus());
		}
	}
	
	private BigInteger getMemoryBlockBaseAddress(IMemoryBlock mb) throws DebugException
	{
		if (mb instanceof IMemoryBlockExtension)
			return ((IMemoryBlockExtension)mb).getBigBaseAddress();
		else
			return BigInteger.valueOf(mb.getStartAddress());
	}
	
	private AsyncTableRenderingViewer getTableViewer()
	{
		if (getViewer() instanceof AsyncTableRenderingViewer)
			return (AsyncTableRenderingViewer)getViewer();
		return null;
	}
	
	private boolean containsEvent(IModelDelta delta)
	{
		if (getViewer().getPresentationContext() instanceof TableRenderingPresentationContext)
		{
			TableRenderingPresentationContext context = (TableRenderingPresentationContext) getViewer().getPresentationContext();
			if (context.getRendering() instanceof AbstractAsyncTableRendering)
			{
				AbstractAsyncTableRendering rendering = (AbstractAsyncTableRendering)context.getRendering();
				if (!rendering.isVisible())
					return false;
			}
		}
		return true;
	}
}