REQUEST=${1:-/tmp/request.cudf}
echo REQUEST: $REQUEST
CRITERIA=${2:-"-removed,-notuptodate,-unsat_recommends,-new,-changed,-versionchanged"}
echo CRITERIA: $CRITERIA
echo
java -jar `ls target/org.eclipse.equinox.p2.cudf*jar-with-dependencies.jar` -obj $CRITERIA $REQUEST

#echo WITH VERBOSE:
#java -jar `ls target/org.eclipse.equinox.p2.cudf*jar-with-dependencies.jar` -verbose -obj $CRITERIA $REQUEST

# BUG: verbose+explain changes the results!
#echo WITH VERBOSE+EXPLAIN:
#java -jar `ls target/org.eclipse.equinox.p2.cudf*jar-with-dependencies.jar` -verbose -explain -obj $CRITERIA $REQUEST
