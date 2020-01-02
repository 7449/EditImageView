# EditImageView

android image edit

## gradle 

[core]![](https://api.bintray.com/packages/ydevelop/maven/editimageview/images/download.svg)

[circle]![](https://api.bintray.com/packages/ydevelop/maven/editimageview.circle/images/download.svg)

[eraser]![](https://api.bintray.com/packages/ydevelop/maven/editimageview.eraser/images/download.svg)

[line]![](https://api.bintray.com/packages/ydevelop/maven/editimageview.line/images/download.svg)

[point]![](https://api.bintray.com/packages/ydevelop/maven/editimageview.point/images/download.svg)

[rect]![](https://api.bintray.com/packages/ydevelop/maven/editimageview.rect/images/download.svg)

[text]![](https://api.bintray.com/packages/ydevelop/maven/editimageview.text/images/download.svg)

#### core

    implementation 'com.davemorrissey.labs:subsampling-scale-image-view:3.10.0'
    implementation 'com.ydevelop:editimageview:version'

#### circle

    implementation 'com.ydevelop:editimageview:version'
    implementation 'com.ydevelop:editimageview.circle:version'

#### eraser

    implementation 'com.ydevelop:editimageview:version'
    implementation 'com.ydevelop:editimageview.eraser:version'
    
#### line

    implementation 'com.ydevelop:editimageview:version'
    implementation 'com.ydevelop:editimageview.line:version'
    
#### point

    implementation 'com.ydevelop:editimageview:version'
    implementation 'com.ydevelop:editimageview.point:version'
    
#### rect

    implementation 'com.ydevelop:editimageview:version'
    implementation 'com.ydevelop:editimageview.rect:version'
    
#### text

    implementation 'com.ydevelop:editimageview:version'
    implementation 'com.ydevelop:editimageview.text:version'

![](https://github.com/7449/EditImageView/blob/master/screen/edit_image_sample.gif)
   
## custom listener

    class CustomCallback : OnEditImageListener {
        override fun onLastImageEmpty() {
        }
        override fun onLastCacheMax() {
        }
    }
    
## custom action listener

     class SimpleOnEditAction : OnEditImageAction {
         override fun onDraw(callback: OnEditImageCallback, canvas: Canvas) {
         }
     
         override fun onDrawCache(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
         }
     
         override fun onDrawBitmap(callback: OnEditImageCallback, canvas: Canvas, editImageCache: EditImageCache) {
         }
     
         override fun onDown(callback: OnEditImageCallback, x: Float, y: Float) {
         }
     
         override fun onMove(callback: OnEditImageCallback, x: Float, y: Float) {
         }
     
         override fun onUp(callback: OnEditImageCallback, x: Float, y: Float) {
         }
     
         override fun copy(): OnEditImageAction {
             return SimpleOnEditAction()
         }
     }