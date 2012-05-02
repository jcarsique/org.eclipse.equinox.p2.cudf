/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.metadata;

/**
 * Describes a capability as exposed or required by an installable unit
 */
public class ProvidedCapability implements IProvidedCapability {
	private final String name;
	private final VersionRange version;

	public ProvidedCapability(String name, VersionRange version) {
		this.name = name;
		this.version = version == null ? VersionRange.emptyRange : version;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof IProvidedCapability))
			return false;
		IProvidedCapability otherCapability = (IProvidedCapability) other;
		if (!(name.equals(otherCapability.getName())))
			return false;
		return version.equals(otherCapability.getVersion());
	}

	public String getName() {
		return name;
	}

	public VersionRange getVersion() {
		return version;
	}

	public int hashCode() {
		return name.hashCode() * version.hashCode();
	}

	public String toString() {
		return name + '/' + version;
	}

}
