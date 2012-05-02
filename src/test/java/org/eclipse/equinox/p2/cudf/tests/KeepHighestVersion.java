/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * Sonatype, Inc. - initial implementation and ideas 
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.tests;

import junit.framework.TestCase;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.*;
import org.eclipse.equinox.p2.cudf.query.*;
import org.eclipse.equinox.p2.cudf.solver.ProfileChangeRequest;
import org.eclipse.equinox.p2.cudf.solver.Slicer;

public class KeepHighestVersion extends TestCase {
	private ProfileChangeRequest pcr = null;

	protected void setUp() throws Exception {
		pcr = new Parser().parse(this.getClass().getClassLoader().getResource("testData/keepHighestVersion.cudf").openStream());
	}

	public void testHighestVersion() {
		final String ID = "libxapian-dev";
		QueryableArray result = slice(pcr.getInitialState(), ID, new Version(2));
		assertEquals(2, result.getSize());
		assertEquals(1, result.query(new CapabilityQuery(new RequiredCapability(ID, new VersionRange(new Version(5), true, new Version(5), true))), new Collector(), null).size());
		assertEquals(1, result.query(new CapabilityQuery(new RequiredCapability(ID, new VersionRange(new Version(2), true, new Version(2), true))), new Collector(), null).size());
	}

	private QueryableArray slice(QueryableArray input, String id, Version version) {
		return new Slicer(input).slice((InstallableUnit) input.query(new CapabilityQuery(new RequiredCapability(id, new VersionRange(version))), new Collector(), null).iterator().next(), null);
	}
}
