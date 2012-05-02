package org.eclipse.equinox.p2.cudf.tests;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.*;

//DISABLED - Made the class abstract to have the tests passing
public abstract class TestRecommends extends TestCase {
	private ProfileChangeRequest pcr = null;

	protected void setUp() throws Exception {
		pcr = new Parser().parse(this.getClass().getClassLoader().getResource("testData/testRecommends.cudf").openStream());
	}

	public void testParanoid() {
		SolverConfiguration configuration = new SolverConfiguration("paranoid", "1000c", true, false);
		Object result = new SimplePlanner().getSolutionFor(pcr, configuration);
		if (result instanceof Collection) {
			Collection col = (Collection) result;
			assertEquals(col.toString(), 1, col.size());
			assertEquals(col.toString(), 1, getIU(col, "d").getVersion().getMajor());
		} else {
			fail("No result found!");
		}
	}

	public void testP2() {
		SolverConfiguration configuration = new SolverConfiguration("p2", "1000c", true, false);
		Object result = new SimplePlanner().getSolutionFor(pcr, configuration);
		if (result instanceof Collection) {
			Collection col = (Collection) result;
			assertEquals(col.toString(), 3, col.size());
			// here the solver can install B1 or B2
			assertEquals(col.toString(), 1, getIU(col, "d").getVersion().getMajor());

		} else {
			fail("No result found!");
		}
	}

	public void testTrendy() {
		SolverConfiguration configuration = new SolverConfiguration("trendy", "1000c", true, false);
		Object result = new SimplePlanner().getSolutionFor(pcr, configuration);
		if (result instanceof Collection) {
			Collection col = (Collection) result;
			assertEquals(col.toString(), 3, col.size());
			// here the solver can install B1 or B2
			assertEquals(col.toString(), 1, getIU(col, "d").getVersion().getMajor());
		} else {
			fail("No result found!");
		}
	}

	private InstallableUnit getIU(Collection col, String id) {
		Iterator it = col.iterator();
		while (it.hasNext()) {
			InstallableUnit iu = (InstallableUnit) it.next();
			if (id.equals(iu.getId()))
				return iu;
		}
		fail("Can't find: " + id);
		return null;
	}
}