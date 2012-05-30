package org.eclipse.equinox.p2.cudf.tests;

import java.util.*;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.OptimizationFunction.Criteria;
import org.eclipse.equinox.p2.cudf.solver.*;

public class TestNuxeoObjectives extends TestCase {
    private static final Log log = LogFactory.getLog(TestNuxeoObjectives.class);

    private ProfileChangeRequest pcr = null;

    private Collection<InstallableUnit> solution = null;

    private SimplePlanner planner = null;

    /**
     * A 1 (installed) & 2
     * B 1 & 2 depends A conflicts C
     * C 1 (installed) & 2 depends A conflicts B
     * D 1 depends B 1, D 2 depends B 2
     * E 1 depends B 1, E 2 depends B 2
     * request install D 1, install E
     */
    protected void initPCR() throws Exception {
        pcr = new Parser().parse(this.getClass().getClassLoader().getResource(
                "testData/nuxeo.cudf").openStream());
    }

    public void testParanoid() throws Exception {
        if (false)
            return; // not interesting solution
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution(SolverConfiguration.OBJ_PARANOID, false, false);
        solution = getBestSolution();
        /*
         * [A 1, B 1, D 1, E 1]
         * {CHANGED=[B, C, D, E], VERSION_CHANGED=[], NOTUPTODATE=[], NEW=[],
         * RECOMMENDED=[], REMOVED=[C]}
         */

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution(SolverConfiguration.OBJ_PARANOID, true, true);
        solution = getBestSolution();
        /*
         * [A 1, B 1, D 1, E 1]
         * {CHANGED=[B, C, D, E], VERSION_CHANGED=[], NOTUPTODATE=[], NEW=[],
         * RECOMMENDED=[], REMOVED=[C]}
         */
    }

    public void testTrendy() throws Exception {
        if (false)
            return; // not interesting solution
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution(SolverConfiguration.OBJ_TRENDY, false, false);
        solution = getBestSolution();
        /*
         * [A 2, B 1, D 1, E 1]
         * {CHANGED=[], VERSION_CHANGED=[], NOTUPTODATE=[B, D, E], NEW=[B, D,
         * E], RECOMMENDED=[], REMOVED=[C]}
         */

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution(SolverConfiguration.OBJ_TRENDY, true, true);
        solution = getBestSolution();
        /*
         * [A 2, B 1, D 1, E 1]
         * {CHANGED=[A, B, C, D, E], VERSION_CHANGED=[A], NOTUPTODATE=[B, D, E],
         * NEW=[B, D, E], RECOMMENDED=[], REMOVED=[C]}
         */
    }

    public void testAllCriteria() throws Exception {
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        solution = getSolution(SolverConfiguration.OBJ_ALL_CRITERIA, false,
                false);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [A 1, B 1, D 1, E 1]
         * {CHANGED=[A, B, C, D, E], VERSION_CHANGED=[A], NOTUPTODATE=[B, D, E],
         * NEW=[B, D, E], RECOMMENDED=[], REMOVED=[C]}
         */

        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution(SolverConfiguration.OBJ_ALL_CRITERIA, true, true);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [A 1, B 1, D 1, E 1]
         * {CHANGED=[A, B, C, D, E], VERSION_CHANGED=[A], NOTUPTODATE=[B, D, E],
         * NEW=[B, D, E], RECOMMENDED=[], REMOVED=[C]}
         */

        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()
    }

    public void testCustom() throws Exception {
        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (false, false)");
        // -removed,-notuptodate,-unsat_recommends,-new,-changed,-versionchanged
        solution = getSolution(
                "-removed,-new,-versionchanged,-notuptodate,-unsat_recommends,-changed",
                false, false);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [A 1, B 1, D 1, E 1]
         * {CHANGED=[B, C, D, E], VERSION_CHANGED=[], NOTUPTODATE=[A, B, D, E],
         * NEW=[B, D, E], RECOMMENDED=[], REMOVED=[C]}
         */

        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        log.info("\n" + new Object() {
        }.getClass().getEnclosingMethod().getName() + " (true, true)");
        solution = getSolution(
                "-removed,-new,-versionchanged,-notuptodate,-unsat_recommends,-changed",
                true, true);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [A 1, B 1, D 1, E 1]
         * {CHANGED=[B, C, D, E], VERSION_CHANGED=[], NOTUPTODATE=[A, B, D, E],
         * NEW=[B, D, E], RECOMMENDED=[], REMOVED=[C]}
         */

        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()
    }

    @SuppressWarnings("unchecked")
    protected Collection<InstallableUnit> getSolution(String objectives,
            boolean verbose, boolean explain) throws Exception {
        planner = new SimplePlanner();
        SolverConfiguration configuration = new SolverConfiguration(objectives,
                null, verbose, explain);
        initPCR();
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
