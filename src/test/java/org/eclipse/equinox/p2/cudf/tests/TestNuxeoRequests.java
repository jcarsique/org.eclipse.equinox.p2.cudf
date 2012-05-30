package org.eclipse.equinox.p2.cudf.tests;

import java.util.*;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.OptimizationFunction.Criteria;
import org.eclipse.equinox.p2.cudf.solver.*;

public class TestNuxeoRequests extends TestCase {
    private static final Log log = LogFactory.getLog(TestNuxeoRequests.class);

    private ProfileChangeRequest pcr = null;

    private Collection<InstallableUnit> solution = null;

    private SimplePlanner planner = null;

    public void testInstallSpecificVersion() throws Exception {
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution("testData/nuxeo.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, false, false);
        solution = getBestSolution();
        /*
         * [A 3, B 2, D 2, E 2]
         * {REMOVED=[C], CHANGED=[A, B, C, D, E], NOTUPTODATE=[B, D, E],
         * RECOMMENDED=[], NEW=[B, D, E], VERSION_CHANGED=[A]}
         */

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution("testData/nuxeo.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, true, true);
        solution = getBestSolution();
        /*
         * [A 2, B 2, D 2, E 2]
         * {REMOVED=[C], CHANGED=[A, B, C, D, E], NOTUPTODATE=[B, D, E],
         * RECOMMENDED=[], NEW=[B, D, E], VERSION_CHANGED=[A]}
         */
    }

    public void testInstallNoVersion() throws Exception {
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution("testData/nuxeoInstall.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, false, false);
        solution = getBestSolution();
        /*
         * [A 3, B 3, D 3, E 3]
         * {REMOVED=[C], CHANGED=[A, B, C, D, E], NOTUPTODATE=[],
         * RECOMMENDED=[], NEW=[B, D, E], VERSION_CHANGED=[A]}
         */

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution("testData/nuxeoInstall.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, true, true);
        solution = getBestSolution();
        /*
         * [A 2, B 1, D 1, E 1]
         * {REMOVED=[C], CHANGED=[A, B, C, D, E], NOTUPTODATE=[],
         * RECOMMENDED=[], NEW=[B, D, E], VERSION_CHANGED=[A]}
         */
    }

    public void testRemoveSpecificVersion() throws Exception {
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution("testData/nuxeoRemoveSpecific.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, false, false);
        solution = getBestSolution();
        /*
         * [A 3, C 3]
         * {REMOVED=[], CHANGED=[A, C], NOTUPTODATE=[], RECOMMENDED=[], NEW=[],
         * VERSION_CHANGED=[A, C]}
         */

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution("testData/nuxeoRemoveSpecific.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, true, true);
        solution = getBestSolution();
        /*
         * [A 3, C 3]
         * {REMOVED=[], CHANGED=[A, C], NOTUPTODATE=[], RECOMMENDED=[], NEW=[],
         * VERSION_CHANGED=[A, C]}
         */
    }

    public void testRemove() throws Exception {
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution("testData/nuxeoRemove.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, false, false);
        solution = getBestSolution();
        /*
         * []
         * {REMOVED=[A, C], CHANGED=[A, C], NOTUPTODATE=[], RECOMMENDED=[],
         * NEW=[], VERSION_CHANGED=[]}
         */

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution("testData/nuxeoRemove.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, true, true);
        solution = getBestSolution();
        /*
         * []
         * {REMOVED=[A, C], CHANGED=[A, C], NOTUPTODATE=[], RECOMMENDED=[],
         * NEW=[], VERSION_CHANGED=[]}
         */
    }

    public void testUpgrade() throws Exception {
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution("testData/nuxeoUpgrade.cudf",
                SolverConfiguration.OBJ_ALL_CRITERIA, false, false);
        solution = getBestSolution();
        /*
         * [A 3, C 3]
         * {REMOVED=[], CHANGED=[A, C], NOTUPTODATE=[], RECOMMENDED=[], NEW=[],
         * VERSION_CHANGED=[A, C]}
         */

        // log.info(new
        // Object(){}.getClass().getEnclosingMethod().getName()+" (true, true)");
        // java.lang.UnsupportedOperationException
        // solution = getSolution("testData/nuxeoUpgrade.cudf",
        // SolverConfiguration.OBJ_ALL_CRITERIA, true, true);
        // solution = getBestSolution();
    }

    @SuppressWarnings("unchecked")
    protected Collection<InstallableUnit> getSolution(String cudf,
            String objectives, boolean verbose, boolean explain)
            throws Exception {
        planner = new SimplePlanner();
        SolverConfiguration configuration = new SolverConfiguration(objectives,
                null, verbose, explain);
        pcr = new Parser().parse(this.getClass().getClassLoader().getResource(
                cudf).openStream());
        Object result = planner.getSolutionFor(pcr, configuration);
        planner.stopSolver();
        if (result instanceof Collection<?>) {
            solution = (Collection<InstallableUnit>) result;
        } else {
            fail("No result found!");
        }
        log.info(solution);
        return solution;
    }

    protected Collection<InstallableUnit> getBestSolution() {
        solution = planner.getBestSolutionFoundSoFar();
        log.info(solution);
        Map<Criteria, List<String>> details = planner.getSolutionDetails();
        log.info(details);
        return solution;
    }

    private InstallableUnit getIU(Collection<InstallableUnit> solution,
            String id) {
        for (InstallableUnit iu : solution) {
            if (id.equals(iu.getId())) {
                return iu;
            }
        }
        return null;
    }
}
