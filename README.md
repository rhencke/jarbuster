This is a very bare-bones program for suggesting how to take a large, monolithic JAR and break it into proper modules.
(Proper, in this sense, is defined as having no circular dependencies between modules, as this is what most build
systems require)

It was born out of frustration when dealing with a massive, monolithic Java codebase full of spaghetti dependencies,
and lacking the tools to break it apart.

Compile with `gradlew installDist`

Run with `build/install/jarbuster/bin/jarbuster`