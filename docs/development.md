# How to develop Sudachi

## Requirements

You need to install a JDK, for example from https://adoptium.net/
Both 11 and 17 will suffice.
Sudachi keeps Java 8 source compatibility at the moment, but we use JDK 11 for CI.

## Build System

Sudachi uses [Gradle](https://gradle.org/) for build.
Basic build can be done with 

`./gradlew build`

It will produce a jar file in the `build/libs` directory.

Build enforces the code formatting, so during the development the recommended build command is

`./gradlew spotlessApply test`

## Running development version

Sometimes you would like to run a development version of Sudachi from a jar file.
Gradle allows you to make a development jar installation of Sudachi with all dependencies with 

`./gradlew installExecutableDist`

## List of Gradle tasks

List of all Gradle tasks can be seen with `./gradlew tasks`