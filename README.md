Subsampling Scale Image View
===========================

## Quick start

**1)** Add this library as a dependency in your app's build.gradle file.

    dependencies {
        api 'com.ydevelop:editimageview.subsamplingscaleimageview:beta02'
    }

**2)** Add the view to your layout XML.

    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
            android:id="@+id/imageView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>

    </LinearLayout>

**3a)** Now, in your fragment or activity, set the image resource, asset name or file path.

    SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
    imageView.setImage(ImageSource.resource(R.drawable.monkey));
    // ... or ...
    imageView.setImage(ImageSource.asset("map.png"))
    // ... or ...
    imageView.setImage(ImageSource.uri("/sdcard/DCIM/DSCM00123.JPG"));

**3b)** Or, if you have a `Bitmap` object in memory, load it into the view. This is unsuitable for large images because it bypasses subsampling - you may get an `OutOfMemoryError`.

    SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
    imageView.setImage(ImageSource.bitmap(bitmap));

## Photo credits

* San Martino by Luca Bravo, via [unsplash.com](https://unsplash.com/photos/lWAOc0UuJ-A)
* Swiss Road by Ludovic Fremondiere, via [unsplash.com](https://unsplash.com/photos/3XN-BNRDUyY)

## About

Copyright 2018 David Morrissey, and licensed under the Apache License, Version 2.0. No attribution is necessary but it's very much appreciated. Star this project if you like it!
