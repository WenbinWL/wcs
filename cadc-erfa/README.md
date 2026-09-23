# cadc-erfa-1.0

The `cadc-erfa` library is the java interface to `ERFA (Essential Routines for Fundamental Astronomy)` 
(https://github.com/liberfa/erfa).

This library can be built on its own but testing and beyond requires `liberfa`, the ERFA C library. 
The JNI binding library is compiled and included inside the `cadc-erfa` JAR file and deployed during class loading.

## Supported ERFA versions

The JNI binary is linked against `ERFA` version 2.0.0. The `src/main/resources` directory includes the JNI linked library.
To add support for a different version, build against that version of `ERFA`, and copy the `liberfaLibJNI.so` 
to `src/main/resources/`.

Linux architecture-specific JNI libraries use the filename `liberfaLibJNI.<architecture>.so`, where the architecture
is the normalized Java architecture name (`aarch64` or `x86_64`). The loader tries this name first and then falls back
to the legacy filename above. The included `liberfaLibJNI.aarch64.so` was built on Ubuntu 24.04 arm64 against ERFA 2.0.1.

The src tree now contains a build of `liberfaLibJNI.dylib` for MacOS. This is expected to be useful to make life easier for
developers working on code that uses this library and and is compatible with MacOS versions 11.x, 12.x, and 13.x. 
If a 10.x version of the library is required, the library can be rebuilt following `With JNI changes` below. 
The new library, in `build/libs/erfaLibJNI/shared/` should be copied into `src/main/resources` to be included in the jar file.

The MacOS JNI library uses a hardcoded path to the locally installed ERFA C library. The included JNI library was built
using a `homebrew` install of the ERFA C library in `/usr/local/opt/`. If the local ERFA C library uses a different path,
the JNI library can be rebuilt using `With JNI changes` below.



## Building & Testing

JDK 1.8 (or higher) is required.  The Gradle Wrapper is provided.

### ERFA

### No JNI changes

To use `cadc-erfa`, the `ERFA` C library is required.  If it is not installed, skip running the unit tests using the `-x test` option:

 1. `$> ./gradlew -i -x test clean build` -- build the JAR file and do not run the unit tests.

Or in an environment where `ERFA` with a supported version (currently 2.0.0) is installed:

 1. `$> ./gradlew -i clean build test` -- build the JAR file and run the unit tests.

### With JNI changes

If JNI changes are made to `erfalib.c`, use the `build-jni.gradle` build file. 

The `ERFA` C library is required. If it is not installed, skip running the unit tests using the `-x test` option:

1. `$> ./gradlew -i -b build-jni.gradle -x test clean build && ./gradlew -i build` -- build the JAR file 
and do not run the unit tests.

Or in an environment where `ERFA` with a supported version (currently 2.0.0) is installed:

1. `$> ./gradlew -i -b build-jni.gradle clean build test && ./gradlew -i build` -- build the JAR file and run the unit tests.
