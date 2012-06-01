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
import org.eclipse.equinox.p2.cudf.solver.Projector.AbstractVariable;
import org.sat4j.core.Vec;
import org.sat4j.pb.tools.LexicoHelper;
import org.sat4j.pb.tools.WeightedObject;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;

public abstract class OptimizationFunction {
    protected TwoTierMap slice;

    protected Map<?, ?> noopVariables;

    protected QueryableArray picker;

    protected LexicoHelper<Object, String> dependencyHelper;

    protected List<AbstractVariable> removalVariables = new ArrayList<AbstractVariable>();

    protected List<AbstractVariable> changeVariables = new ArrayList<AbstractVariable>();

    protected List<AbstractVariable> versionChangeVariables = new ArrayList<AbstractVariable>();

    protected List<AbstractVariable> nouptodateVariables = new ArrayList<AbstractVariable>();

    protected List<AbstractVariable> newVariables = new ArrayList<AbstractVariable>();

    protected List<AbstractVariable> unmetVariables = new ArrayList<AbstractVariable>();

    protected List<AbstractVariable> optionalityVariables;

    protected List<Pair> optionalityPairs;

    public abstract List<WeightedObject<Object>> createOptimizationFunction(
            InstallableUnit metaIu);

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

    protected void removed(Collection<WeightedObject<Object>> weightedObjects,
            BigInteger weight, InstallableUnit metaIu) {
        Set<?> s = slice.entrySet();
        for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection<?> versions = ((Map<?, ?>) entry.getValue()).values();
            boolean installed = false;
            Object[] literals = new Object[versions.size()];
            int i = 0;
            for (Iterator<?> iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iuv = (InstallableUnit) iterator2.next();
                installed = installed || iuv.isInstalled();
                literals[i++] = dependencyHelper.not(iuv);
            }
            if (installed) {
                try {
                    AbstractVariable abs = new AbstractVariable(
                            entry.getKey().toString());
                    removalVariables.add(abs);
                    // abs <=> not iuv1 and ... and not iuvn
                    dependencyHelper.and("OPT1", abs, literals);
                    weightedObjects.add(WeightedObject.newWO((Object) abs,
                            weight));
                } catch (ContradictionException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }

        }
    }

    protected void versionChanged(List<WeightedObject<Object>> weightedObjects,
            BigInteger weight, InstallableUnit metaIu) {
        Set<?> s = slice.entrySet();
        for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection<?> versions = ((Map<?, ?>) entry.getValue()).values();
            boolean installed = false;
            IVec<InstallableUnit> changed = new Vec<InstallableUnit>(
                    versions.size());
            for (Iterator<?> iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iu = (InstallableUnit) iterator2.next();
                installed = installed || iu.isInstalled();
                if (!iu.isInstalled()) {
                    changed.push(iu);
                }
            }
            if (installed) {
                InstallableUnit[] changedarray = new InstallableUnit[changed.size()];
                changed.copyTo(changedarray);
                try {
                    AbstractVariable abs = new AbstractVariable(
                            entry.getKey().toString());
                    versionChangeVariables.add(abs);
                    // abs <=> iuv1 or not iuv2 or ... or not iuvn
                    dependencyHelper.or("OPT3", abs, (Object[]) changedarray);
                    weightedObjects.add(WeightedObject.newWO((Object) abs,
                            weight));
                } catch (ContradictionException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }
        }
    }

    protected void changed(Collection<WeightedObject<Object>> weightedObjects,
            BigInteger weight, InstallableUnit metaIu) {
        Set<?> s = slice.entrySet();
        for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            Collection<?> versions = ((Map<?, ?>) entry.getValue()).values();
            Object[] changed = new Object[versions.size()];
            int i = 0;
            for (Iterator<?> iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iu = (InstallableUnit) iterator2.next();

                changed[i++] = (iu.isInstalled() ? dependencyHelper.not(iu)
                        : iu);
            }
            try {
                AbstractVariable abs = new AbstractVariable(
                        entry.getKey().toString());
                changeVariables.add(abs);
                // abs <=> iuv1 or not iuv2 or ... or not iuvn
                dependencyHelper.or("OPT3", abs, changed);
                weightedObjects.add(WeightedObject.newWO((Object) abs, weight));
            } catch (ContradictionException e) {
                // should not happen
                e.printStackTrace();
            }
        }
    }

    protected void uptodate(List<WeightedObject<?>> weightedObjects,
            BigInteger weight, InstallableUnit metaIu) {
        Set<?> s = slice.entrySet();
        for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            @SuppressWarnings("rawtypes")
            Map versions = (Map) entry.getValue();
            @SuppressWarnings("unchecked")
            List<InstallableUnit> toSort = new ArrayList<InstallableUnit>(
                    versions.values());
            Collections.sort(toSort, Collections.reverseOrder());
            weightedObjects.add(WeightedObject.newWO(toSort.get(0), weight));
        }
    }

    protected void notuptodate(
            Collection<WeightedObject<Object>> weightedObjects,
            BigInteger weight, InstallableUnit metaIu) {
        Set<?> s = slice.entrySet();
        for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            @SuppressWarnings("rawtypes")
            Map versions = (Map) entry.getValue();
            @SuppressWarnings("unchecked")
            List<AbstractVariable> toSort = new ArrayList<AbstractVariable>(
                    versions.values());
            Collections.sort(toSort, Collections.reverseOrder());
            AbstractVariable abs = new AbstractVariable(
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

            weightedObjects.add(WeightedObject.newWO((Object) abs, weight));
            nouptodateVariables.add(abs);
        }
    }

    protected void unmetRecommends(
            List<WeightedObject<Object>> weightedObjects, BigInteger weight,
            InstallableUnit metaIu) {
        for (Iterator<Pair> iterator = optionalityPairs.iterator(); iterator.hasNext();) {
            Pair entry = iterator.next();
            if (entry.left == metaIu) {
                // weightedObjects.add(WeightedObject.newWO(entry.right,
                // weight));
                continue;
            }

            AbstractVariable abs = new AbstractVariable(entry.left.toString()
                    + entry.right);
            try {
                dependencyHelper.and("OPTX", abs, new Object[] { entry.right,
                        entry.left });
            } catch (ContradictionException e) {
                // should never happen
                e.printStackTrace();
            }
            weightedObjects.add(WeightedObject.newWO((Object) abs, weight));
            unmetVariables.add(abs);
        }
    }

    protected void niou(Collection<WeightedObject<Object>> weightedObjects,
            BigInteger weight, InstallableUnit metaIu) {
        Set<?> s = slice.entrySet();
        for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Collection<InstallableUnit> versions = ((Map) entry.getValue()).values();
            boolean installed = false;
            for (Iterator<InstallableUnit> iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iuv = iterator2.next();
                installed = installed || iuv.isInstalled();
            }
            if (!installed) {
                try {
                    AbstractVariable abs = new AbstractVariable(
                            entry.getKey().toString());
                    newVariables.add(abs);
                    // a <=> iuv1 or ... or iuvn
                    AbstractVariable[] clause = new AbstractVariable[versions.size()];
                    versions.toArray(clause);
                    dependencyHelper.or("OPT2", abs, (Object[]) clause);
                    weightedObjects.add(WeightedObject.newWO((Object) abs,
                            weight));
                } catch (ContradictionException e) {
                    // should not happen
                    e.printStackTrace();
                }
            }

        }
    }

    protected void optional(List<WeightedObject<Object>> weightedObjects,
            BigInteger weight, InstallableUnit metaIu) {
        for (Iterator<Pair> it = optionalityPairs.iterator(); it.hasNext();) {
            Pair pair = it.next();
            if (pair.left != metaIu) {
                weightedObjects.add(WeightedObject.newWO((Object) pair.right,
                        weight));
                unmetVariables.add(pair.right);
            }
        }
    }

    protected void sum(List<WeightedObject<Object>> weightedObjects,
            boolean minimize, InstallableUnit metaIu, String sumProperty) {
        Set<?> s = slice.entrySet();
        for (Iterator<?> iterator = s.iterator(); iterator.hasNext();) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getKey() == metaIu.getId())
                continue;
            @SuppressWarnings("rawtypes")
            Collection versions = ((Map) entry.getValue()).values();
            for (@SuppressWarnings("unchecked")
            Iterator<InstallableUnit> iterator2 = versions.iterator(); iterator2.hasNext();) {
                InstallableUnit iuv = iterator2.next();
                if (iuv.getSumProperty() != 0) {
                    BigInteger weight = BigInteger.valueOf(iuv.getSumProperty());
                    weightedObjects.add(WeightedObject.newWO((Object) iuv,
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

    private List<String> getAsStringList(List<AbstractVariable> variables) {
        List<String> list = new ArrayList<String>();
        for (AbstractVariable var : variables) {
            if (dependencyHelper.getBooleanValueFor(var)) {
                list.add(var.toString().substring(18));
            }
        }
        return list;
    }

}
