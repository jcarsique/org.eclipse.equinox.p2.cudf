package org.eclipse.equinox.p2.cudf;

public class Log {
	static boolean verbose = true;

	public static void println(String s) {
		if (verbose)
			System.out.println("# " + s);
	}

	public static void printlnNoPrefix(String s) {
		if (verbose)
			System.out.println(s);
	}
}
