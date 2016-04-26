# TUNE Segment Integration for Android
Segment integration for the TUNE Android SDK.

## Installation
To install the TUNE Segment integration, simply add these lines to your
module's build.gradle file:
```groovy
dependencies {
  compile 'com.segment.analytics.android:analytics:4.0.4'
  compile 'com.tune:tune-segment-integration:1.0.0'
}
```
Please add at least the following permissions to your application's AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

## Usage
Add the following lines to your Application:

```java
private static final String SEGMENT_WRITE_KEY = " ... ";

Analytics analytics = new Analytics.Builder(getApplicationContext(), "SEGMENT_WRITE_KEY")
    .use(TuneIntegration.FACTORY)
    .build();
```

Now you can use the Segment API to measure events with TUNE!

Measuring sessions and deeplinks with TUNE is automatically wired into Segment,
so you don't need to worry about adding anything for that.

```java
analytics.track();
```
will measure events with TUNE.

```java
analytics.identify();
```
will set user identifiers with TUNE.

## License
See LICENSE file.