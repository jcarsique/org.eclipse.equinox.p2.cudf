package org.eclipse.equinox.p2.cudf.tests;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.*;

public class SolverComparator {
	public static void main(String[] args) throws IOException {
		File inputFile = new File("/Users/Pascal/tmp/compet/data.mancoosi.org/misc2010/results/problems/debian-dudf/58a4a468-38a5-11df-a561-00163e7a6f5e.cudf.bz2");
		File solutionFile = new File("/Users/Pascal/tmp/compet/data.mancoosi.org/misc2010/results/solutions/uns-paranoid-0.0002/58a4a468-38a5-11df-a561-00163e7a6f5e.cudf.debian-dudf.result.bz2");
		ProfileChangeRequest solution = new Parser().parse(CUDFTestHelper.getStream(solutionFile));

		ProfileChangeRequest req = new Parser().parse(CUDFTestHelper.getStream(inputFile));
		SolverConfiguration configuration = new SolverConfiguration("paranoid", "25s", true, false, false);
		Object result = new SimplePlanner().getSolutionFor(req, configuration);
		if (result instanceof Collection) {
			if (((Collection) result).containsAll(solution.getInitialState().getList()) && solution.getInitialState().getList().containsAll((Collection) result))
				System.out.println("cool");
			else {
				System.err.println("Computed solution does not match expected one");
				System.err.println("The solution to compare with contains: " + solution.getInitialState().getList().size());
				System.err.println("Your solution contains: " + ((Collection) result).size());
				if (((Collection) result).size() > solution.getInitialState().getList().size()) {
					HashSet resultAsmap = new HashSet((Collection) result);
					resultAsmap.removeAll(solution.getInitialState().getList());
					for (Iterator iterator = resultAsmap.iterator(); iterator.hasNext();) {
						InstallableUnit iu = (InstallableUnit) iterator.next();
						System.err.println("package: " + iu.getId());
						System.err.println("version: " + iu.getVersion().getMajor());
						System.err.println();
					}
				} else {
					HashSet originalSolution = new HashSet(solution.getInitialState().getList());
					originalSolution.removeAll((Collection) result);
					for (Iterator iterator = originalSolution.iterator(); iterator.hasNext();) {
						InstallableUnit iu = (InstallableUnit) iterator.next();
						System.err.println("package: " + iu.getId());
						System.err.println("version: " + iu.getVersion().getMajor());
						System.err.println();
					}
				}
			}
		} else {
			System.out.println("No solution found");
		}
	}
}
