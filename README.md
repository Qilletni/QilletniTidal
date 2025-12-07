# Qilletni Tidal

This is a Qilletni package which implements Tidal through a service provider. For documentation on using the package, see [Tidal Integration](https://qilletni.dev/quickstart/tidal_integration/) documentation.

## Building

This repository relies on a fork of the Tidal Android SDK, located at [Qilletni/tidal-sdk-android-standalone](https://github.com/Qilletni/tidal-sdk-android-standalone). The fork is a modification of the original repository that removes any Android components, and implements OAuth2 auth. Because of its Kotlin dependency, Java 21 must be used for building this, whereas Java 22 is required for any other Qilletni build (including the Tidal package). This is a relatively noninvative patch on top of the project, so it must be built locally, as described below.

```
git clone https://github.com/Qilletni/tidal-sdk-android-standalone
cd tidal-sdk-android-standalone
./gradlew -x javadoc :common:publishToMavenLocal
./gradlew -x javadoc :tidalapi:publishToMavenLocal
```

From here, the `com.tidal.sdk:tidalapi-standalone:0.3.21` package is available in Maven local.

To build the Qilletni package, run `qilletni build` in the root of this repository.
