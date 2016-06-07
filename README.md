# PixelPerfect

PixelPerfect is aimed to help you to create pixel perfect UI for Android apps. It lets developers and designers easily compare implementation with reference design and fix visual difference between them.

![Pixel Perfect Sample](https://s3.amazonaws.com/f.cl.ly/items/1L3b1C3h1s2k2t350C2D/ezgif.com-resize.gif?v=391643b5 "Pixel Perfect Sample")

#### Supported features:
- Picking overlay image. 
- Adjustment transparency.
- Moving overlay.
- Measuring offset.
- Inverse mode.

## Download

Grab via Gradle:

```gradle
compile 'is.handsome.pixelperfect:pixelperfect:1.0.1'
```

or Maven:

```maven
<dependency>
<groupId>is.handsome.pixelperfect</groupId>
<artifactId>pixelperfect</artifactId>
<version>1.0.1</version>
<type>pom</type>
</dependency>
```

## Getting started

#### Show PixelPerfect
```java
PixelPerfect.show(HomeActivity.this);
```

In default configuration PixelPerfect will be linked with 'pixelperfect' assets folder.

![Assets](https://s3.amazonaws.com/f.cl.ly/items/3m231E1W312M0U0X2t3U/assets.png?v=630e4d1b "Assets")

If you want to use different folder, please read `Configuration` section.

#### Hide PixelPerfect
```java
PixelPerfect.hide();
```

## Configuration

`PixelPerfect.Config` provides possibility to configure major attributes of PixelPerfect. This snippet demonstrates usage of custom `overlayImagesAssetsPath` (assets folder for overlay images) and `overlayActiveImageName` (name of active by default overlay image):

```java
PixelPerfect.Config config = new PixelPerfect.Config.Builder()
.overlayImagesAssetsPath("my_overlays")
.overlayActiveImageName("main.png")
.build();
PixelPerfect.show(HomeActivity.this, config);
```

## Permissions and Android Marshmallow+

PixelPerfect requires SYSTEM_ALERT_WINDOW permission to run properly. So, for Marshmallow and later versions you have to handle permissions with `PixelPerfect.hasPermission(context)` and `PixelPerfect.askForPermission(context)` methods.

You can find demonstartion of its usage in Sample app.

