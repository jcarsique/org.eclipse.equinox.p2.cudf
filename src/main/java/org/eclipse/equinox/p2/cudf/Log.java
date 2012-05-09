package org.eclipse.equinox.p2.cudf;

import org.apache.commons.logging.LogFactory;

public class Log {
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(Log.class);

    static boolean verbose = true;

    public static void println(String s) {
        if (verbose)
            log.info("# " + s);
    }

    public static void printlnNoPrefix(String s) {
        if (verbose)
            log.info(s);
    }
}
