/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.metadata;

/**
 * A required capability represents some external constraint on an {@link IInstallableUnit}.
 * Each capability represents something an {@link IInstallableUnit} needs that
 * it expects to be provided by another {@link IInstallableUnit}. Capabilities are
 * entirely generic, and are intended to be capable of representing anything that
 * an {@link IInstallableUnit} may need either at install time, or at runtime.
 * <p>
 * Capabilities are segmented into namespaces.  Anyone can introduce new 
 * capability namespaces. Some well-known namespaces are introduced directly
 * by the provisioning framework.
 * 
 * @see IInstallableUnit#NAMESPACE_IU_ID
 */
public class RequiredCapability implements IRequiredCapability {
	private final String name;//never null
	private final VersionRange range;//never null
	private boolean optional;
	private int arity;

	public RequiredCapability(String name, VersionRange range) {
		this.name = name;
		this.range = range == null ? VersionRange.emptyRange : range;
	}

	public RequiredCapability(String name, VersionRange range, boolean optional) {
		this(name, range);
		this.optional = optional;
	}

	public RequiredCapability(String name, VersionRange range, int arity) {
		this(name, range);
		this.arity = arity;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IRequiredCapability))
			return false;
		final IRequiredCapability other = (IRequiredCapability) obj;
		if (!name.equals(other.getName()))
			return false;
		if (!range.equals(other.getRange()))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the range of versions that satisfy this required capability. Returns
	 * an empty version range ({@link VersionRange#emptyRange} if any version
	 * will satisfy the capability.
	 * @return the range of versions that satisfy this required capability.
	 */
	public VersionRange getRange() {
		return range;
	}

	/**
	 * Returns the properties to use for evaluating required capability filters 
	 * downstream from this capability. For example, if the selector "doc"
	 * is provided, then a downstream InstallableUnit with a required capability
	 * filtered with "doc=true" will be included.
	 */

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		result = prime * result + range.hashCode();
		return result;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(getName());
		result.append(' ');
		//for an exact version match, print a simpler expression
		if (range.getMinimum().equals(range.getMaximum()))
			result.append('[').append(range.getMinimum()).append(']');
		else
			result.append(range);
		return result.toString();
	}

	public boolean isNegation() {
		return false;
	}

	public boolean isOptional() {
		return optional;
	}

	public int getArity() {
		return arity;
	}

	public void setArity(int arity) {
		this.arity = arity;
	}
}
