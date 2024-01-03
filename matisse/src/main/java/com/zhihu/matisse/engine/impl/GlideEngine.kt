/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.zhihu.matisse.engine.ImageEngine

/**
 * [ImageEngine] implementation using Glide.
 */
class GlideEngine(val createGlideApp: (Context) -> RequestManager = { Glide.with(it) }) : ImageEngine {
    override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, uri: Uri) {
        createGlideApp(context)
            .load(uri)
            .override(resize)
            .placeholder(placeholder)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transition(withCrossFade())
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .into(imageView)
    }

    override fun loadGifThumbnail(
        context: Context, resize: Int, placeholder: Drawable, imageView: ImageView,
        uri: Uri
    ) {
        createGlideApp(context)
            .asGif()
            .load(uri)
            .override(resize)
            .placeholder(placeholder)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .into(imageView)
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        createGlideApp(context)
            .load(uri)
            .override(resizeX, resizeY)
            .priority(Priority.HIGH)
            .fitCenter()
            .into(imageView)
    }

    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        createGlideApp(context)
            .asGif()
            .load(uri)
            .override(resizeX, resizeY)
            .priority(Priority.HIGH)
            .fitCenter()
            .into(imageView)
    }

    override fun supportAnimatedGif(): Boolean {
        return true
    }
}