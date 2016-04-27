# TUNE Segment Integration for Android
Segment integration for the TUNE Android SDK.

## Installation
To install the TUNE Segment integration, simply add these lines to your
module's build.gradle file:
```groovy
dependencies {
  compile 'com.segment.analytics.android:analytics:4.+'
  compile 'com.tune:tune-segment-integration:1.0.0'
}
```
Please add at least the following permissions to your application's AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

## Setup
Add the following lines to your Application:

```java
Analytics analytics = new Analytics.Builder(getApplicationContext(), "SEGMENT_WRITE_KEY")
    .use(TuneIntegration.FACTORY)
    .build();
```

Now you can use the Segment API to measure events with TUNE!

## Usage

### Installs and App Opens
Measuring installs and app opens from deeplinks with TUNE is automatically wired into this integration,
so you don't need to worry about adding anything for that.


### Identify Users
```java
analytics.identify();
```
will set user identifiers in TUNE for `userId`, `email`, `phone`, and `username`.

```java
analytics.reset();
```
will reset any user identifiers set by `identify`.


### Track Actions
```java
analytics.track();
```
will measure events in TUNE. If present, the `revenue`, `currency`, `orderId`, `productId`, and `category` fields will automatically
map to corresponding fields for the event in TUNE.

## License
See LICENSE file.