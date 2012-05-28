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

    // list of ius to remove
    private ArrayList<IRequiredCapability> iusToRemove = new ArrayList<IRequiredCapability>(
            10);

    // list of ius to add
    private ArrayList<IRequiredCapability> iusToAdd = new ArrayList<IRequiredCapability>(
            10);

    // list of ius to add
    private List<IRequiredCapability> iusToUpdate = new ArrayList<IRequiredCapability>(
            10);

    // this will get overwritten
    private List<IRequiredCapability> iusPreInstalled = new ArrayList<IRequiredCapability>(
            1);

    // this will get overwritten
    private List<IRequiredCapability> constraintsFromKeep = new ArrayList<IRequiredCapability>(
            1);

    private int expected = -10;

    public ProfileChangeRequest(QueryableArray initialState) {
        this.initialState = initialState;
    }

    public void setPreInstalledIUs(List<IRequiredCapability> list) {
        iusPreInstalled = list;
    }

    public void setContrainstFromKeep(List<IRequiredCapability> constraints) {
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

    public ArrayList<IRequiredCapability> getAllRequests() {
        ArrayList<IRequiredCapability> result = new ArrayList<IRequiredCapability>(
                iusToAdd.size() + iusToRemove.size() + iusToUpdate.size()
                        + iusPreInstalled.size());
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

    public List<IRequiredCapability> getExtraRequirements() {
        List<IRequiredCapability> result = new ArrayList<IRequiredCapability>(
                iusPreInstalled.size());
        for (Iterator<IRequiredCapability> iterator = iusPreInstalled.iterator(); iterator.hasNext();) {
            IRequiredCapability type = iterator.next();
            result.add(new RequiredCapability(type.getName(),
                    VersionRange.emptyRange, false));
        }
        return result;
    }
}
