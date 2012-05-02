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

import java.util.Arrays;

public class ORRequirement implements IRequiredCapability {
	private IRequiredCapability[] oredRequirements;

	private boolean optional;

	public ORRequirement(IRequiredCapability[] reqs, boolean optional) {
		oredRequirements = reqs;
		this.optional = optional;
	}

	public IRequiredCapability[] getRequirements() {
		return oredRequirements;
	}

	public String getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "OR" + Arrays.asList(oredRequirements);
	}

	public String getNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	public VersionRange getRange() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getSelectors() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isGreedy() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isMultiple() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setFilter(String filter) {
		// TODO Auto-generated method stub

	}

	public void setSelectors(String[] selectors) {
		// TODO Auto-generated method stub

	}

	public boolean isNegation() {
		return false;
	}

	public String toString() {
		String result = "OR(";
		for (int i = 0; i < oredRequirements.length; i++) {
			result += oredRequirements[i].toString();
		}
		return result + ")";
	}

	public int getArity() {
		return -1;
	}

	public void setArity(int arity) {
		throw new IllegalStateException();
	}
}
