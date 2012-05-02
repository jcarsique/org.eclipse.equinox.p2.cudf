/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * Daniel Le Berre - Fix in the encoding and the optimization function
 * Alban Browaeys - Optimized string concatenation in bug 251357
 * Jed Anderson - switch from opb files to API calls to DependencyHelper in bug 200380
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.solver;

import java.io.PrintWriter;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.cudf.Log;
import org.eclipse.equinox.p2.cudf.Main;
import org.eclipse.equinox.p2.cudf.metadata.*;
import org.eclipse.equinox.p2.cudf.query.*;
import org.eclipse.osgi.util.NLS;
import org.sat4j.minisat.restarts.LubyRestarts;
import org.sat4j.pb.*;
import org.sat4j.pb.core.PBSolverResolution;
import org.sat4j.pb.tools.LexicoHelper;
import org.sat4j.pb.tools.WeightedObject;
import org.sat4j.specs.*;

/**
 * This class is the interface between SAT4J and the planner. It produces a
 * boolean satisfiability problem, invokes the solver, and converts the solver
 * result
 * back into information understandable by the planner.
 */
public class Projector {
    private static final boolean DEBUG = false; // SET THIS TO FALSE FOR THE
                                                // COMPETITION

    private static final boolean TIMING = false; // SET THIS TO FALSE FOR THE
                                                 // COMPETITION

    private static final boolean DEBUG_ENCODING = false; // SET THIS TO FALSE
                                                         // FOR THE COMPETITION

    private static final boolean PURGE = true;

    private QueryableArray picker;

    private Map noopVariables; // key IU, value AbstractVariable

    private List abstractVariables;

    private TwoTierMap slice; // The IUs that have been considered to be part of
                              // the problem

    LexicoHelper dependencyHelper;

    private Collection solution;

    private Collection assumptions;

    private MultiStatus result;

    private InstallableUnit entryPoint;

    private SolverConfiguration configuration;

    private OptimizationFunction optFunction;

    private List optionalityVariables;

    private List optionalityPairs;

    static class AbstractVariable {
        private String str;

        AbstractVariable() {
            // no value for str
        }

        AbstractVariable(String str) {
            this.str = str;
        }

        public String toString() {
            return "AbstractVariable: " + (str == null ? "" + hashCode() : str); //$NON-NLS-1$
        }
    }

    public Projector(QueryableArray q) {
        picker = q;
        noopVariables = new HashMap();
        slice = new TwoTierMap(q.getSize(),
                TwoTierMap.POLICY_BOTH_MAPS_PRESERVE_ORDERING);
        abstractVariables = new ArrayList();
        result = new MultiStatus(Main.PLUGIN_ID, IStatus.OK,
                Messages.Planner_Problems_resolving_plan, null);
        assumptions = new ArrayList();
        optionalityVariables = new ArrayList();
        optionalityPairs = new ArrayList();
    }

    private void purgeIU(InstallableUnit iu) {
        if (PURGE) {
            iu.setCapabilities(InstallableUnit.NO_PROVIDES);
            iu.setRequiredCapabilities(InstallableUnit.NO_REQUIRES);
        }
    }

    public void encode(InstallableUnit entryPointIU, SolverConfiguration conf) {
        this.configuration = conf;
        this.entryPoint = entryPointIU;
        solution = null;
        try {
            long start = 0;
            if (TIMING) {
                start = System.currentTimeMillis();
                Tracing.debug("Starting projection ... "); //$NON-NLS-1$
            }
            IPBSolver solver;
            if (DEBUG_ENCODING) {
                solver = new UserFriendlyPBStringSolver(); // .newOPBStringSolver();
            } else if (conf.encoding) {
                solver = SolverFactory.newOPBStringSolver();
            } else {
                PBSolverResolution mysolver = SolverFactory.newCompetPBResLongWLMixedConstraintsObjectiveExpSimp();
                mysolver.setSimplifier(mysolver.SIMPLE_SIMPLIFICATION);
                mysolver.setRestartStrategy(new LubyRestarts(512));
                // mysolver.setLearnedConstraintsDeletionStrategy(mysolver.memory_based);
                // mysolver.setSearchListener(new
                // DecisionTracing("/tmp/rand992.dat"));
                solver = mysolver; // SolverFactory.newResolutionGlucoseSimpleSimp();//
                                   // SolverFactory.newEclipseP2();
            }
            if ("default".equals(configuration.timeout)) {
                solver.setTimeout(300); // 5 minutes
            } else {
                int number = Integer.valueOf(
                        configuration.timeout.substring(0,
                                configuration.timeout.length() - 1)).intValue();
                if (configuration.timeout.endsWith("s")) {
                    solver.setTimeout(number);
                } else {
                    solver.setTimeoutOnConflicts(number);
                }
            }
            solver.setVerbose(configuration.verbose);
            solver.setLogPrefix("# ");
            Log.printlnNoPrefix(solver.toString("# "));
            dependencyHelper = new LexicoHelper(solver, conf.explain);
            if (DEBUG_ENCODING) {
                ((UserFriendlyPBStringSolver) solver).setMapping(dependencyHelper.getMappingToDomain());
            }
            Iterator iusToEncode = picker.iterator();
            List iusToOrder = new ArrayList(picker.getSize());
            while (iusToEncode.hasNext()) {
                iusToOrder.add(iusToEncode.next());
            }
            Collections.sort(iusToOrder);
            iusToEncode = iusToOrder.iterator();
            while (iusToEncode.hasNext()) {
                InstallableUnit iuToEncode = (InstallableUnit) iusToEncode.next();
                if (iuToEncode != entryPointIU) {
                    processIU(iuToEncode, false);
                }
            }
            createConstraintsForSingleton();

            createMustHave(entryPointIU);

            optFunction = getOptimizationFactory(configuration.objective);
            setObjectiveFunction(optFunction.createOptimizationFunction(entryPointIU));
            if (TIMING) {
                Tracing.debug("Objective function contains "
                        + solver.getObjectiveFunction().getVars().size()
                        + " literals");
                long stop = System.currentTimeMillis();
                Tracing.debug("Projection completed: " + (stop - start) + "ms."); //$NON-NLS-1$
            }
            if (DEBUG_ENCODING) {
                Log.println(solver.toString());
            }
        } catch (IllegalStateException e) {
            result.add(new Status(IStatus.ERROR, Main.PLUGIN_ID,
                    e.getMessage(), e));
            if (configuration.verbose) {
                Log.println("*** PBM *** " + e.getMessage());
            }
        } catch (ContradictionException e) {
            result.add(new Status(IStatus.ERROR, Main.PLUGIN_ID,
                    Messages.Planner_Unsatisfiable_problem));
            if (configuration.verbose) {
                Log.println("Unsat OPB problem ");
            }
        }
    }

    private OptimizationFunction getOptimizationFactory(String optFunctionName) {
        OptimizationFunction function = null;
        function = new UserDefinedOptimizationFunction(optFunctionName);
        Log.println("Optimization function: " + function.getName());
        function.slice = slice;
        function.noopVariables = noopVariables;
        function.picker = picker;
        function.dependencyHelper = dependencyHelper;
        function.optionalityVariables = optionalityVariables;
        function.optionalityPairs = optionalityPairs;
        return function;
    }

    private void setObjectiveFunction(List weightedObjects) {
        if (weightedObjects == null)
            return;
        if (DEBUG) {
            StringBuffer b = new StringBuffer();
            for (Iterator i = weightedObjects.iterator(); i.hasNext();) {
                WeightedObject object = (WeightedObject) i.next();
                if (b.length() > 0)
                    b.append(", "); //$NON-NLS-1$
                b.append(object.getWeight());
                b.append(' ');
                b.append(object.thing);
            }
            Tracing.debug("objective function: " + b); //$NON-NLS-1$
        }
        dependencyHelper.setObjectiveFunction((WeightedObject[]) weightedObjects.toArray(new WeightedObject[weightedObjects.size()]));
    }

    private void createMustHave(InstallableUnit iu)
            throws ContradictionException {
        processIU(iu, true);
        if (DEBUG) {
            Tracing.debug(iu + "=1"); //$NON-NLS-1$
        }
        dependencyHelper.setTrue(iu, new Explanation.IUToInstall(iu));
        // assumptions.add(iu);
    }

    private void createNegation(InstallableUnit iu, IRequiredCapability req)
            throws ContradictionException {
        if (DEBUG) {
            Tracing.debug(iu + "=0"); //$NON-NLS-1$
        }
        dependencyHelper.setFalse(iu, new Explanation.MissingIU(iu, req));
    }

    private void expandNegatedRequirement(IRequiredCapability req,
            InstallableUnit iu, boolean isRootIu) throws ContradictionException {
        IRequiredCapability negatedReq = ((NotRequirement) req).getRequirement();
        List matches = getApplicableMatches(negatedReq);
        matches.remove(iu);
        if (matches.isEmpty()) {
            return;
        }
        Explanation explanation;
        if (isRootIu) {
            InstallableUnit reqIu = (InstallableUnit) matches.iterator().next();
            explanation = new Explanation.IUToInstall(reqIu);
        } else {
            explanation = new Explanation.HardRequirement(iu, req);
        }
        createNegationImplication(iu, matches, explanation);
    }

    private void expandRequirement(IRequiredCapability req, InstallableUnit iu,
            boolean isRootIu) throws ContradictionException {
        if (req.isNegation()) {
            expandNegatedRequirement(req, iu, isRootIu);
            return;
        }
        List matches = getApplicableMatches(req);
        if (!req.isOptional()) {
            if (matches.isEmpty()) {
                missingRequirement(iu, req);
            } else {
                if (req.getArity() == 1) {
                    createAtMostOne((InstallableUnit[]) matches.toArray(new InstallableUnit[matches.size()]));
                    return;
                }
                InstallableUnit reqIu = (InstallableUnit) matches.iterator().next();
                Explanation explanation = new Explanation.IUToInstall(reqIu);
                createImplication(iu, matches, explanation);
            }
        } else {
            AbstractVariable abs = getAbstractVariable(iu.toString() + "->"
                    + req.toString());
            matches.add(abs);
            createImplication(iu, matches, Explanation.OPTIONAL_REQUIREMENT);
            optionalityVariables.add(abs);
            optionalityPairs.add(new Pair(iu, abs));
        }
    }

    private void expandRequirements(IRequiredCapability[] reqs,
            InstallableUnit iu, boolean isRootIu) throws ContradictionException {
        if (reqs.length == 0) {
            return;
        }
        for (int i = 0; i < reqs.length; i++) {
            expandRequirement(reqs[i], iu, isRootIu);
        }
    }

    public void processIU(InstallableUnit iu, boolean isRootIU)
            throws ContradictionException {
        slice.put(iu.getId(), iu.getVersion(), iu);
        expandRequirements(getRequiredCapabilities(iu), iu, isRootIU);
    }

    private IRequiredCapability[] getRequiredCapabilities(InstallableUnit iu) {
        return iu.getRequiredCapabilities();
    }

    private void missingRequirement(InstallableUnit iu, IRequiredCapability req)
            throws ContradictionException {
        result.add(new Status(IStatus.WARNING, Main.PLUGIN_ID, NLS.bind(
                Messages.Planner_Unsatisfied_dependency, iu, req)));
        createNegation(iu, req);
    }

    /**
     * @param req
     * @return a list of mandatory requirements if any, an empty list if
     *         req.isOptional().
     */
    private List getApplicableMatches(IRequiredCapability req) {
        List target = new ArrayList();
        Collector matches = picker.query(new CapabilityQuery(req),
                new Collector(), null);
        for (Iterator iterator = matches.iterator(); iterator.hasNext();) {
            InstallableUnit match = (InstallableUnit) iterator.next();
            target.add(match);
        }
        return target;
    }

    // This will create as many implication as there is element in the right
    // argument
    private void createNegationImplication(Object left, List right,
            Explanation name) throws ContradictionException {
        if (DEBUG) {
            Tracing.debug(name + ": " + left + "->" + right); //$NON-NLS-1$ //$NON-NLS-2$
        }
        for (Iterator iterator = right.iterator(); iterator.hasNext();) {
            dependencyHelper.implication(new Object[] { left }).impliesNot(
                    iterator.next()).named(name);
        }

    }

    private void createImplication(Object left, List right, Explanation name)
            throws ContradictionException {
        if (DEBUG) {
            Tracing.debug(name + ": " + left + "->" + right); //$NON-NLS-1$ //$NON-NLS-2$
        }
        dependencyHelper.implication(new Object[] { left }).implies(
                right.toArray()).named(name);
    }

    // Create constraints to deal with singleton
    // When there is a mix of singleton and non singleton, several constraints
    // are generated
    private void createConstraintsForSingleton() throws ContradictionException {
        Set s = slice.entrySet();
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            HashMap conflictingEntries = (HashMap) entry.getValue();
            if (conflictingEntries.size() < 2)
                continue;

            Collection conflictingVersions = conflictingEntries.values();
            List singletons = new ArrayList();
            List nonSingletons = new ArrayList();
            for (Iterator conflictIterator = conflictingVersions.iterator(); conflictIterator.hasNext();) {
                InstallableUnit iu = (InstallableUnit) conflictIterator.next();
                if (iu.isSingleton()) {
                    singletons.add(iu);
                } else {
                    nonSingletons.add(iu);
                }
            }
            if (singletons.isEmpty())
                continue;

            InstallableUnit[] singletonArray;
            if (nonSingletons.isEmpty()) {
                singletonArray = (InstallableUnit[]) singletons.toArray(new InstallableUnit[singletons.size()]);
                createAtMostOne(singletonArray);
            } else {
                singletonArray = (InstallableUnit[]) singletons.toArray(new InstallableUnit[singletons.size() + 1]);
                for (Iterator iterator2 = nonSingletons.iterator(); iterator2.hasNext();) {
                    singletonArray[singletonArray.length - 1] = (InstallableUnit) iterator2.next();
                    createAtMostOne(singletonArray);
                }
            }
        }
    }

    private void createAtMostOne(InstallableUnit[] ius)
            throws ContradictionException {
        if (DEBUG) {
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < ius.length; i++) {
                b.append(ius[i].toString());
            }
            Tracing.debug("At most 1 of " + b); //$NON-NLS-1$
        }
        dependencyHelper.atMost(1, ius).named(new Explanation.Singleton(ius));
    }

    private AbstractVariable getAbstractVariable(String name) {
        AbstractVariable abstractVariable = new AbstractVariable(name);
        abstractVariables.add(abstractVariable);
        return abstractVariable;
    }

    private void purge() {
        if (PURGE) {
            Iterator iusToEncode = picker.iterator();
            while (iusToEncode.hasNext()) {
                purgeIU((InstallableUnit) iusToEncode.next());
            }

            picker = null;
            noopVariables = null;
            abstractVariables = null;
            slice = null;
            // assumptions = null;
        }
    }

    private boolean isSatisfiable;

    public IStatus invokeSolver() {
        purge();
        isSatisfiable = false;
        if (result.getSeverity() == IStatus.ERROR)
            return result;
        // CNF filename is given on the command line
        long start = System.currentTimeMillis();
        if (TIMING)
            Tracing.debug("Invoking solver ..."); //$NON-NLS-1$
        try {
            Log.println("p cnf " + dependencyHelper.getSolver().nVars() + " "
                    + dependencyHelper.getSolver().nConstraints());
            if (dependencyHelper.hasASolution(assumptions)) {
                isSatisfiable = true;
                if (DEBUG) {
                    Tracing.debug("Satisfiable !"); //$NON-NLS-1$
                }
                if (TIMING)
                    Tracing.debug("Solver first solution found: " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$
                backToIU();
                long stop = System.currentTimeMillis();
                if (TIMING)
                    Tracing.debug("Solver best solution decoded: " + (stop - start) + "ms."); //$NON-NLS-1$
                if (configuration.verbose)
                    dependencyHelper.getSolver().printStat(
                            new PrintWriter(System.out, true), "# ");
            } else {
                long stop = System.currentTimeMillis();
                if (DEBUG) {
                    Tracing.debug("Unsatisfiable !"); //$NON-NLS-1$
                    Tracing.debug("Solver solution NOT found: " + (stop - start)); //$NON-NLS-1$
                }
                result.merge(new Status(IStatus.ERROR, Main.PLUGIN_ID,
                        Messages.Planner_Unsatisfiable_problem));
            }
        } catch (TimeoutException e) {
            result.merge(new Status(IStatus.ERROR, Main.PLUGIN_ID,
                    Messages.Planner_Timeout));
            if (configuration.verbose) {
                Log.println("Timeout reached");
            }
        } catch (Exception e) {
            result.merge(new Status(IStatus.ERROR, Main.PLUGIN_ID,
                    Messages.Planner_Unexpected_problem, e));
            if (configuration.verbose) {
                Log.println("*** PBM *** " + e.getMessage());
            }
        }
        return result;
    }

    private void backToIU() {
        solution = null;
        if (!isSatisfiable)
            return;
        solution = new ArrayList();
        IVec sat4jSolution = dependencyHelper.getSolution();
        if (sat4jSolution.isEmpty())
            return;
        if (configuration.verbose && optFunction != null)
            optFunction.printSolutionValue();
        for (Iterator i = sat4jSolution.iterator(); i.hasNext();) {
            Object var = i.next();
            if (var instanceof InstallableUnit) {
                InstallableUnit iu = (InstallableUnit) var;
                if (iu == entryPoint)
                    continue;
                solution.add(iu);
            }
        }
    }

    private void printSolution(Collection state) {
        ArrayList l = new ArrayList(state);
        Collections.sort(l);
        Tracing.debug("Solution:"); //$NON-NLS-1$
        Tracing.debug("Numbers of IUs selected: " + l.size()); //$NON-NLS-1$
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            Tracing.debug(iterator.next().toString());
        }
    }

    public Collection extractSolution() {
        if (DEBUG)
            printSolution(solution);
        return solution;
    }

    public Set getExplanation() {
        ExplanationJob job = new ExplanationJob(dependencyHelper);
        job.schedule();
        IProgressMonitor pm = new NullProgressMonitor();
        pm.beginTask(Messages.Planner_NoSolution, 1000);
        try {
            synchronized (job) {
                while (job.getExplanationResult() == null
                        && job.getState() != Job.NONE) {
                    if (pm.isCanceled()) {
                        job.cancel();
                        throw new OperationCanceledException();
                    }
                    pm.worked(1);
                    try {
                        job.wait(100);
                    } catch (InterruptedException e) {
                        if (DEBUG)
                            Tracing.debug("Interrupted while computing explanations"); //$NON-NLS-1$
                    }
                }
            }
        } finally {
            pm.done();
        }
        return job.getExplanationResult();
    }

    public void stopSolver() {
        dependencyHelper.stopSolver();
    }

    public Collection getBestSolutionFoundSoFar() {
        if (solution == null) {
            backToIU();
        }
        if (solution == null)
            return null;
        return extractSolution();
    }
}