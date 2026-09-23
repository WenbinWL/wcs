# cadc-wcs-2.1

The `cadc-wcs` library is the java interface to `WCSLib` (http://www.atnf.csiro.au/people/mcalabre/WCS/).

This library can be built on its own but testing and beyond requires `WCSLib`. The JNI binding library is
compiled and included inside the `cadc-wcs` JAR file and deployed during class loading.

## Supported WCSLib versions

Beginning with version 2.1.0, the JNI binaries are linked against `WCSLib` major versions 5, 6, or 7 (which ever is found).  The
`src/main/resources` directory includes JNI linked libraries.  To add support for a different version, build
against that version of `WCSLib`, copy the `libwcsLibJNI.so` to `src/main/resources/libwcsLibJNI.<version>.so` and add 
it to the array in `WCSLib.java`.  To remove support, delete the `src/main/resources/libwcsLibJNI.<version>.so` file from
both the `src/main/resources` directory and the `WCSLib.java` file.

Linux architecture-specific JNI libraries use the filename
`libwcsLibJNI.<version>.<architecture>.so`, where the architecture is the normalized Java architecture name
(`aarch64` or `x86_64`). The loader tries this name first and then falls back to the legacy filename above.
The included `libwcsLibJNI.8.aarch64.so` was built on Ubuntu 24.04 arm64 against WCSLib 8.2.2.

The src tree now contains a build of libwcsLibJNI.7.dylib for MacOS. This is expected to be useful to make life easier for
developers working on code that uses this library and is compatible with MacOS versions 11.x, 12.x, and 13.x. If a 10.x version
of the library is required, the library can be rebuilt following `With JNI changes` below. The new library,
in `build/libs/wcsLibJNI/shared/` should be copied into `src/main/resources` to be included in the jar file.

The MacOS JNI library uses a hardcoded path to the locally installed WCSLib C library. The included JNI library was built 
using a `homebrew` install of the WCSLib C library in `/usr/local/opt/`. If the local WCSLib C library uses a different path, 
the JNI library can be rebuilt using `With JNI changes` below.

## Building & Testing

JDK 1.8 (or higher) is required.  The Gradle Wrapper is provided.

### WCSLib

### No JNI changes

To use `cadc-wcs`, the `WCSLib` C library is required.  If it is not installed, skip running the unit tests using the `-x test` option:

 1. `$> ./gradlew -i -x test clean build` -- build the JAR file and do not run the unit tests.

Or in an environment where `WCSLib` with a supported version (currently 5.x, 6.x, or 7.x) is installed:

 1. `$> ./gradlew -i clean build test` -- build the JAR file and run the unit tests.

### With JNI changes

If JNI changes are made to wcslib.c, use the `build-jni.gradle` build file. 

The `WCSLib` C library is required. If it is not installed, skip running the unit tests using the `-x test` option:

1. `$> ./gradlew -i -b build-jni.gradle -x test clean build && ./gradlew -i build` -- build the JAR file 
and do not run the unit tests.

Or in an environment where `WCSLib`with a supported version (currently 5.x, 6.x, or 7.x) is installed:

1. `$> ./gradlew -i -b build-jni.gradle clean build test && ./gradlew -i build` -- build the JAR file and run the unit tests.
