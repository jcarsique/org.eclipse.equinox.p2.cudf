/*******************************************************************************
 * Copyright (c) 2009 Daniel Le Berre and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Daniel Le Berre - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.solver;

import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.Projector.AbstractVariable;

class Pair {
    public final InstallableUnit left;

    public final AbstractVariable right;

    Pair(InstallableUnit left, AbstractVariable right) {
        this.left = left;
        this.right = right;
    }

    public String toString() {
        return "(" + left + "," + right + ")";
    }
}