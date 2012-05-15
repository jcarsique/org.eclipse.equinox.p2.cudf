package org.eclipse.equinox.p2.cudf;

import java.io.File;

public class Options {
    public static final String PARANOID = "-removed,-changed";

    public static final String TRENDY = "-removed,-notuptodate,-unsat_recommends,-new";

    public static String extractSumProperty(String sumCriterion) {
        return sumCriterion.substring(5, sumCriterion.length() - 1);
    }

    boolean verbose = false;

    String objective = PARANOID;

    String timeout = "default";

    boolean explain = false;

    public File input;

    public File output;

    public boolean sort = false;

    boolean encoding = false;
}
