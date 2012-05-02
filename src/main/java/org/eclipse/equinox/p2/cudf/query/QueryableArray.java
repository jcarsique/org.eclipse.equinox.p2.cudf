/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.query;

import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.cudf.metadata.*;

public class QueryableArray implements IQueryable {
	static class IUCapability {
		final InstallableUnit iu;
		final IProvidedCapability capability;

		public IUCapability(InstallableUnit iu, IProvidedCapability capability) {
			this.iu = iu;
			this.capability = capability;
		}
	}

	private final List dataSet;
	private Map namedCapabilityIndex;

	public QueryableArray(InstallableUnit[] ius) {
		dataSet = Arrays.asList(ius);
	}

	public List getList() {
		return dataSet;
	}

	public Collector query(Query query, Collector collector, IProgressMonitor monitor) {
		if (query instanceof CapabilityQuery)
			return queryCapability((CapabilityQuery) query, collector, monitor);
		throw new IllegalArgumentException();
	}

	private Collector queryCapability(CapabilityQuery query, Collector collector, IProgressMonitor monitor) {
		generateNamedCapabilityIndex();

		Collection resultIUs = new ArrayList();
		IRequiredCapability iRequiredCapability = query.getRequiredCapabilities();
		if (iRequiredCapability instanceof ORRequirement) {
			IRequiredCapability[] ored = ((ORRequirement) iRequiredCapability).getRequirements();
			for (int j = 0; j < ored.length; j++) {
				Collection orMatches = findMatchingIUs(ored[j]);
				if (orMatches != null)
					resultIUs.addAll(orMatches);
			}
		} else {
			Collection matchingIUs = findMatchingIUs(iRequiredCapability);
			if (matchingIUs == null)
				return collector;
			resultIUs.addAll(matchingIUs);
		}

		for (Iterator iterator = resultIUs.iterator(); iterator.hasNext();)
			collector.accept(iterator.next());

		return collector;
	}

	private Collection findMatchingIUs(IRequiredCapability requiredCapability) {
		List iuCapabilities = (List) namedCapabilityIndex.get(requiredCapability.getName());
		if (iuCapabilities == null)
			return null;

		Set matchingIUs = new HashSet();
		for (Iterator iterator = iuCapabilities.iterator(); iterator.hasNext();) {
			IUCapability iuCapability = (IUCapability) iterator.next();
			if (intersect(requiredCapability.getRange(), iuCapability.capability.getVersion()) != null)
				matchingIUs.add(iuCapability.iu);
		}
		return matchingIUs;
	}

	private void generateNamedCapabilityIndex() {
		if (namedCapabilityIndex != null)
			return;

		namedCapabilityIndex = new HashMap();
		for (Iterator iterator = dataSet.iterator(); iterator.hasNext();) {
			InstallableUnit iu = (InstallableUnit) iterator.next();

			IProvidedCapability[] providedCapabilities = iu.getProvidedCapabilities();
			for (int i = 0; i < providedCapabilities.length; i++) {
				String name = providedCapabilities[i].getName();
				List iuCapabilities = (List) namedCapabilityIndex.get(name);
				if (iuCapabilities == null) {
					iuCapabilities = new ArrayList(5);
					namedCapabilityIndex.put(name, iuCapabilities);
				}
				iuCapabilities.add(new IUCapability(iu, providedCapabilities[i]));
			}
		}
	}

	public Iterator iterator() {
		return dataSet.iterator();
	}

	private VersionRange intersect(VersionRange r1, VersionRange r2) {
		Version resultMin = null;
		boolean resultMinIncluded = false;
		Version resultMax = null;
		boolean resultMaxIncluded = false;

		int minCompare = r1.getMinimum().compareTo(r2.getMinimum());
		if (minCompare < 0) {
			resultMin = r2.getMinimum();
			resultMinIncluded = r2.getIncludeMinimum();
		} else if (minCompare > 0) {
			resultMin = r1.getMinimum();
			resultMinIncluded = r1.getIncludeMinimum();
		} else if (minCompare == 0) {
			resultMin = r1.getMinimum();
			resultMinIncluded = r1.getIncludeMinimum() && r2.getIncludeMinimum();
		}

		int maxCompare = r1.getMaximum().compareTo(r2.getMaximum());
		if (maxCompare > 0) {
			resultMax = r2.getMaximum();
			resultMaxIncluded = r2.getIncludeMaximum();
		} else if (maxCompare < 0) {
			resultMax = r1.getMaximum();
			resultMaxIncluded = r1.getIncludeMaximum();
		} else if (maxCompare == 0) {
			resultMax = r1.getMaximum();
			resultMaxIncluded = r1.getIncludeMaximum() && r2.getIncludeMaximum();
		}

		int resultRangeComparison = resultMin.compareTo(resultMax);
		if (resultRangeComparison < 0)
			return new VersionRange(resultMin, resultMinIncluded, resultMax, resultMaxIncluded);
		else if (resultRangeComparison == 0 && resultMinIncluded == resultMaxIncluded)
			return new VersionRange(resultMin, resultMinIncluded, resultMax, resultMaxIncluded);
		else
			return null;
	}

	public int getSize() {
		return dataSet.size();
	}
}
