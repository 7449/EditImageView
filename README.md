# EditImageView

android image edit

## gradle 

#### core

    implementation 'com.ydevelop:editimageview:beta09'

#### circle

    implementation 'com.ydevelop:editimageview:beta09'
    implementation 'com.ydevelop:editimageview.circle:0.0.1'

#### eraser

    implementation 'com.ydevelop:editimageview:beta09'
    implementation 'com.ydevelop:editimageview.eraser:0.0.1'
    
#### line

    implementation 'com.ydevelop:editimageview:beta09'
    implementation 'com.ydevelop:editimageview.line:0.0.1'
    
#### point

    implementation 'com.ydevelop:editimageview:beta09'
    implementation 'com.ydevelop:editimageview.point:0.0.1'
    
#### rect

    implementation 'com.ydevelop:editimageview:beta09'
    implementation 'com.ydevelop:editimageview.rect:0.0.1'
    
#### text

    implementation 'com.ydevelop:editimageview:beta09'
    implementation 'com.ydevelop:editimageview.text:0.0.1'

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