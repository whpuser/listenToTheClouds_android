package com.example.listen_to_the_clouds.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter


//图像模糊工具类
object ImageBlurUtils {
    fun blurInto(imageView: ImageView, source: Bitmap, context: Context, blurSize: Float = 4.0f) {
        imageView.post {
            val width = imageView.width
            val height = imageView.height
            if (width == 0 || height == 0) return@post

            val scaledBitmap = Bitmap.createScaledBitmap(source, width, height, true)

            val gpuImage = GPUImage(context)
            gpuImage.setImage(scaledBitmap)
            val filter = GPUImageGaussianBlurFilter().apply {
                setBlurSize(blurSize)
            }
            gpuImage.setFilter(filter)

            imageView.setImageBitmap(gpuImage.bitmapWithFilterApplied)
        }
    }
}
