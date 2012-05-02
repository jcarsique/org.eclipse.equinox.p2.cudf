package org.eclipse.equinox.p2.cudf.tests;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.*;

public class TestJosepExample extends TestCase {
	private ProfileChangeRequest pcr = null;

	protected void setUp() throws Exception {
		pcr = new Parser().parse(this.getClass().getClassLoader().getResource("testData/josep.cudf").openStream());
	}

	public void testParanoid() {
		SolverConfiguration configuration = new SolverConfiguration("paranoid", "1000c", true, false);
		Object result = new SimplePlanner().getSolutionFor(pcr, configuration);
		if (result instanceof Collection) {
			Collection col = (Collection) result;
			assertEquals(col.toString(), 2, col.size());
			assertEquals(col.toString(), 1, getIU(col, "A").getVersion().getMajor());
			// here the solver can install B1 or B2
			assertTrue(col.toString(), 1 == getIU(col, "B").getVersion().getMajor() || 2 == getIU(col, "B").getVersion().getMajor());

		} else {
			fail("No result found!");
		}
	}

	public void testP2() {
		SolverConfiguration configuration = new SolverConfiguration("p2", "1000c", true, false);
		Object result = new SimplePlanner().getSolutionFor(pcr, configuration);
		if (result instanceof Collection) {
			Collection col = (Collection) result;
			assertEquals(col.toString(), 2, col.size());
			assertEquals(col.toString(), 1, getIU(col, "A").getVersion().getMajor());
			assertEquals(col.toString(), 2, getIU(col, "B").getVersion().getMajor());

		} else {
			fail("No result found!");
		}
	}

	//DISABLED
	//	public void testTrendy() {
	//		SolverConfiguration configuration = new SolverConfiguration("trendy", "1000c", true, false);
	//		Object result = new SimplePlanner().getSolutionFor(pcr, configuration);
	//		if (result instanceof Collection) {
	//			Collection col = (Collection) result;
	//			assertEquals(col.toString(), 2, col.size());
	//			assertEquals(col.toString(), 2, getIU(col, "A").getVersion().getMajor());
	//			assertEquals(col.toString(), 2, getIU(col, "B").getVersion().getMajor());
	//
	//		} else {
	//			fail("No result found!");
	//		}
	//	}

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