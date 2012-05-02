/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genuitec, LLC - added license support
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.metadata;

public class InstallableUnit implements Comparable {

	public static final IProvidedCapability[] NO_PROVIDES = new IProvidedCapability[0];
	public static final IRequiredCapability[] NO_REQUIRES = new IRequiredCapability[0];
	public static final String NAMESPACE_IU_ID = "org.eclipse.equinox.p2.iu"; //$NON-NLS-1$

	private String id;
	private Version version;

	IProvidedCapability[] providedCapabilities = NO_PROVIDES;
	private IRequiredCapability[] requires = NO_REQUIRES;

	private boolean singleton;
	private boolean installed;

	private long sumProperty;

	public InstallableUnit() {
		super();
	}

	public int compareTo(Object toCompareTo) {
		if (!(toCompareTo instanceof InstallableUnit)) {
			return -1;
		}
		InstallableUnit other = (InstallableUnit) toCompareTo;
		if (getId().compareTo(other.getId()) == 0)
			return (getVersion().compareTo(other.getVersion()));
		return getId().compareTo(other.getId());
	}

	public String getId() {
		return id;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof InstallableUnit))
			return false;
		final InstallableUnit other = (InstallableUnit) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		if (getVersion() == null) {
			if (other.getVersion() != null)
				return false;
		} else if (!getVersion().equals(other.getVersion()))
			return false;
		return true;
	}

	public IProvidedCapability[] getProvidedCapabilities() {
		return providedCapabilities;
	}

	public IRequiredCapability[] getRequiredCapabilities() {
		return requires;

	}

	public Version getVersion() {
		return version;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
		return result;
	}

	public boolean isResolved() {
		return false;
	}

	public boolean isSingleton() {
		return singleton;
	}

	public void setCapabilities(IProvidedCapability[] newCapabilities) {
		if (newCapabilities == null || newCapabilities.length == 0)
			providedCapabilities = NO_PROVIDES;
		else
			providedCapabilities = newCapabilities;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRequiredCapabilities(IRequiredCapability[] capabilities) {
		if (capabilities.length == 0) {
			this.requires = NO_REQUIRES;
		} else {
			//copy array for safety
			this.requires = (IRequiredCapability[]) capabilities.clone();
		}
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public void setVersion(Version newVersion) {
		this.version = (newVersion != null ? newVersion : Version.emptyVersion);
	}

	public String toString() {
		return id + ' ' + getVersion();
	}

	public void setInstalled(boolean isInstalled) {
		installed = isInstalled;
	}

	public boolean isInstalled() {
		return installed;
	}

	public long getSumProperty() {
		return sumProperty;
	}

	public void setSumProperty(long sumProperty) {
		this.sumProperty = sumProperty;
	}
}
