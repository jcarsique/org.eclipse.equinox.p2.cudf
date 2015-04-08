org.eclipse.equinox.p2.cudf
===========================

A p2 [CUDF](http://www.mancoosi.org/cudf/primer/) resolver forked from [an org.eclipse.equinox.p2.cudf 2010 demo hosted at Equinox incubator (under the directory `p2/demos/misc-conf-2010/`)](http://git.eclipse.org/c/equinox/rt.equinox.incubator.git).
It is used at low-level by [the Nuxeo Connect Client library](https://github.com/nuxeo/nuxeo-connect/) to manage [the "Marketplace Package" system](http://marketplace.nuxeo.com/) of [the Nuxeo ECM Platform](http://github.com/nuxeo/nuxeo/).

See [p2 CUDF Resolver wiki](http://wiki.eclipse.org/Equinox/p2/CUDFResolver).

## Objective functions

paranoid: The paranoid objective function will focus on returning a solution with the least change possible from the original solution.

trendy: The trendy objective function will focus on installing the most up to date version for each package.

It is possible to use a custom optimization criteria using those criterion:

    new: number of newly installed packages
    changed: number of packages that changed (installed, removed, or version change)
    notuptodate: number of installed packages for which there exists a newer version not installed
    unmet_recommends: number of packages recommended but not installed.
    removed: number of packages that were installed and are no longer installed.
    sum(property): sum up the values of attribute "property". Obviously, those values are expected to be a number.

Each criterion can be either maximized by prefixing it by + or minimized by prefixing it by -.

Paranoid is equivalent to -removed,-changed and trendy is equivalent to -removed,-notuptodate,-unmet_recommends,-new.

See [MISC optimization criteria](http://www.mancoosi.org/misc-live/20101126/criteria/) for more details.

# Building

    mvn clean install

## QA results

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=org.eclipse.equinox.p2.cudf-master)](https://qa.nuxeo.org/jenkins/job/org.eclipse.equinox.p2.cudf-master/)

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at www.nuxeo.com.
