/*******************************************************************************
 * Copyright (c) 2009 Daniel Le Berre and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Daniel Le Berre - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.solver;

import java.math.BigInteger;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;

//	TRENDY:   we want to answer the user request, minimizing the number
//    of packages removed in the solution, maximizing the number
//    of packages at their most recent version in the solution, and
//    minimizing the number of extra packages installed;
//    the optimization criterion is
//
//         lex( min #removed, min #notuptodate, min #new)
//
//    Hence, two solutions S1 and S2 will be compared as follows:
//
//    i) compute ri = #removed(U,Si), ui = #uptodate(U,Si), ni = #new(U,Si)
//
//    ii) S1 is better than S2 iff
//        r1 < r2 or (r1=r2 and (u1>u2 or (u1=u2 and n1<n2)))

public class TrendyOptimizationFunction extends OptimizationFunction {
    private static final Log log = LogFactory.getLog(TrendyOptimizationFunction.class);

    public List createOptimizationFunction(InstallableUnit metaIu) {
        List weightedObjects = new ArrayList();
        BigInteger weight = BigInteger.valueOf(slice.size() + 1);
        removed(weightedObjects, weight.multiply(weight).multiply(weight),
                metaIu);
        notuptodate(weightedObjects, weight.multiply(weight), metaIu);
        optional(weightedObjects, weight, metaIu);
        niou(weightedObjects, BigInteger.ONE, metaIu);
        if (!weightedObjects.isEmpty()) {
            return weightedObjects;
        }
        return null;
    }

    public String getName() {
        return "misc 2010, trendy";
    }

    public String printSolutionValue() {
        StringBuilder sb = new StringBuilder();
        int removed = 0, notUpToDate = 0, recommends = 0, niou = 0;
        List proof = new ArrayList();

        for (int i = 0; i < removalVariables.size(); i++) {
            Object var = removalVariables.get(i);
            if (dependencyHelper.getBooleanValueFor(var)) {
                removed++;
                proof.add(var.toString().substring(18));
            }
        }
        sb.append("# Removed packages: " + proof);
        proof.clear();
        for (int i = 0; i < nouptodateVariables.size(); i++) {
            Object var = nouptodateVariables.get(i);
            if (dependencyHelper.getBooleanValueFor(var)) {
                notUpToDate++;
                proof.add(var.toString().substring(18));
            }
        }
        sb.append("# Not up-to-date packages: " + proof);
        proof.clear();
        for (Iterator it = unmetVariables.iterator(); it.hasNext();) {
            Object var = it.next();
            if (dependencyHelper.getBooleanValueFor(var)) {
                recommends++;
                proof.add(var.toString().substring(18));
            }
        }
        sb.append("# Not installed recommended packages: " + proof);
        proof.clear();
        for (int i = 0; i < newVariables.size(); i++) {
            Object var = newVariables.get(i);
            if (dependencyHelper.getBooleanValueFor(var)) {
                niou++;
                proof.add(var.toString().substring(18));
            }
        }
        sb.append("# Newly installed packages: " + proof);
        proof.clear();
        sb.append("# Trendy criteria value: -" + removed + ", -" + notUpToDate
                + ", -" + recommends + ", -" + niou);
        return sb.toString();
    }
}
