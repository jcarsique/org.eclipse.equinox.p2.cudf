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

    private boolean verbose = true;

    private boolean explain = true;

    protected void setUp() throws Exception {
        pcr = new Parser().parse(this.getClass().getClassLoader().getResource(
                "testData/nuxeo.cudf").openStream());
    }

    public void testParanoid() {
        log.info("PARANOID");
        solution = getSolution(SolverConfiguration.OBJ_PARANOID);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()
    }

    public void testTrendy() {
        log.info("TRENDY");
        solution = getSolution(SolverConfiguration.OBJ_TRENDY);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()
    }

    public void testAllCriteria() {
        log.info("ALL");
        solution = getSolution(SolverConfiguration.OBJ_ALL_CRITERIA);
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()
    }

    public void testCustom() {
        log.info("CUSTOM");
        solution = getSolution("-removed,-new,-changed");
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()

        solution = getBestSolution();
        // TODO: complete
        // check solution.size()
        // check getIU(solution, "A").getVersion().getMajor()
    }

    @SuppressWarnings("unchecked")
    protected Collection<InstallableUnit> getSolution(String objectives) {
        planner = new SimplePlanner();
        SolverConfiguration configuration = new SolverConfiguration(objectives,
                null, verbose, explain);
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
