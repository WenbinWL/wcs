# cadc-wcs-2.1

The `cadc-wcs` library is the java interface to `WCSLib` (http://www.atnf.csiro.au/people/mcalabre/WCS/).

The JNI binding and its WCSLib shared-library dependency are included inside the `cadc-wcs` JAR file for Linux and
deployed during class loading. On macOS, WCSLib must be installed separately.

## Supported WCSLib versions

The JNI binaries are linked against `WCSLib` major version 8. The `src/main/resources` directory includes the JNI
libraries for the supported operating systems and architectures. To replace the supported WCSLib version, build against
that version, copy the JNI library to `src/main/resources/libwcsLibJNI.<version>.<architecture>.<extension>`, and update
the version in `WCSLib.java`.

JNI libraries use the filename `libwcsLibJNI.<version>.<architecture>.<extension>`, where the architecture is the
normalized Java architecture name (`aarch64` or `x86_64`) and the extension identifies the operating system (`so` on
Linux and `dylib` on macOS). The loader selects only the resource matching the current operating system and architecture.
The included `libwcsLibJNI.8.aarch64.so` was built on Ubuntu 24.04 arm64 against WCSLib 8.2.2.

The src tree contains a macOS aarch64 build. This is expected to make life easier for
developers working on code that uses this library and is compatible with MacOS versions 11.x, 12.x, and 13.x. If a 10.x version
of the library is required, the library can be rebuilt following `With JNI changes` below. The new library,
in `build/libs/wcsLibJNI/shared/` should be copied into `src/main/resources` to be included in the jar file.

The macOS JNI library uses a hardcoded path to the locally installed WCSLib C library. The included aarch64 library uses
`/opt/homebrew/opt/`. If the local WCSLib C library uses a different path, the JNI library can be rebuilt using
`With JNI changes` below.

## Building & Testing

JDK 1.8 (or higher) is required.  The Gradle Wrapper is provided.

### WCSLib

### No JNI changes

On Linux, the JAR includes WCSLib 8.4 for `aarch64` and `x86_64`, so no system WCSLib package is required. The loader
extracts and loads WCSLib before loading the JNI binding. On macOS, WCSLib is still required at the path described above.

To build the JAR without running the unit tests:

 1. `$> ./gradlew -i -x test clean build` -- build the JAR file and do not run the unit tests.

To build the JAR and run the unit tests:

 1. `$> ./gradlew -i clean build test` -- build the JAR file and run the unit tests.

### With JNI changes

If JNI changes are made to wcslib.c, use the `build-jni.gradle` build file. 

The `WCSLib` C library is required. If it is not installed, skip running the unit tests using the `-x test` option:

1. `$> ./gradlew -i -b build-jni.gradle -x test clean build && ./gradlew -i build` -- build the JAR file 
and do not run the unit tests.

Or in an environment where `WCSLib` 8.x is installed:

1. `$> ./gradlew -i -b build-jni.gradle clean build test && ./gradlew -i build` -- build the JAR file and run the unit tests.
