# EditImageView

android image edit

## gradle 

    compile 'com.ydevelop:editimageview:beta03'
    
![](https://github.com/7449/EditImageView/blob/master/screen/edit_image_sample.gif)
   
## sample 

    editImageView
            .apply {
                setOnEditImageInitializeListener(SimpleOnEditImageInitializeListener())
            }
               
  [see MainActivity](https://github.com/7449/EditImageView/blob/master/app/src/main/java/com/edit/image/sample/MainActivity.kt)
    
## custom listener

    class CustomCallback : OnEditImageListener {
        override fun onLastImageEmpty() {
        }
        override fun onLastCacheMax() {
        }
        override fun onDeleteText() {
        }
    }
    
## custom Paint

    class CustomPaint : OnEditImageInitializeListener {
        override fun initPointPaint(editImageView: EditImageView): Paint {
            return Paint()
        }
        override fun initEraserPaint(editImageView: EditImageView): Paint {
            return Paint()
        }
        override fun initTextPaint(editImageView: EditImageView): TextPaint {
            return TextPaint()
        }
        override fun initTextFramePaint(editImageView: EditImageView): Paint {
            return Paint()
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