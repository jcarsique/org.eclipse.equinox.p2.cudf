package org.eclipse.equinox.p2.cudf.solver;

import java.math.BigInteger;
import java.util.*;
import org.eclipse.equinox.p2.cudf.Options;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.sat4j.pb.tools.WeightedObject;
import org.sat4j.specs.IVec;

public class UserDefinedOptimizationFunction extends OptimizationFunction {

	private String optfunction;

	public UserDefinedOptimizationFunction(String optfunction) {
		this.optfunction = optfunction;
	}

	public List createOptimizationFunction(InstallableUnit metaIu) {
		List weightedObjects = new ArrayList();
		List objects = new ArrayList();
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
				niou(weightedObjects, criteria[i].startsWith("+") ? currentWeight.negate() : currentWeight, metaIu);
				currentWeight = currentWeight.divide(weight);
			} else if (criteria[i].endsWith("removed")) {
				weightedObjects.clear();
				removed(weightedObjects, criteria[i].startsWith("+") ? currentWeight.negate() : currentWeight, metaIu);
				currentWeight = currentWeight.divide(weight);
			} else if (criteria[i].endsWith("notuptodate")) {
				weightedObjects.clear();
				notuptodate(weightedObjects, criteria[i].startsWith("+") ? currentWeight.negate() : currentWeight, metaIu);
				currentWeight = currentWeight.divide(weight);
			} else if (criteria[i].endsWith("unsat_recommends")) {
				weightedObjects.clear();
				optional(weightedObjects, criteria[i].startsWith("+") ? currentWeight.negate() : currentWeight, metaIu);
				currentWeight = currentWeight.divide(weight);
			} else if (criteria[i].endsWith("versionchanged")) {
				weightedObjects.clear();
				versionChanged(weightedObjects, criteria[i].startsWith("+") ? currentWeight.negate() : currentWeight, metaIu);
			} else if (criteria[i].endsWith("changed")) {
				weightedObjects.clear();
				changed(weightedObjects, criteria[i].startsWith("+") ? currentWeight.negate() : currentWeight, metaIu);
				currentWeight = currentWeight.divide(weight);
			} else if (criteria[i].contains("sum")) {
				weightedObjects.clear();
				sum(weightedObjects, criteria[i].charAt(0) == '-', metaIu, Options.extractSumProperty(criteria[i]));
				dependencyHelper.addWeightedCriterion(weightedObjects);
				System.out.println("# criteria " + criteria[i].substring(1) + " size is " + weightedObjects.size());
				continue;
			} else {
				System.out.println("Skipping unknown criteria:" + criteria[i]);
			}
			objects.clear();
			maximizes = criteria[i].startsWith("+");
			for (Iterator it = weightedObjects.iterator(); it.hasNext();) {
				thing = ((WeightedObject) it.next()).thing;
				if (maximizes) {
					thing = dependencyHelper.not(thing);
				}
				objects.add(thing);
			}
			dependencyHelper.addCriterion(objects);
			newmaxvarid = dependencyHelper.getSolver().nextFreeVarId(false);
			System.out.println("# criteria " + criteria[i].substring(1) + " size is " + objects.size() + " using new vars " + formermaxvarid + " to " + newmaxvarid);
			formermaxvarid = newmaxvarid;
		}
		weightedObjects.clear();
		return null;
	}

	public String getName() {
		return "User defined:" + optfunction;
	}

	public void printSolutionValue() {
		int counter;
		List proof = new ArrayList();
		String[] criteria = optfunction.split(",");
		for (int i = 0; i < criteria.length; i++) {
			if (criteria[i].endsWith("new")) {
				proof.clear();
				counter = 0;
				for (int j = 0; j < newVariables.size(); j++) {
					Object var = newVariables.get(j);
					if (dependencyHelper.getBooleanValueFor(var)) {
						counter++;
						proof.add(var.toString().substring(18));
					}
				}
				System.out.println("# " + criteria[i] + " criteria value: " + counter);
				System.out.println("# Newly installed packages: " + proof);
				continue;
			}
			if (criteria[i].endsWith("removed")) {
				proof.clear();
				counter = 0;
				for (int j = 0; j < removalVariables.size(); j++) {
					Object var = removalVariables.get(j);
					if (dependencyHelper.getBooleanValueFor(var)) {
						counter++;
						proof.add(var.toString().substring(18));
					}
				}
				System.out.println("# " + criteria[i] + " criteria value: " + counter);
				System.out.println("# Removed packages: " + proof);
				continue;
			}
			if (criteria[i].endsWith("notuptodate")) {
				proof.clear();
				counter = 0;
				for (int j = 0; j < nouptodateVariables.size(); j++) {
					Object var = nouptodateVariables.get(j);
					if (dependencyHelper.getBooleanValueFor(var)) {
						counter++;
						proof.add(var.toString().substring(18));
					}
				}
				System.out.println("# " + criteria[i] + " criteria value: " + counter);
				System.out.println("# Not up-to-date packages: " + proof);
				continue;
			}
			if (criteria[i].endsWith("recommended") || criteria[i].endsWith("unsat_recommends")) {
				proof.clear();
				counter = 0;
				for (Iterator it = unmetVariables.iterator(); it.hasNext();) {
					Object var = it.next();
					if (dependencyHelper.getBooleanValueFor(var)) {
						counter++;
						proof.add(var.toString().substring(18));
					}
				}
				System.out.println("# " + criteria[i] + " criteria value: " + counter);
				System.out.println("# Not installed recommended packages: " + proof);
				continue;
			}
			if (criteria[i].endsWith("versionchanged")) {
				proof.clear();
				counter = 0;
				for (int j = 0; j < versionChangeVariables.size(); j++) {
					Object var = versionChangeVariables.get(j);
					if (dependencyHelper.getBooleanValueFor(var)) {
						counter++;
						proof.add(var.toString().substring(18));
					}
				}
				System.out.println("# " + criteria[i] + " criteria value: " + counter);
				System.out.println("# Packages with version change: " + proof);
				continue;
			}
			if (criteria[i].endsWith("changed")) {
				proof.clear();
				counter = 0;
				for (int j = 0; j < changeVariables.size(); j++) {
					Object var = changeVariables.get(j);
					if (dependencyHelper.getBooleanValueFor(var)) {
						counter++;
						proof.add(var.toString().substring(18));
					}
				}
				System.out.println("# " + criteria[i] + " criteria value: " + counter);
				System.out.println("# Changed packages: " + proof);
				continue;
			}
			if (criteria[i].contains("sum")) {
				String sumpProperty = Options.extractSumProperty(criteria[i]);
				long sum = 0;
				IVec sol = dependencyHelper.getSolution();
				for (Iterator it = sol.iterator(); it.hasNext();) {
					Object element = it.next();
					if (element instanceof InstallableUnit) {
						InstallableUnit iu = (InstallableUnit) element;
						sum += iu.getSumProperty();
					}
				}
				System.out.println("# " + criteria[i] + " criteria value: " + sum);
				continue;
			}
		}
	}
}
