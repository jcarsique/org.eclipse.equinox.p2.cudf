package org.eclipse.equinox.p2.cudf.tests;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.*;

public class TestPascalExample extends TestCase {
	private ProfileChangeRequest pcr = null;

	protected void setUp() throws Exception {
		pcr = new Parser().parse(this.getClass().getClassLoader().getResource("testData/pascal.cudf").openStream());
	}

	public void testParanoid() {
		Object result = new SimplePlanner().getSolutionFor(pcr, new SolverConfiguration("paranoid", "1000c", true, false));
		if (result instanceof Collection) {
			Collection col = (Collection) result;
			assertEquals(col.toString(), 1, col.size());
			assertEquals(col.toString(), 1, getIU(col, "A").getVersion().getMajor());
		} else {
			fail("No result found!");
		}
	}

	public void testP2() {
		Object result = new SimplePlanner().getSolutionFor(pcr, new SolverConfiguration("p2", "1000c", true, false));
		if (result instanceof Collection) {
			Collection col = (Collection) result;
			assertEquals(col.toString(), 1, col.size());
			assertEquals(col.toString(), 1, getIU(col, "A").getVersion().getMajor());
		} else {
			fail("No result found!");
		}
	}

	//DISABLED
	//	public void testTrendy() {
	//		Object result = new SimplePlanner().getSolutionFor(pcr, new SolverConfiguration("trendy", "1000c", true, false));
	//		if (result instanceof Collection) {
	//			Collection col = (Collection) result;
	//			assertEquals(col.toString(), 1, col.size());
	//			assertEquals(col.toString(), 3, getIU(col, "A").getVersion().getMajor());
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