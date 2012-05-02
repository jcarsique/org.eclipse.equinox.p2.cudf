/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.tools;

import java.io.File;
import java.util.*;
import org.eclipse.equinox.p2.cudf.Log;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.ProfileChangeRequest;

//Sort a CUDF file on the standard otput
public class StanzaSorter {
	public static void main(String[] args) {
		ProfileChangeRequest pcr = new Parser().parse(new File(args[0]));
		List state = pcr.getInitialState().getList();
		Collections.sort(state);
		Log.println(("Solution contains:" + state.size()));
		for (Iterator iterator = state.iterator(); iterator.hasNext();) {
			InstallableUnit iu = (InstallableUnit) iterator.next();
			System.out.println("package: " + iu.getId());
			System.out.println("version: " + iu.getVersion().getMajor());
			System.out.println("installed: true");
			System.out.println();
		}

	}
}
