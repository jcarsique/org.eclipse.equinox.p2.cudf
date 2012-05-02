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
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.solver.ProfileChangeRequest;

public class Comparator {
	public static void main(String[] args) {
		ProfileChangeRequest stanza1 = new Parser().parse(new File(args[0]));
		ProfileChangeRequest stanza2 = new Parser().parse(new File(args[1]));
		Set set1 = new HashSet(stanza1.getInitialState().getList());
		Set set2 = new HashSet(stanza2.getInitialState().getList());
		if (set1.containsAll(set2) && set2.containsAll(set1)) {
			System.out.println("NO DIFFERENCE");
			return;
		}
		System.out.println(args[0] + " contains " + set1.size());
		System.out.println(args[1] + " contains " + set2.size());
		set1.removeAll(set2);
		if (set1.isEmpty())
			System.out.println(args[0] + " contains all elements of " + args[1]);
		else {
			System.out.println(args[0] + " contains the following additional elements over" + args[1]);
			for (Iterator iterator = set1.iterator(); iterator.hasNext();) {
				System.out.println(iterator.next());
			}
		}
		Set set1p = new HashSet(stanza1.getInitialState().getList());
		Set set2p = new HashSet(stanza2.getInitialState().getList());
		set2p.removeAll(set1p);
		if (set2p.isEmpty())
			System.out.println(args[1] + " contains all elements of " + args[0]);
		else {
			System.out.println(args[1] + " contains the following additional elements over" + args[0]);
			for (Iterator iterator = set2p.iterator(); iterator.hasNext();) {
				System.out.println(iterator.next());
			}
		}
	}
}
