Foursquare Grow Website
=========================

This is the source for the [Foursquare GROW](http://foursquaregrow.com) Website.

Requirements
--------------

* JDK 1.8
* Maven

Usage
-------

1. Copy devfiles/grow-server.properties.default to devfiles/grow-server.properties and insert your
   AWS and F1 or CCB credentials.
2. Run `maven compile` to compile.
3. Run `maven exec:exec` to start the website on http://localhost:8085
   The website defaults to running in dev mode which will only modify the dev Dynamo tables.
   You *must recompile* for changes to take effect.
4. Run `maven package` to produce a war file to deploy.

DbTool Usage
----------------

DbTool is a utility to manage the DynamoDB tables used by GROW. You can run it from your IDE or from
the command line using Maven.

Run DbTool without arguments to see all options:

    mvn exec:java -Dexec.mainClass="com.p4square.grow.backend.dynamo.DbTool" \
        -Dexec.arguments=""

To create and populate the DynamoDB tables, run

    mvn exec:java -Dexec.mainClass="com.p4square.grow.backend.dynamo.DbTool" \
        -Dexec.arguments="--config,devfiles/gw-server.properties,--dev,--bootstrap,devfiles"
