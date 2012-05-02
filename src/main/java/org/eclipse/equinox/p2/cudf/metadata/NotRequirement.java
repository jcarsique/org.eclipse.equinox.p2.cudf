/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM Corporation - initial implementation and ideas 
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.metadata;

public class NotRequirement implements IRequiredCapability {
	private IRequiredCapability negatedRequirement;

	public NotRequirement(IRequiredCapability iRequiredCapabilities) {
		negatedRequirement = iRequiredCapabilities;
	}

	public IRequiredCapability getRequirement() {
		return negatedRequirement;
	}

	public String getName() {
		return negatedRequirement.getName();
	}

	public VersionRange getRange() {
		return negatedRequirement.getRange();
	}

	public boolean isNegation() {
		return true;
	}

	public String toString() {
		return "NOT(" + negatedRequirement.toString() + ')'; //$NON-NLS-1$
	}

	public boolean isOptional() {
		return negatedRequirement.isOptional();
	}

	public int getArity() {
		return -1;
	}

	public void setArity(int arity) {
		throw new IllegalStateException();
	}
}
