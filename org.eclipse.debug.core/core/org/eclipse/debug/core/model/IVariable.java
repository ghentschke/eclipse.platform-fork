package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;

/**
 * A variable represents a visible data structure in a stack frame
 * or value.
 * Each variable has a value which may in turn contain more variables.
 * A variable may support value modification.
 * <p>
 * An implementation may choose to re-use or discard
 * variables on iterative thread suspensions. Clients
 * cannot assume that variables are identical or equal across
 * iterative thread suspensions and must check for equality on iterative
 * suspensions if they wish to re-use the objects.
 * </p>
 * <p>
 * An implementation that preserves equality
 * across iterative suspensions may display more desirable behavior in
 * some clients. For example, if variables are preserved
 * while stepping, a UI client would be able to update the UI incrementally,
 * rather than collapse and redraw the entire list or tree.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IValue
 * @see IStackFrame
 * @see IValueModification
 */
public interface IVariable extends IDebugElement, IValueModification {
	/**
	 * Returns the value of this variable.
	 * 
	 * @return this variable's value
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public IValue getValue() throws DebugException;
	/**
	 * Returns the name of this variable. Name format is debug model
	 * specific, and should be specified by a debug model.
	 *
	 * @return this variable's name
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public String getName() throws DebugException;	
	/**
	 * Returns a description of the type of data this variable is
	 * declared to reference. Note that the declared type of a
	 * variable and the concrete type of its value are not neccessarily
	 * the same.
	 *
	 * @return the declared type of this variable
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the VM.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 */
	public String getReferenceTypeName() throws DebugException;
	
	/** 
	 * Returns whether this variable's value has changed since the last suspend event. 
	 * Implementations may choose whether the last suspend event is the last suspend 
	 * event in this variable's debug target, or within the thread(s) in which this variable 
	 * is visible. 
	 * <p>
	 * Implementations that choose not to implement this function should always
	 * return <code>false</code>.
	 * </p>
	 * 
	 * @return whether this variable's value has changed since the last suspend event 
	 * @exception DebugException if an exception occurrs determining if this variable's 
	 *   value has changed since the last suspend event 
	 */ 
	public boolean hasValueChanged() throws DebugException; 

	
}
