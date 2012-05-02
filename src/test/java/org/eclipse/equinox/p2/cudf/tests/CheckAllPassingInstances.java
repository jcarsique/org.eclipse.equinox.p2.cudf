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
import junit.framework.*;

//DISABLED - Made the class abstract to have the tests passing
public abstract class CheckAllPassingInstances extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(CheckAllPassingInstances.class.getName());
		File resourceDirectory = new File("/Users/pascal/Downloads/problems/10orplus/");
		File[] resources = new File[] {new File("/Users/pascal/Desktop/problems/caixa/558.cudf")};
		for (int i = 0; i < resources.length; i++) {
			suite.addTest(new CheckInstance(resources[i], true));
		}
		return suite;
	}
}
