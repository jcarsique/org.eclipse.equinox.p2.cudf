/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM Corporation - initial implementation and ideas 
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.tests;

import java.util.*;
import junit.framework.TestCase;
import org.eclipse.equinox.p2.cudf.metadata.*;
import org.eclipse.equinox.p2.cudf.query.QueryableArray;
import org.eclipse.equinox.p2.cudf.solver.*;

public class TestRemoval extends TestCase {
	private QueryableArray dataSet;
	private List alreadyInstalled = new ArrayList(3);

	protected void setUp() throws Exception {
		InstallableUnit iu = new InstallableUnit();
		iu.setId("A");
		iu.setVersion(new Version(1, 0, 0));
		iu.setInstalled(true);
		iu.setCapabilities(new ProvidedCapability[] {new ProvidedCapability("A", new VersionRange(new Version(1, 0, 0), true, new Version(1, 0, 0), true))});

		InstallableUnit iu2 = new InstallableUnit();
		iu2.setId("A");
		iu2.setVersion(new Version(2, 0, 0));
		iu2.setInstalled(true);
		iu2.setCapabilities(new ProvidedCapability[] {new ProvidedCapability("A", new VersionRange(new Version(2, 0, 0), true, new Version(2, 0, 0), true))});

		InstallableUnit iu3 = new InstallableUnit();
		iu3.setId("A");
		iu3.setVersion(new Version(3, 0, 0));
		iu.setInstalled(true);
		iu3.setCapabilities(new ProvidedCapability[] {new ProvidedCapability("A", new VersionRange(new Version(3, 0, 0), true, new Version(3, 0, 0), true))});

		alreadyInstalled.add(new RequiredCapability(iu.getId(), new VersionRange(iu.getVersion()), true));
		alreadyInstalled.add(new RequiredCapability(iu2.getId(), new VersionRange(iu2.getVersion()), true));
		alreadyInstalled.add(new RequiredCapability(iu3.getId(), new VersionRange(iu3.getVersion()), true));
		dataSet = new QueryableArray(new InstallableUnit[] {iu, iu2, iu3});
	}

	public void testRemoveEverything() {
		ProfileChangeRequest pcr = new ProfileChangeRequest(dataSet);
		pcr.removeInstallableUnit(new RequiredCapability("A", VersionRange.emptyRange));
		Collection result = (Collection) new SimplePlanner().getSolutionFor(pcr, new SolverConfiguration("paranoid", "1000c", true, false));
		assertEquals(0, result.size());
	}

	public void testRemoveOne() {
		ProfileChangeRequest pcr = new ProfileChangeRequest(dataSet);
		pcr.removeInstallableUnit(new RequiredCapability("A", new VersionRange("[3.0.0, 3.0.0]")));
		pcr.setPreInstalledIUs(alreadyInstalled);
		Collection result = (Collection) new SimplePlanner().getSolutionFor(pcr, new SolverConfiguration("paranoid", "1000c", true, false));
		assertEquals(2, result.size());
	}

}
