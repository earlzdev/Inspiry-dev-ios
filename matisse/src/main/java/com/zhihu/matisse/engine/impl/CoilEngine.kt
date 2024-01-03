package com.zhihu.matisse.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import coil.size.Scale
import com.zhihu.matisse.engine.ImageEngine

class CoilEngine(private val imageLoader: ImageLoader) : ImageEngine {

    override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, uri: Uri?) {
        imageView.load(uri, imageLoader) {
            size(resize)
            crossfade(true)
            placeholder(placeholder)
            scale(Scale.FILL)
        }
    }

    override fun loadGifThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, uri: Uri?) {
        loadThumbnail(context, resize, placeholder, imageView, uri)
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri?) {
        imageView.load(uri, imageLoader) {
            size(resizeX, resizeY)
            crossfade(true)
            scale(Scale.FILL)
        }
    }

    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri?) {
        loadImage(context, resizeX, resizeY, imageView, uri)
    }

    override fun supportAnimatedGif() = true

}