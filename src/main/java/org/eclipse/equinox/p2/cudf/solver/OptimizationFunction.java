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
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.query.QueryableArray;
import org.sat4j.core.Vec;
import org.sat4j.pb.tools.LexicoHelper;
import org.sat4j.pb.tools.WeightedObject;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;

public abstract class OptimizationFunction {
    protected Map slice;

    protected Map noopVariables;

    protected QueryableArray picker;

    protected LexicoHelper dependencyHelper;

    protected List removalVariables = new ArrayList();

    protected List changeVariables = new ArrayList();

    protected List versionChangeVariables = new ArrayList();

    protected List nouptodateVariables = new ArrayList();

    protected List newVariables = new ArrayList();

    protected List unmetVariables = new ArrayList();

    protected List optionalityVariables;

    protected List optionalityPairs;

    public abstract List createOptimizationFunction(InstallableUnit metaIu);

    public abstract String printSolutionValue();

    /**
     * @since 1.14
     */
    public enum Criteria {
        /**
         * Newly installed packages
         */
        NEW("new"),
        /**
         * Removed packages
         */
        REMOVED("removed"),
        /**
         * Not up-to-date packages
         */
        NOTUPTODATE("notuptodate"),
        /**
         * Not installed recommended packages
         */
        RECOMMENDED("recommended"),
        /**
         * Packages with version change
         */
        VERSION_CHANGED("versionchanged"),
        /**
         * Changed packages
         */
        CHANGED("changed");

        public final String label;

        private Criteria(String label) {
            this.label = label;
        }
    }

    protected void removed(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection versions = ((HashMap) entry.getValue()).values();
            boolean installed = false;
            Object[] literals = new Object[versions.size()];
            int i = 0;
            for (Iterator iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iuv = (InstallableUnit) iterator2.next();
                installed = installed || iuv.isInstalled();
                literals[i++] = dependencyHelper.not(iuv);
            }
            if (installed) {
                try {
                    Projector.AbstractVariable abs = new Projector.AbstractVariable(
                            entry.getKey().toString());
                    removalVariables.add(abs);
                    // abs <=> not iuv1 and ... and not iuvn
                    dependencyHelper.and("OPT1", abs, literals);
                    weightedObjects.add(WeightedObject.newWO(abs, weight));
                } catch (ContradictionException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }

        }
    }

    protected void versionChanged(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection versions = ((HashMap) entry.getValue()).values();
            boolean installed = false;
            IVec<InstallableUnit> changed = new Vec<InstallableUnit>(
                    versions.size());
            for (Iterator iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iu = (InstallableUnit) iterator2.next();
                installed = installed || iu.isInstalled();
                if (!iu.isInstalled()) {
                    changed.push(iu);
                }
            }
            if (installed) {
                Object[] changedarray = new Object[changed.size()];
                changed.copyTo(changedarray);
                try {
                    Projector.AbstractVariable abs = new Projector.AbstractVariable(
                            entry.getKey().toString());
                    versionChangeVariables.add(abs);
                    // abs <=> iuv1 or not iuv2 or ... or not iuvn
                    dependencyHelper.or("OPT3", abs, changedarray);
                    weightedObjects.add(WeightedObject.newWO(abs, weight));
                } catch (ContradictionException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }
        }
    }

    protected void changed(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection versions = ((HashMap) entry.getValue()).values();
            Object[] changed = new Object[versions.size()];
            int i = 0;
            for (Iterator iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iu = (InstallableUnit) iterator2.next();

                changed[i++] = iu.isInstalled() ? dependencyHelper.not(iu) : iu;
            }
            try {
                Projector.AbstractVariable abs = new Projector.AbstractVariable(
                        entry.getKey().toString());
                changeVariables.add(abs);
                // abs <=> iuv1 or not iuv2 or ... or not iuvn
                dependencyHelper.or("OPT3", abs, changed);
                weightedObjects.add(WeightedObject.newWO(abs, weight));
            } catch (ContradictionException e) {
                // should not happen
                e.printStackTrace();
            }
        }
    }

    protected void uptodate(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            HashMap versions = (HashMap) entry.getValue();
            List toSort = new ArrayList(versions.values());
            Collections.sort(toSort, Collections.reverseOrder());
            weightedObjects.add(WeightedObject.newWO(toSort.get(0), weight));
        }
    }

    protected void notuptodate(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            HashMap versions = (HashMap) entry.getValue();
            List toSort = new ArrayList(versions.values());
            Collections.sort(toSort, Collections.reverseOrder());
            Projector.AbstractVariable abs = new Projector.AbstractVariable(
                    entry.getKey().toString());
            Object notlatest = dependencyHelper.not(toSort.get(0));
            try {
                // notuptodate <=> not iuvn and (iuv1 or iuv2 or ... iuvn-1)
                dependencyHelper.implication(new Object[] { abs }).implies(
                        notlatest).named("OPT4");
                Object[] clause = new Object[toSort.size()];
                toSort.toArray(clause);
                clause[0] = dependencyHelper.not(abs);
                dependencyHelper.clause("OPT4", clause);
                for (int i = 1; i < toSort.size(); i++) {
                    dependencyHelper.implication(
                            new Object[] { notlatest, toSort.get(i) }).implies(
                            abs).named("OPT4");
                }
            } catch (ContradictionException e) {
                // should never happen
                e.printStackTrace();
            }

            weightedObjects.add(WeightedObject.newWO(abs, weight));
            nouptodateVariables.add(abs);
        }
    }

    protected void unmetRecommends(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        for (Iterator iterator = optionalityPairs.iterator(); iterator.hasNext();) {
            Pair entry = (Pair) iterator.next();
            if (entry.left == metaIu) {
                // weightedObjects.add(WeightedObject.newWO(entry.right,
                // weight));
                continue;
            }

            Projector.AbstractVariable abs = new Projector.AbstractVariable(
                    entry.left.toString() + entry.right);
            try {
                dependencyHelper.and("OPTX", abs, new Object[] { entry.right,
                        entry.left });
            } catch (ContradictionException e) {
                // should never happen
                e.printStackTrace();
            }
            weightedObjects.add(WeightedObject.newWO(abs, weight));
            unmetVariables.add(abs);
        }
    }

    protected void niou(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection versions = ((HashMap) entry.getValue()).values();
            boolean installed = false;
            for (Iterator iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iuv = (InstallableUnit) iterator2.next();
                installed = installed || iuv.isInstalled();
            }
            if (!installed) {
                try {
                    Projector.AbstractVariable abs = new Projector.AbstractVariable(
                            entry.getKey().toString());
                    newVariables.add(abs);
                    // a <=> iuv1 or ... or iuvn
                    Object[] clause = new Object[versions.size()];
                    versions.toArray(clause);
                    dependencyHelper.or("OPT2", abs, clause);
                    weightedObjects.add(WeightedObject.newWO(abs, weight));
                } catch (ContradictionException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }

        }
    }

    protected void optional(List weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        for (Iterator it = optionalityPairs.iterator(); it.hasNext();) {
            Pair pair = (Pair) it.next();
            if (pair.left != metaIu) {
                weightedObjects.add(WeightedObject.newWO(pair.right, weight));
                unmetVariables.add(pair.right);
            }
        }
    }

    protected void sum(List weightedObjects, boolean minimize,
            InstallableUnit metaIu, String sumProperty) {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection versions = ((HashMap) entry.getValue()).values();
            for (Iterator iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iuv = (InstallableUnit) iterator2.next();
                if (iuv.getSumProperty() != 0) {
                    BigInteger weight = BigInteger.valueOf(iuv.getSumProperty());
                    weightedObjects.add(WeightedObject.newWO(iuv,
                            minimize ? weight : weight.negate()));
                }
            }
        }
    }

    public abstract String getName();

    /**
     * @since 1.14
     * @return a map of IU names per {@link Criteria}
     */
    public Map<Criteria, List<String>> getSolutionDetails() {
        Map<Criteria, List<String>> details = new HashMap<Criteria, List<String>>();
        details.put(Criteria.NEW, getAsStringList(newVariables));
        details.put(Criteria.REMOVED, getAsStringList(removalVariables));
        details.put(Criteria.NOTUPTODATE, getAsStringList(nouptodateVariables));
        details.put(Criteria.RECOMMENDED, getAsStringList(unmetVariables));
        details.put(Criteria.VERSION_CHANGED,
                getAsStringList(versionChangeVariables));
        details.put(Criteria.CHANGED, getAsStringList(changeVariables));
        return details;
    }

    private List<String> getAsStringList(List variables) {
        List<String> list = new ArrayList<String>();
        for (Object var : variables) {
            if (dependencyHelper.getBooleanValueFor(var)) {
                list.add(var.toString().substring(18));
            }
        }
        return list;
    }

}
