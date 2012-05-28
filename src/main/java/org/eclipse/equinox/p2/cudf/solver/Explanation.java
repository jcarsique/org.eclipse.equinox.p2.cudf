/*******************************************************************************
 * Copyright (c) 2009 Daniel Le Berre and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Daniel Le Berre - initial API and implementation
 *   IBM - ongoing development
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.solver;

import java.util.Arrays;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.p2.cudf.Main;
import org.eclipse.equinox.p2.cudf.metadata.IRequiredCapability;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.osgi.util.NLS;

public abstract class Explanation implements Comparable<Explanation> {

    public static class HardRequirement extends Explanation {
        public final InstallableUnit iu;

        public final IRequiredCapability req;

        public HardRequirement(InstallableUnit iu, IRequiredCapability req) {
            this.iu = iu;
            this.req = req;
        }

        public int orderValue() {
            return 5;
        }

        public IStatus toStatus() {
            MultiStatus result = new MultiStatus(Main.PLUGIN_ID, 1,
                    Messages.Explanation_unsatisfied, null);
            result.add(new Status(IStatus.ERROR, Main.PLUGIN_ID, NLS.bind(
                    Messages.Explanation_from, getUserReadableName(iu))));
            result.add(new Status(IStatus.ERROR, Main.PLUGIN_ID, NLS.bind(
                    Messages.Explanation_to, req)));
            return result;
        }

        public String toString() {
            return NLS.bind(Messages.Explanation_hardDependency, iu, req);
        }
    }

    public static class IUInstalled extends Explanation {
        public final InstallableUnit iu;

        public IUInstalled(InstallableUnit iu) {
            this.iu = iu;
        }

        public int orderValue() {
            return 2;
        }

        public String toString() {
            return NLS.bind(Messages.Explanation_alreadyInstalled, iu);
        }

        public IStatus toStatus() {
            return new Status(IStatus.ERROR, Main.PLUGIN_ID, NLS.bind(
                    Messages.Explanation_alreadyInstalled,
                    getUserReadableName(iu)));
        }
    }

    public static class IUToInstall extends Explanation {
        public final InstallableUnit iu;

        public IUToInstall(InstallableUnit iu) {
            this.iu = iu;
        }

        public int orderValue() {
            return 1;
        }

        public String toString() {
            return NLS.bind(Messages.Explanation_toInstall, iu);
        }

        public IStatus toStatus() {
            return new Status(IStatus.ERROR, Main.PLUGIN_ID, NLS.bind(
                    Messages.Explanation_toInstall, getUserReadableName(iu)));
        }
    }

    public static class MissingIU extends Explanation {
        public final InstallableUnit iu;

        public final IRequiredCapability req;

        public MissingIU(InstallableUnit iu, IRequiredCapability req) {
            this.iu = iu;
            this.req = req;
        }

        public int orderValue() {
            return 3;
        }

        public int shortAnswer() {
            return MISSING_REQUIREMENT;
        }

        public String toString() {
            return NLS.bind(Messages.Explanation_missingRequired, iu, req);
        }

        public IStatus toStatus() {
            return new Status(IStatus.ERROR, Main.PLUGIN_ID, NLS.bind(
                    Messages.Explanation_missingRequired,
                    getUserReadableName(iu), req));
        }
    }

    public static class Singleton extends Explanation {
        public final InstallableUnit[] ius;

        public Singleton(InstallableUnit[] ius) {
            this.ius = ius;
        }

        public int orderValue() {
            return 4;
        }

        public int shortAnswer() {
            return VIOLATED_SINGLETON_CONSTRAINT;
        }

        public IStatus toStatus() {
            MultiStatus result = new MultiStatus(Main.PLUGIN_ID, 1, NLS.bind(
                    Messages.Explanation_singleton, ""), null); //$NON-NLS-1$
            for (int i = 0; i < ius.length; i++)
                result.add(new Status(IStatus.ERROR, Main.PLUGIN_ID,
                        getUserReadableName(ius[i])));
            return result;
        }

        public String toString() {
            return NLS.bind(Messages.Explanation_singleton, Arrays.asList(ius));
        }

    }

    public static final int MISSING_REQUIREMENT = 1;

    public static final Explanation OPTIONAL_REQUIREMENT = new Explanation() {

        public int orderValue() {
            return 6;
        }

        public String toString() {
            return Messages.Explanation_optionalDependency;
        }
    };

    public static final int VIOLATED_SINGLETON_CONSTRAINT = 2;

    protected Explanation() {
        super();
    }

    public int compareTo(Explanation arg0) {
        Explanation exp = arg0;
        if (this.orderValue() == exp.orderValue()) {
            return this.toString().compareTo(exp.toString());
        }
        return this.orderValue() - exp.orderValue();
    }

    protected abstract int orderValue();

    public int shortAnswer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a representation of this explanation as a status object.
     */
    public IStatus toStatus() {
        return new Status(IStatus.ERROR, Main.PLUGIN_ID, toString());
    }

    protected String getUserReadableName(InstallableUnit iu) {
        if (iu == null)
            return ""; //$NON-NLS-1$
        String result = getLocalized(iu);
        if (result == null)
            return iu.toString();
        return result + ' ' + iu.getVersion() + " (" + iu.toString() + ')'; //$NON-NLS-1$
    }

    private String getLocalized(InstallableUnit iu) {
        return iu.getId();
    }
}
