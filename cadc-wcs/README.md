# cadc-wcs-2.1

The `cadc-wcs` library is the java interface to `WCSLib` (http://www.atnf.csiro.au/people/mcalabre/WCS/).

This library can be built on its own but testing and beyond requires `WCSLib`. The JNI binding library is
compiled and included inside the `cadc-wcs` JAR file and deployed during class loading.

## Supported WCSLib versions

Beginning with version 2.1.0, the JNI binaries are linked against supported `WCSLib` major versions (whichever is found). The
`src/main/resources` directory includes JNI linked libraries.  To add support for a different version, build
against that version of `WCSLib`, copy the JNI library to
`src/main/resources/libwcsLibJNI.<version>.<architecture>.<extension>` and add
it to the array in `WCSLib.java`. To remove support, delete the corresponding architecture-specific JNI files from
`src/main/resources` and remove the version from `WCSLib.java`.

JNI libraries use the filename `libwcsLibJNI.<version>.<architecture>.<extension>`, where the architecture is the
normalized Java architecture name (`aarch64` or `x86_64`) and the extension identifies the operating system (`so` on
Linux and `dylib` on macOS). The loader selects only the resource matching the current operating system and architecture.
The included `libwcsLibJNI.8.aarch64.so` was built on Ubuntu 24.04 arm64 against WCSLib 8.2.2.

The src tree contains macOS builds for supported architectures. These are expected to make life easier for
developers working on code that uses this library and is compatible with MacOS versions 11.x, 12.x, and 13.x. If a 10.x version
of the library is required, the library can be rebuilt following `With JNI changes` below. The new library,
in `build/libs/wcsLibJNI/shared/` should be copied into `src/main/resources` to be included in the jar file.

The macOS JNI libraries use a hardcoded path to the locally installed WCSLib C library. The included x86_64 library was
built using a Homebrew install in `/usr/local/opt/`; the aarch64 library uses `/opt/homebrew/opt/`. If the local WCSLib C
library uses a different path, the JNI library can be rebuilt using `With JNI changes` below.

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
