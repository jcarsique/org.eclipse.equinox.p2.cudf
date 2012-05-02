/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.solver;

import java.util.*;
import org.eclipse.equinox.p2.cudf.metadata.*;
import org.eclipse.equinox.p2.cudf.query.QueryableArray;

public class ProfileChangeRequest {

	private QueryableArray initialState;
	private ArrayList iusToRemove = new ArrayList(10); // list of ius to remove
	private ArrayList iusToAdd = new ArrayList(10); // list of ius to add
	private ArrayList iusToUpdate = new ArrayList(10); // list of ius to add
	private List iusPreInstalled = new ArrayList(1); //this will get overwritten
	private List constraintsFromKeep = new ArrayList(1); //this will get overwritten

	private int expected = -10;

	public ProfileChangeRequest(QueryableArray initialState) {
		this.initialState = initialState;
	}

	public void setPreInstalledIUs(List list) {
		iusPreInstalled = list;
	}

	public void setContrainstFromKeep(List constraints) {
		constraintsFromKeep = constraints;
	}

	public void addInstallableUnit(IRequiredCapability req) {
		iusToAdd.add(req);
	}

	public void removeInstallableUnit(IRequiredCapability toUninstall) {
		iusToRemove.add(new NotRequirement(toUninstall));
	}

	public void upgradeInstallableUnit(IRequiredCapability toUpgrade) {
		iusToUpdate.add(toUpgrade);
	}

	public ArrayList getAllRequests() {
		ArrayList result = new ArrayList(iusToAdd.size() + iusToRemove.size() + iusToUpdate.size() + iusPreInstalled.size());
		result.addAll(constraintsFromKeep);
		result.addAll(iusToAdd);
		result.addAll(iusToRemove);
		result.addAll(iusToUpdate);
		result.addAll(iusPreInstalled);
		return result;
	}

	public QueryableArray getInitialState() {
		return initialState;
	}

	public int getExpected() {
		return expected;
	}

	public void setExpected(int expected) {
		this.expected = expected;
	}

	public void purge() {
		iusPreInstalled = null;
		iusToAdd = null;
		iusToRemove = null;
		iusToUpdate = null;
		initialState = null;
	}

	public List getExtraRequirements() {
		List result = new ArrayList(iusPreInstalled.size());
		for (Iterator iterator = iusPreInstalled.iterator(); iterator.hasNext();) {
			IRequiredCapability type = (IRequiredCapability) iterator.next();
			result.add(new RequiredCapability(type.getName(), VersionRange.emptyRange, false));
		}
		return result;
	}
}
