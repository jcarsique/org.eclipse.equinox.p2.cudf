/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.query;

import org.eclipse.equinox.p2.cudf.metadata.IRequiredCapability;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;


/**
 * A query that searches for {@link InstallableUnit} instances that provide
 * capabilities that match one or more required capabilities.
 */
public class CapabilityQuery extends MatchQuery {
	private IRequiredCapability required;

	/**
	 * Creates a new query on the given required capability.
	 * @param required The required capability
	 */
	public CapabilityQuery(IRequiredCapability required) {
		this.required = required;
	}

	/**
	 * Returns the required capability that this query is matching against.
	 * @return the required capability that this query is matching against.
	 */
	public IRequiredCapability getRequiredCapabilities() {
		return required;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.p2.query2.Query#isMatch(java.lang.Object)
	 */
	public boolean isMatch(Object object) {
//		if (!(object instanceof InstallableUnit))
//			return false;
//		InstallableUnit candidate = (InstallableUnit) object;
//			if (!candidate.satisfies(required))
//				return false;
		return true;
	}
}
