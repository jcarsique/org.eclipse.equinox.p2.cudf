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
import org.eclipse.equinox.p2.cudf.metadata.IRequiredCapability;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.query.CapabilityQuery;
import org.eclipse.equinox.p2.cudf.query.Collector;
import org.sat4j.pb.tools.WeightedObject;

public class P2OptimizationFunction extends OptimizationFunction {

    public List createOptimizationFunction(InstallableUnit metaIu) {
        List weightedObjects = new ArrayList();

        Set s = slice.entrySet();
        final BigInteger POWER = BigInteger.valueOf(2);

        BigInteger maxWeight = POWER;
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            HashMap conflictingEntries = (HashMap) entry.getValue();
            if (conflictingEntries.size() == 1) {
                continue;
            }
            List toSort = new ArrayList(conflictingEntries.values());
            Collections.sort(toSort, Collections.reverseOrder());
            BigInteger weight = POWER;
            int count = toSort.size();
            for (int i = 0; i < count; i++) {
                InstallableUnit iu = (InstallableUnit) toSort.get(i);
                weightedObjects.add(WeightedObject.newWO(iu,
                        iu.isInstalled() ? BigInteger.ONE : weight));
                weight = weight.multiply(POWER);
            }
            if (weight.compareTo(maxWeight) > 0)
                maxWeight = weight;
        }

        // maxWeight = maxWeight.multiply(POWER);
        //
        // // Weight the no-op variables beneath the abstract variables
        // for (Iterator iterator = noopVariables.values().iterator();
        // iterator.hasNext();) {
        // weightedObjects.add(WeightedObject.newWO(iterator.next(),
        // maxWeight));
        // }

        maxWeight = maxWeight.multiply(POWER);

        // DISABLED the complete following block because there was a compile
        // error
        // // Add the abstract variables
        // BigInteger abstractWeight = maxWeight.negate();
        // for (Iterator iterator = abstractVariables.iterator();
        // iterator.hasNext();) {
        // weightedObjects.add(WeightedObject.newWO(iterator.next(),
        // abstractWeight));
        // }

        maxWeight = maxWeight.multiply(POWER);

        BigInteger optionalWeight = maxWeight.negate();
        long countOptional = 1;
        List requestedPatches = new ArrayList();
        IRequiredCapability[] reqs = metaIu.getRequiredCapabilities();
        for (int j = 0; j < reqs.length; j++) {
            if (!reqs[j].isOptional())
                continue;
            Collector matches = picker.query(new CapabilityQuery(reqs[j]),
                    new Collector(), null);
            for (Iterator iterator = matches.iterator(); iterator.hasNext();) {
                InstallableUnit match = (InstallableUnit) iterator.next();
                weightedObjects.add(WeightedObject.newWO(match, optionalWeight));
            }
        }

        BigInteger patchWeight = maxWeight.multiply(POWER).multiply(
                BigInteger.valueOf(countOptional)).negate();
        for (Iterator iterator = requestedPatches.iterator(); iterator.hasNext();) {
            weightedObjects.add(WeightedObject.newWO(iterator.next(),
                    patchWeight));
        }
        if (!weightedObjects.isEmpty()) {
            return weightedObjects;
        }
        return null;
    }

    public String getName() {
        return "p2";
    }

    public String printSolutionValue() {
        // nothing to do
        return "";
    }
}
