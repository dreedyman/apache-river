# Apache River
This is a clone of Apache River subversion branch of 
http://svn.apache.org/repos/asf/river/jtsk/modules, created as a Git repository, and built with Gradle.

This is by no means complete, but used as a means to demonstrate how a modular version of Apache River can be built with Gradle.

* The `groovy-config` module has been enabled.
* All OSGi configurations need work.
* Need to work on JAR manifests

To build this project, run:

`./gradlew build`

To create a distribution, run:

`./gradlew distribution`

The distribution is built in the `dist/build` directory