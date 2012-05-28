/*******************************************************************************
 * Copyright (c) 2007-2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.solver;

import java.util.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.cudf.Log;
import org.eclipse.equinox.p2.cudf.metadata.*;
import org.eclipse.equinox.p2.cudf.query.QueryableArray;
import org.eclipse.equinox.p2.cudf.solver.OptimizationFunction.Criteria;
import org.sat4j.pb.IPBSolver;

public class SimplePlanner {
    private final static boolean PURGE = true;

    private Projector projector;

    public Object getSolutionFor(ProfileChangeRequest profileChangeRequest,
            SolverConfiguration configuration) {
        QueryableArray profile = profileChangeRequest.getInitialState();

        InstallableUnit updatedPlan = updatePlannerInfo(profileChangeRequest);

        Slicer slice = new Slicer(profile);
        profile = slice.slice(updatedPlan,
                configuration.objective.equals("p2") ? null
                        : profileChangeRequest.getExtraRequirements());
        if (PURGE) {
            Log.println("Number of  packages after slice: " + profile.getSize());
            if (profileChangeRequest.getInitialState().getSize() != 0)
                Log.println("Slice efficiency: "
                        + (100 - ((profile.getSize() - 1) * 100)
                                / profileChangeRequest.getInitialState().getSize())
                        + "%");
            profileChangeRequest.purge();

        }
        projector = new Projector(profile);
        projector.encode(updatedPlan, configuration);
        IStatus s = projector.invokeSolver();
        if (s.getSeverity() == IStatus.ERROR) {
            return s;
        }
        return projector.extractSolution();
    }

    private InstallableUnit updatePlannerInfo(
            ProfileChangeRequest profileChangeRequest) {
        return createIURepresentingTheProfile(profileChangeRequest.getAllRequests());
    }

    private InstallableUnit createIURepresentingTheProfile(
            List<IRequiredCapability> allRequirements) {
        InstallableUnit iud = new InstallableUnit();
        String time = Long.toString(System.currentTimeMillis());
        iud.setId(time);
        iud.setVersion(new Version(0, 0, 0, time));
        iud.setRequiredCapabilities(allRequirements.toArray(new IRequiredCapability[allRequirements.size()]));
        Log.println("Request size: " + iud.getRequiredCapabilities().length);
        return iud;
    }

    public void stopSolver() {
        if (projector != null) {
            projector.stopSolver();
        }
    }

    public Collection<InstallableUnit> getBestSolutionFoundSoFar() {
        return projector.getBestSolutionFoundSoFar();
    }

    public Set<?> getExplanation() {
        return projector.getExplanation();
    }

    public IPBSolver getSolver() {
        return projector.dependencyHelper.getSolver();
    }

    public Map<Integer, Object> getMappingToDomain() {
        return projector.dependencyHelper.getMappingToDomain();
    }

    public boolean isSolutionOptimal() {
        return projector.dependencyHelper.isOptimal();
    }

    /**
     * @since 1.14
     * @return a map of IU lists in the solution, per {@link Criteria}
     */
    public Map<Criteria, List<String>> getSolutionDetails() {
        return projector.getSolutionDetails();
    }

}
