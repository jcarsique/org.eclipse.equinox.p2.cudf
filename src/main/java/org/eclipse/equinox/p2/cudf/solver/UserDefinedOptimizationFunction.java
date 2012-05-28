package org.eclipse.equinox.p2.cudf.solver;

import java.math.BigInteger;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.Options;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.Projector.AbstractVariable;
import org.sat4j.pb.tools.WeightedObject;
import org.sat4j.specs.IVec;

public class UserDefinedOptimizationFunction extends OptimizationFunction {
    private static final Log log = LogFactory.getLog(UserDefinedOptimizationFunction.class);

    private String optfunction;

    public UserDefinedOptimizationFunction(String optfunction) {
        this.optfunction = optfunction;
    }

    public List<WeightedObject<Object>> createOptimizationFunction(
            InstallableUnit metaIu) {
        List<WeightedObject<Object>> weightedObjects = new ArrayList<WeightedObject<Object>>();
        List<Object> objects = new ArrayList<Object>();
        BigInteger weight = BigInteger.valueOf(slice.size() + 1);
        String[] criteria = optfunction.split(",");
        BigInteger currentWeight = weight.pow(criteria.length - 1);
        int formermaxvarid = dependencyHelper.getSolver().nextFreeVarId(false);
        int newmaxvarid;
        boolean maximizes;
        Object thing;
        for (int i = 0; i < criteria.length; i++) {
            if (criteria[i].endsWith("new")) {
                weightedObjects.clear();
                niou(weightedObjects,
                        criteria[i].startsWith("+") ? currentWeight.negate()
                                : currentWeight, metaIu);
                currentWeight = currentWeight.divide(weight);
            } else if (criteria[i].endsWith("removed")) {
                weightedObjects.clear();
                removed(weightedObjects,
                        criteria[i].startsWith("+") ? currentWeight.negate()
                                : currentWeight, metaIu);
                currentWeight = currentWeight.divide(weight);
            } else if (criteria[i].endsWith("notuptodate")) {
                weightedObjects.clear();
                notuptodate(weightedObjects,
                        criteria[i].startsWith("+") ? currentWeight.negate()
                                : currentWeight, metaIu);
                currentWeight = currentWeight.divide(weight);
            } else if (criteria[i].endsWith("unsat_recommends")) {
                weightedObjects.clear();
                optional(weightedObjects,
                        criteria[i].startsWith("+") ? currentWeight.negate()
                                : currentWeight, metaIu);
                currentWeight = currentWeight.divide(weight);
            } else if (criteria[i].endsWith("versionchanged")) {
                weightedObjects.clear();
                versionChanged(weightedObjects,
                        criteria[i].startsWith("+") ? currentWeight.negate()
                                : currentWeight, metaIu);
            } else if (criteria[i].endsWith("changed")) {
                weightedObjects.clear();
                changed(weightedObjects,
                        criteria[i].startsWith("+") ? currentWeight.negate()
                                : currentWeight, metaIu);
                currentWeight = currentWeight.divide(weight);
            } else if (criteria[i].contains("sum")) {
                weightedObjects.clear();
                sum(weightedObjects, criteria[i].charAt(0) == '-', metaIu,
                        Options.extractSumProperty(criteria[i]));
                dependencyHelper.addWeightedCriterion(weightedObjects);
                log.info("# criteria " + criteria[i].substring(1) + " size is "
                        + weightedObjects.size());
                continue;
            } else {
                log.info("Skipping unknown criteria:" + criteria[i]);
            }
            objects.clear();
            maximizes = criteria[i].startsWith("+");
            for (Iterator<WeightedObject<Object>> it = weightedObjects.iterator(); it.hasNext();) {
                thing = (it.next()).thing;
                if (maximizes) {
                    thing = dependencyHelper.not(thing);
                }
                objects.add(thing);
            }
            dependencyHelper.addCriterion(objects);
            newmaxvarid = dependencyHelper.getSolver().nextFreeVarId(false);
            log.info("# criteria " + criteria[i].substring(1) + " size is "
                    + objects.size() + " using new vars " + formermaxvarid
                    + " to " + newmaxvarid);
            formermaxvarid = newmaxvarid;
        }
        weightedObjects.clear();
        return null;
    }

    public String getName() {
        return "User defined:" + optfunction;
    }

    public String printSolutionValue() {
        StringBuilder sb = new StringBuilder();
        int counter;
        List<String> proof = new ArrayList<String>();
        String[] criteria = optfunction.split(",");
        for (int i = 0; i < criteria.length; i++) {
            if (criteria[i].endsWith("new")) {
                proof.clear();
                counter = 0;
                for (int j = 0; j < newVariables.size(); j++) {
                    AbstractVariable var = newVariables.get(j);
                    if (dependencyHelper.getBooleanValueFor(var)) {
                        counter++;
                        proof.add(var.toString().substring(18));
                    }
                }
                sb.append("# " + criteria[i] + " criteria value: " + counter
                        + "\n");
                sb.append("# Newly installed packages: " + proof + "\n");
                continue;
            }
            if (criteria[i].endsWith("removed")) {
                proof.clear();
                counter = 0;
                for (int j = 0; j < removalVariables.size(); j++) {
                    AbstractVariable var = removalVariables.get(j);
                    if (dependencyHelper.getBooleanValueFor(var)) {
                        counter++;
                        proof.add(var.toString().substring(18));
                    }
                }
                sb.append("# " + criteria[i] + " criteria value: " + counter
                        + "\n");
                sb.append("# Removed packages: " + proof + "\n");
                continue;
            }
            if (criteria[i].endsWith("notuptodate")) {
                proof.clear();
                counter = 0;
                for (int j = 0; j < nouptodateVariables.size(); j++) {
                    AbstractVariable var = nouptodateVariables.get(j);
                    if (dependencyHelper.getBooleanValueFor(var)) {
                        counter++;
                        proof.add(var.toString().substring(18));
                    }
                }
                sb.append("# " + criteria[i] + " criteria value: " + counter
                        + "\n");
                sb.append("# Not up-to-date packages: " + proof + "\n");
                continue;
            }
            if (criteria[i].endsWith("recommended")
                    || criteria[i].endsWith("unsat_recommends")) {
                proof.clear();
                counter = 0;
                for (Iterator<AbstractVariable> it = unmetVariables.iterator(); it.hasNext();) {
                    AbstractVariable var = it.next();
                    if (dependencyHelper.getBooleanValueFor(var)) {
                        counter++;
                        proof.add(var.toString().substring(18));
                    }
                }
                sb.append("# " + criteria[i] + " criteria value: " + counter
                        + "\n");
                sb.append("# Not installed recommended packages: " + proof
                        + "\n");
                continue;
            }
            if (criteria[i].endsWith("versionchanged")) {
                proof.clear();
                counter = 0;
                for (int j = 0; j < versionChangeVariables.size(); j++) {
                    AbstractVariable var = versionChangeVariables.get(j);
                    if (dependencyHelper.getBooleanValueFor(var)) {
                        counter++;
                        proof.add(var.toString().substring(18));
                    }
                }
                sb.append("# " + criteria[i] + " criteria value: " + counter
                        + "\n");
                sb.append("# Packages with version change: " + proof + "\n");
                continue;
            }
            if (criteria[i].endsWith("changed")) {
                proof.clear();
                counter = 0;
                for (int j = 0; j < changeVariables.size(); j++) {
                    AbstractVariable var = changeVariables.get(j);
                    if (dependencyHelper.getBooleanValueFor(var)) {
                        counter++;
                        proof.add(var.toString().substring(18));
                    }
                }
                sb.append("# " + criteria[i] + " criteria value: " + counter
                        + "\n");
                sb.append("# Changed packages: " + proof + "\n");
                continue;
            }
            if (criteria[i].contains("sum")) {
                String sumpProperty = Options.extractSumProperty(criteria[i]);
                log.debug("sumpProperty: " + sumpProperty);
                long sum = 0;
                IVec<Object> sol = dependencyHelper.getSolution();
                for (Iterator<Object> it = sol.iterator(); it.hasNext();) {
                    Object element = it.next();
                    if (element instanceof InstallableUnit) {
                        InstallableUnit iu = (InstallableUnit) element;
                        sum += iu.getSumProperty();
                    }
                }
                sb.append("# " + criteria[i] + " criteria value: " + sum + "\n");
                continue;
            }
        }
        return sb.toString();
    }
}
