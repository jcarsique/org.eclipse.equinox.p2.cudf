/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.cudf.query;


import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An IQueryable contains objects, and is able to perform queries on those objects.
 * <p>
 * This interface may be implemented by clients.
 */
public interface IQueryable {
	/**
	 * Performs a query, passing any objects that satisfy the
	 * query to the provided collector.
	 * <p>
	 * This method is long-running; progress and cancellation are provided
	 * by the given progress monitor. 
	 * </p>
	 * 
	 * @param query The query to perform
	 * @param collector Collects the results of the query
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @return The collector argument
	 */
	public Collector query(Query query, Collector collector, IProgressMonitor monitor);
}
