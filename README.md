# EditImageView

android image edit

## gradle 

#### core

    implementation 'com.ydevelop:editimageview:beta12'

#### circle

    implementation 'com.ydevelop:editimageview:beta12'
    implementation 'com.ydevelop:editimageview.circle:0.0.3'

#### eraser

    implementation 'com.ydevelop:editimageview:beta12'
    implementation 'com.ydevelop:editimageview.eraser:0.0.3'
    
#### line

    implementation 'com.ydevelop:editimageview:beta12'
    implementation 'com.ydevelop:editimageview.line:0.0.3'
    
#### point

    implementation 'com.ydevelop:editimageview:beta12'
    implementation 'com.ydevelop:editimageview.point:0.0.3'
    
#### rect

    implementation 'com.ydevelop:editimageview:beta12'
    implementation 'com.ydevelop:editimageview.rect:0.0.3'
    
#### text

    implementation 'com.ydevelop:editimageview:beta12'
    implementation 'com.ydevelop:editimageview.text:0.0.4'

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