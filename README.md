# Pixel Perfect

The tool can be used by developers as well as designers to achieve the highest visual quality of an app and to add more transparency to the both sides.
Pixel Perfect Tool lets user to use the following features:
- control Overlay Transperency
- see and fix Overlay offset position
- inverse Overlay Image.

![Pixel Perfect Sample](https://s3.amazonaws.com/f.cl.ly/items/44371W2K3E2V3z1h3f2A/pixel_perfect_demo.gif?v=807da98a "Pixel Perfect Sample")


## Getting started

To add the library to the project update `build.gradle` file:

```gradle
 dependencies {
   compile 'is.handsome.pixelperfect:pixelperfect:0.2.1'
 }
```

To add your overlay images you should simply create folder in assets directory and specify its name in PixelPerfect config builder. Or use default 'pixelperfect' folder name.

![assets folder](https://s3.amazonaws.com/f.cl.ly/items/1m39120Y423f242R2k1K/Image%202016-02-17%20at%206.53.34%20PM.png?v=1d4527b7)

To show PixelPerfect overlay you should add the following lines in your code:
```java
 PixelPerfect.Config config = new PixelPerfect.Config.Builder()
         .overlayImagesAssetsPath("overlays")
         .overlayInitialImageName("im_android.png")
         .build();
 PixelPerfect.show(HomeActivity.this, config);
```

Or use default Pixel Perfect configuration:
```java
 PixelPerfect.show(HomeActivity.this);
```

Also you can use `PixelPerfect.hasPermission(context)` and `PixelPerfect.askForPermission(context)` methods to handle SYSTEM_ALERT_WINDOW permission carefully for Android M.

Please, see sample application that demonstrates the main idea of the Pixel Perfect tool.
