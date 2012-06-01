/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial implementation and ideas
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.*;

public class Main {
    public static final String PLUGIN_ID = "org.eclipse.equinox.p2.cudf"; //$NON-NLS-1$

    private static final String VERBOSE = "-verbose";

    private static final String OBJECTIVE = "-obj";

    private static final String TIMEOUT = "-timeout";

    private static final String SORT = "-sort";

    private static final String EXPLAIN = "-explain";

    private static final String ENCODING = "-encoding";

    protected static transient Thread shutdownHook = new Thread() {
        public void run() {
            if (planner != null) {
                if (options.encoding) {
                    out.println(planner.getSolver().toString());
                    PrintWriter outMapping = null;
                    try {
                        String mappingFilename;
                        if (options.output == null) {
                            mappingFilename = "stdout.mapping";
                        } else {
                            mappingFilename = options.output + ".mapping";
                        }
                        outMapping = new PrintWriter(new FileWriter(
                                mappingFilename));
                        Map<Integer, Object> mapping = planner.getMappingToDomain();
                        Set<?> entries = mapping.entrySet();
                        for (Iterator<?> it = entries.iterator(); it.hasNext();) {
                            @SuppressWarnings("rawtypes")
                            Map.Entry entry = (Entry) it.next();
                            outMapping.println(entry.getKey() + "="
                                    + entry.getValue());
                        }
                    } catch (IOException e) {
                        System.out.println("# cannot write mapping: "
                                + e.getMessage());
                    } finally {
                        if (outMapping != null) {
                            outMapping.close();
                        }
                    }
                } else {
                    planner.stopSolver();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // wait for the solver to stop properly.
                    }
                    long end = System.currentTimeMillis();
                    Log.println(("Solving done (" + (end - begin) / 1000.0 + "s)."));
                    Collection<InstallableUnit> col = planner.getBestSolutionFoundSoFar();
                    if (col == null) {
                        printFail("Cannot find a solution");
                        if (options.explain) {
                            out.println(planner.getExplanation());
                        }
                    } else if (col.isEmpty()) {
                        System.out.println("# There is nothing to install ????");
                        out.println("# There is nothing to install ....");
                    } else {
                        if (planner.isSolutionOptimal()) {
                            System.out.println("# The solution found IS optimal");
                        } else {
                            System.out.println("# WARNING: The solution found MIGHT NOT BE optimal");
                        }
                        printSolution(col, options);
                    }
                    if (options.output != null)
                        out.close();
                }
            }
        }
    };

    static {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private static final void usage() {
        System.out.println("Usage: p2cudf [flags] inputFile [outputFile]");
        System.out.println("-obj paranoid|trendy|<user defined>   The objective function to be used to resolve the problem.");
        System.out.println("                                      Users can define their own: -new,-changed,-notuptodate,-unsat_recommends,-removed,-sum(installedsize)");
        System.out.println("-timeout <number>(c|s)                The time out after which the solver will stop. e.g. 10s stops after 10 seconds, 10c stops after 10 conflicts. Default is set to 200c for p2 and 2000c for other objective functions.");
        System.out.println("-sort                                 Sort the output.");
        System.out.println("-explain                              Provides one reason of the inability to fulfill the request");
        System.out.println("-verbose                              Display details on the platform, internal SAT solver and steps reached");
        // System.out.println("-encoding                         Output the original cudf request into an OPB problem");
    }

    static PrintStream out = System.out;

    static SimplePlanner planner;

    static Options options;

    static long begin;

    public static Options processArguments(String[] args) {
        Options result = new Options();
        if (args == null)
            return result;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase(VERBOSE)) {
                result.verbose = true;
                continue;
            }

            if (args[i].equalsIgnoreCase(ENCODING)) {
                throw new IllegalArgumentException(
                        "Encoding not available for lexico solving");
                // result.encoding = true;
                // continue;
            }
            if (args[i].equalsIgnoreCase(EXPLAIN)) {
                result.explain = true;
                continue;
            }

            if (args[i].equalsIgnoreCase(OBJECTIVE)) {
                // if (args[i + 1].startsWith("-")) {
                // printFail("-obj should be followed by the name of the objective function.");
                // System.exit(1);
                // }
                result.objective = args[++i];
                if ("paranoid".equalsIgnoreCase(result.objective)) {
                    result.objective = Options.PARANOID;
                } else if ("trendy".equalsIgnoreCase(result.objective)) {
                    result.objective = Options.TRENDY;
                }
                continue;
            }

            if (args[i].equalsIgnoreCase(TIMEOUT)) {
                if (args[i + 1].startsWith("-")) {
                    printFail("-"
                            + TIMEOUT
                            + " should be followed by a time in seconds or a number of conflicts.");
                    System.exit(1);
                }
                result.timeout = args[++i];
                continue;
            }

            if (args[i].equalsIgnoreCase(SORT)) {
                result.sort = true;
                continue;
            }
            if (result.input == null)
                result.input = new File(args[i]);
            else
                result.output = new File(args[i]);
        }
        return result;
    }

    private static boolean validateOptions(Options theOptions) {
        boolean error = false;
        // if (!"paranoid".equalsIgnoreCase(options.objective) &&
        // !"trendy".equalsIgnoreCase(options.objective) &&
        // !"p2".equalsIgnoreCase(options.objective)) {
        // printFail("Wrong Optimization criteria: " + options.objective);
        // error = true;
        // }
        if (theOptions.input == null || !theOptions.input.exists()) {
            printFail("Missing input file.");
            error = true;
        }
        if (theOptions.timeout != null && !theOptions.timeout.equals("default")
                && !theOptions.timeout.endsWith("c")
                && !theOptions.timeout.endsWith("s")) {
            printFail("Timeout should be either <number>s (100s) or <number>c (100c)");
            error = true;
        }
        return error;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }
        options = processArguments(args);
        if (validateOptions(options)) {
            System.exit(1);
        }
        Log.verbose = options.verbose;
        logOptions(options);
        logVmDetails();

        if (options.output != null) {
            try {
                out = new PrintStream(new FileOutputStream(options.output));
            } catch (FileNotFoundException e) {
                printFail("Output file does not exist.");
                System.exit(1);
            }
        }
        try {
            invokeSolver(parseCUDF(options.input), new SolverConfiguration(
                    options.objective, options.timeout, options.verbose,
                    options.explain, options.encoding));
        } catch (Exception ex) {
            printFail(ex.getMessage());
        }
        System.exit(0);
    }

    private static void logOptions(Options theOptions) {
        if (!theOptions.verbose)
            return;
        Log.println("Solver launched on " + new Date());
        Log.println("Using input file " + theOptions.input.getAbsolutePath());
        Log.println("Using output file "
                + (theOptions.output == null ? "STDOUT"
                        : theOptions.output.getAbsolutePath()));
        Log.println("Objective function " + theOptions.objective);
        Log.println("Timeout " + theOptions.timeout);
    }

    private static void logVmDetails() {
        Properties prop = System.getProperties();
        String[] infoskeys = {
                "java.runtime.name", "java.vm.name", "java.vm.version", "java.vm.vendor", "sun.arch.data.model", "java.version", "os.name", "os.version", "os.arch" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
        for (int i = 0; i < infoskeys.length; i++) {
            String key = infoskeys[i];
            Log.println((key + ((key.length() < 14) ? "\t\t" : "\t") + prop.getProperty(key))); //$NON-NLS-1$
        }
        Runtime runtime = Runtime.getRuntime();
        Log.println(("Free memory \t\t" + runtime.freeMemory())); //$NON-NLS-1$
        Log.println(("Max memory \t\t" + runtime.maxMemory())); //$NON-NLS-1$
        Log.println(("Total memory \t\t" + runtime.totalMemory())); //$NON-NLS-1$
        Log.println(("Number of processors \t" + runtime.availableProcessors())); //$NON-NLS-1$
    }

    static void printFail(String message) {
        out.println("FAIL");
        out.println(message);
    }

    private static Object invokeSolver(ProfileChangeRequest request,
            SolverConfiguration configuration) {
        Log.println("Solving ...");
        begin = System.currentTimeMillis();
        planner = new SimplePlanner();
        Object result = planner.getSolutionFor(request, configuration);
        return result;
    }

    private static ProfileChangeRequest parseCUDF(File file) {
        Log.println("Parsing ...");
        long myBegin = System.currentTimeMillis();
        String sumpProperty = extractSumProperty(options.objective);
        ProfileChangeRequest result = new Parser().parse(file,
                options.objective.contains("recommend"), sumpProperty);
        long myEnd = System.currentTimeMillis();
        Log.println(("Parsing done (" + (myEnd - myBegin) / 1000.0 + "s)."));
        return result;
    }

    private static String extractSumProperty(String objectiveFunction) {
        String[] criteria = objectiveFunction.split(",");
        for (String criterion : criteria) {
            if (criterion.contains("sum")) {
                return Options.extractSumProperty(criterion);
            }
        }
        return null;
    }

    static public void printSolution(Collection<InstallableUnit> state,
            Options theOptions) {
        if (theOptions.sort) {
            List<InstallableUnit> tmp = new ArrayList<InstallableUnit>(state);
            Collections.sort(tmp);
            state = tmp;
        }
        Log.println(("Solution contains:" + state.size()));
        for (Iterator<InstallableUnit> iterator = state.iterator(); iterator.hasNext();) {
            InstallableUnit iu = iterator.next();
            out.println("package: " + iu.getId());
            out.println("version: " + iu.getVersion().getMajor());
            out.println("installed: " + iu.isInstalled());
            out.println();
        }
    }

}
