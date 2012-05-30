package org.eclipse.equinox.p2.cudf.tests;

import java.util.*;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.OptimizationFunction.Criteria;
import org.eclipse.equinox.p2.cudf.solver.*;

public class TestNuxeoExample extends TestCase {
    private static final Log log = LogFactory.getLog(TestNuxeoExample.class);

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
        log.info("PARANOID (false, false)");
        solution = getSolution(SolverConfiguration.OBJ_PARANOID, false, false);
        solution = getBestSolution();
        /*
         * [B 1, A 1, D 1, E 1]
         * {NEW=[], CHANGED=[B, C, D, E], RECOMMENDED=[], REMOVED=[C],
         * VERSION_CHANGED=[], NOTUPTODATE=[]}
         */

        log.info("PARANOID (true, true)");
        solution = getSolution(SolverConfiguration.OBJ_PARANOID, true, true);
        solution = getBestSolution();
        /*
         * [B 1, A 1, B 2, D 1, E 2]
         * {NOTUPTODATE=[], RECOMMENDED=[], NEW=[], REMOVED=[C],
         * VERSION_CHANGED=[],
         * CHANGED=[B, C, D, E]}
         */
    }

    public void testTrendy() throws Exception {
        if (false)
            return; // not interesting solution
        log.info("TRENDY (false, false)");
        solution = getSolution(SolverConfiguration.OBJ_TRENDY, false, false);
        solution = getBestSolution();
        /*
         * [B 1, A 1, A 2, B 2, D 1, D 2, E 2]
         * {NEW=[B, D, E], CHANGED=[], RECOMMENDED=[], REMOVED=[C],
         * VERSION_CHANGED=[], NOTUPTODATE=[]}
         */

        log.info("TRENDY (true, true)");
        solution = getSolution(SolverConfiguration.OBJ_TRENDY, true, true);
        solution = getBestSolution();
        /*
         * [B 1, A 1, A 2, B 2, D 1, D 2, E 2]
         * {NOTUPTODATE=[], RECOMMENDED=[], NEW=[B, D, E], REMOVED=[C],
         * VERSION_CHANGED=[], CHANGED=[]}
         * Wrong solution: D1 was asked for install, not D2
         */
    }

    public void testAllCriteria() throws Exception {
        log.info("ALL (false, false)");
        solution = getSolution(SolverConfiguration.OBJ_ALL_CRITERIA, false,
                false);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [B 1, A 1, A 2, B 2, D 1, D 2, E 2]
         * {NEW=[B, D, E], CHANGED=[A, B, C, D, E], RECOMMENDED=[], REMOVED=[C],
         * VERSION_CHANGED=[A], NOTUPTODATE=[]}
         */

        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        log.info("ALL (true, true)");
        solution = getSolution(SolverConfiguration.OBJ_ALL_CRITERIA, true, true);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [B 1, A 1, B 2, D 1, D 2, E 1]
         * {NOTUPTODATE=[], RECOMMENDED=[], NEW=[B, D, E], REMOVED=[C],
         * VERSION_CHANGED=[A], CHANGED=[A, B, C, D, E]}
         * Wrong information: A version didn't change
         * Wrong solution: D1 was asked but got D1 or D2 and only E1
         */

        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()
    }

    public void testCustom() throws Exception {
        log.info("CUSTOM (false, false)");
        solution = getSolution("-removed,-new,-changed", false, false);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [B 1, A 1, D 1, E 1]
         * {NEW=[B, D, E], CHANGED=[B, C, D, E], RECOMMENDED=[], REMOVED=[C],
         * VERSION_CHANGED=[], NOTUPTODATE=[]}
         */

        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        log.info("CUSTOM (true, true)");
        solution = getSolution("-removed,-new,-changed", true, true);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        /*
         * [B 1, A 1, B 2, D 1, E 2]
         * {NOTUPTODATE=[], RECOMMENDED=[], NEW=[B, D, E], REMOVED=[C],
         * VERSION_CHANGED=[], CHANGED=[B, C, D, E]}
         * Interesting solution. What means B1, A1, B2 in the solution?
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
