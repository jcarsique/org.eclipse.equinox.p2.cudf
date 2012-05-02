package org.eclipse.equinox.p2.cudf.solver;

public class SolverConfiguration {
    public static final String OBJ_PARANOID = "paranoid";

    public static final String OBJ_TRENDY = "trendy";

    public static final String OBJ_P2 = "p2";

    public boolean verbose = false;

    public String objective = OBJ_P2;

    public String timeout = "default";

    public boolean explain = false;

    public boolean encoding = false;

    public SolverConfiguration(String objective, String timeout,
            boolean verbose, boolean explain, boolean encoding) {
        if (objective != null)
            this.objective = objective;
        if (timeout != null)
            this.timeout = timeout;
        this.verbose = verbose;
        this.explain = explain;
        this.encoding = encoding;
    }

    public SolverConfiguration(String objective, String timeout,
            boolean verbose, boolean explain) {
        this(objective, timeout, verbose, explain, false);
    }
}
