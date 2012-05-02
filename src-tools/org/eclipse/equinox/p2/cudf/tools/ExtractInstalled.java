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

//Print on standard output the set of installed packages from the stanza
public class ExtractInstalled {
	public static void main(String[] args) {
		ProfileChangeRequest pcr = new Parser().parse(new File(args[0]));
		List l = pcr.getInitialState().getList();
		Collections.sort(l);
		Log.println(("Solution contains:" + l.size()));
		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			InstallableUnit iu = (InstallableUnit) iterator.next();
			if (!iu.isInstalled())
				continue;
			System.out.println("package: " + iu.getId());
			System.out.println("version: " + iu.getVersion().getMajor());
			System.out.println("installed: true");
			System.out.println();
		}

	}
}
