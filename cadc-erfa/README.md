# cadc-erfa-1.0

The `cadc-erfa` library is the java interface to `ERFA (Essential Routines for Fundamental Astronomy)` 
(https://github.com/liberfa/erfa).

The JNI binding and its ERFA shared-library dependency are included inside the `cadc-erfa` JAR file for Linux and
deployed during class loading. On macOS, ERFA must be installed separately.

## Supported ERFA versions

The JNI binary is linked against `ERFA` version 2.0.0. The `src/main/resources` directory includes the JNI linked library.
To add support for a different version, build against that version of `ERFA`, and copy the JNI library to
`src/main/resources/liberfaLibJNI.<architecture>.<extension>`.

JNI libraries use the filename `liberfaLibJNI.<architecture>.<extension>`, where the architecture is the normalized Java
architecture name (`aarch64` or `x86_64`) and the extension identifies the operating system (`so` on Linux and `dylib` on
macOS). The loader selects only the resource matching the current operating system and architecture. The included
`liberfaLibJNI.aarch64.so` was built on Ubuntu 24.04 arm64 against ERFA 2.0.1.

The src tree contains a macOS arm64 build. This is expected to make life easier for
developers working on code that uses this library and and is compatible with MacOS versions 11.x, 12.x, and 13.x. 
If a 10.x version of the library is required, the library can be rebuilt following `With JNI changes` below. 
The new library, in `build/libs/erfaLibJNI/shared/` should be copied into `src/main/resources` to be included in the jar file.

The macOS JNI library uses a hardcoded path to the locally installed ERFA C library. The included aarch64 library uses
`/opt/homebrew/lib/`. If the local ERFA C library uses a different path, the JNI library can be rebuilt using
`With JNI changes` below.



## Building & Testing

JDK 1.8 (or higher) is required.  The Gradle Wrapper is provided.

### ERFA

### No JNI changes

On Linux, the JAR includes ERFA for `aarch64` and `x86_64`, so no system ERFA package is required. The loader extracts
and loads ERFA before loading the JNI binding. On macOS, ERFA is still required at the path described above.

To build the JAR without running the unit tests:

 1. `$> ./gradlew -i -x test clean build` -- build the JAR file and do not run the unit tests.

To build the JAR and run the unit tests:

 1. `$> ./gradlew -i clean build test` -- build the JAR file and run the unit tests.

### With JNI changes

If JNI changes are made to `erfalib.c`, use the `build-jni.gradle` build file. 

The `ERFA` C library is required. If it is not installed, skip running the unit tests using the `-x test` option:

1. `$> ./gradlew -i -b build-jni.gradle -x test clean build && ./gradlew -i build` -- build the JAR file 
and do not run the unit tests.

Or in an environment where `ERFA` with a supported version (currently 2.0.0) is installed:

1. `$> ./gradlew -i -b build-jni.gradle clean build test && ./gradlew -i build` -- build the JAR file and run the unit tests.
