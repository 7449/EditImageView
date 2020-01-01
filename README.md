# EditImageView

android image edit

## 待解决

* Bitmap模式加载图片开启橡皮擦

## gradle 

#### core

    implementation 'com.ydevelop:editimageview:beta11'

#### circle

    implementation 'com.ydevelop:editimageview:beta11'
    implementation 'com.ydevelop:editimageview.circle:0.0.2'

#### eraser

    implementation 'com.ydevelop:editimageview:beta11'
    implementation 'com.ydevelop:editimageview.eraser:0.0.2'
    
#### line

    implementation 'com.ydevelop:editimageview:beta11'
    implementation 'com.ydevelop:editimageview.line:0.0.2'
    
#### point

    implementation 'com.ydevelop:editimageview:beta11'
    implementation 'com.ydevelop:editimageview.point:0.0.2'
    
#### rect

    implementation 'com.ydevelop:editimageview:beta11'
    implementation 'com.ydevelop:editimageview.rect:0.0.2'
    
#### text

    implementation 'com.ydevelop:editimageview:beta11'
    implementation 'com.ydevelop:editimageview.text:0.0.3'

![](https://github.com/7449/EditImageView/blob/master/screen/edit_image_sample.gif)
   
## custom listener

    class CustomCallback : OnEditImageListener {
        override fun onLastImageEmpty() {
        }
        override fun onLastCacheMax() {
        }
        override fun onDeleteText() {
        }
    }
    
## custom action listener

    class SimpleOnEditImageCustomActionListener : OnEditImageCustomActionListener {
    
        override fun onDraw(editImageView: EditImageView, canvas: Canvas) {
            Log.d(TAG, "onDraw")
        }
        override fun onDown(editImageView: EditImageView, x: Float, y: Float) {
            Log.d(TAG, "onDown")
        }
        override fun onMove(editImageView: EditImageView, x: Float, y: Float) {
            Log.d(TAG, "onMove")
            editImageView.refresh()
        }
        override fun onUp(editImageView: EditImageView, x: Float, y: Float) {
            Log.d(TAG, "onUp")
        }
        override fun onSaveImageCache(editImageView: EditImageView) {
        }
        override fun onLastImageCache(editImageView: EditImageView, editImageCache: EditImageCache) {
        }
    }