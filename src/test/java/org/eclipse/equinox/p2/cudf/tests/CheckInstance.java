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

import java.io.File;
import java.util.Collection;
import junit.framework.TestCase;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.solver.*;

public class CheckInstance extends TestCase {
	private File inputFile = null;
	private boolean successExpected = true;

	public CheckInstance(File nextElement, boolean expected) {
		super(nextElement.getAbsolutePath());
		inputFile = nextElement;
		successExpected = expected;
	}

	protected void runTest() throws Throwable {
		System.out.println();
		System.out.println();
		System.out.println("# " + inputFile);
		ProfileChangeRequest req = new Parser().parse(CUDFTestHelper.getStream(inputFile));
		SolverConfiguration configuration = new SolverConfiguration("paranoid", "1000c", true, successExpected);
		Object result = new SimplePlanner().getSolutionFor(req, configuration);
		if (successExpected) {
			if (!(result instanceof Collection))
				fail("Can not resolve: " + inputFile);
			if (req.getExpected() != -10)
				assertEquals(result.toString(), req.getExpected(), ((Collection) result).size());
		} else {
			if (result instanceof Collection)
				fail("No solution was expected: " + inputFile);
		}
	}

	protected void tearDown() throws Exception {
		System.gc();
	}

}
