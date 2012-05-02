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

import junit.framework.TestSuite;

import junit.framework.*;

public class AllTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(ParserTest.class);
		suite.addTest(CheckAllPassingInstances.suite());
		suite.addTest(CheckAllFailingInstances.suite());
		suite.addTestSuite(TestInstall.class);
		//		suite.addTestSuite(TestInstallUpdateConflict.class);
		//		suite.addTestSuite(TestNegationInDepends.class);
		//		suite.addTestSuite(TestNegationInRequest.class);
		suite.addTestSuite(TestJosepExample.class);
		suite.addTestSuite(TestPascalExample.class);
		suite.addTestSuite(TestOptional.class);
		suite.addTestSuite(TestRemoval.class);
		return suite;
	}

}
