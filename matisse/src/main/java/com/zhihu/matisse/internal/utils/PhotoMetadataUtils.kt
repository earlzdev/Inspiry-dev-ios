/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Point
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object PhotoMetadataUtils {
    private val TAG = PhotoMetadataUtils::class.java.simpleName
    private const val MAX_WIDTH = 1600
    private const val SCHEME_CONTENT = "content"
    fun getBitmapSize(uri: Uri, activity: Activity): Point {
        val resolver = activity.contentResolver
        val imageSize = getBitmapBound(uri, activity)
        var w = imageSize.x
        var h = imageSize.y
        if (shouldRotate(resolver, uri)) {
            w = imageSize.y
            h = imageSize.x
        }
        if (h == 0) return Point(MAX_WIDTH, MAX_WIDTH)
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels.toFloat()
        val screenHeight = metrics.heightPixels.toFloat()
        val widthScale = screenWidth / w
        val heightScale = screenHeight / h
        return if (widthScale > heightScale) {
            Point((w * widthScale).toInt(), (h * heightScale).toInt())
        } else Point((w * widthScale).toInt(), (h * heightScale).toInt())
    }

    fun getBitmapBound(uri: Uri, activity: Activity): Point {
        val resolver = activity.contentResolver
        var isForOrientation: InputStream? = null
        val width: Int
        val height: Int
        return try {
            val media: Bitmap
            if (uri.pathSegments[1] == "video") {
                val metaRetriever = MediaMetadataRetriever()
                with(metaRetriever) {
                    setDataSource(activity, uri)
                    media = frameAtTime!!
                    width = media.width
                    height = media.height
                }
                metaRetriever.release() //we can't use "use" here. (AutoCloseable since API 29)
            } else {
                media = MediaStore.Images.Media.getBitmap(resolver, uri)
                isForOrientation = resolver.openInputStream(uri)
                val orientation = isForOrientation?.let {
                    ExifInterface(it).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 1
                    )
                }
                if (orientation == 6 || orientation == 8) {
                    width = media.height
                    height = media.width
                } else {
                    width = media.width
                    height = media.height
                }
            }
            Point(width, height)
        } catch (e: FileNotFoundException) {
            Point(0, 0)
        } catch (e: IOException) {
            e.printStackTrace()
            Point(0, 0)
        } finally {
            if (isForOrientation != null) {
                try {
                    isForOrientation.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getPath(resolver: ContentResolver, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        if (SCHEME_CONTENT == uri.scheme) {
            var cursor: Cursor? = null
            return try {
                cursor = resolver.query(
                    uri, arrayOf(MediaStore.Images.ImageColumns.DATA),
                    null, null, null
                )
                if (cursor == null || !cursor.moveToFirst()) {
                    null
                } else cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
            } finally {
                cursor?.close()
            }
        }
        return uri.path
    }

    fun isAcceptable(context: Context, item: Item): IncapableCause? {
        if (!isSelectableType(context, item)) {
            return IncapableCause(
                context.getString(R.string.error_file_type) + " " + context.contentResolver.getType(
                    item.uri
                )
            )
        }
        if (SelectionSpec.getInstance().filters != null) {
            for (filter in SelectionSpec.getInstance().filters) {
                val incapableCause = filter.filter(context, item)
                if (incapableCause != null) {
                    return incapableCause
                }
            }
        }
        return null
    }

    private fun isSelectableType(context: Context?, item: Item): Boolean {
        if (context == null) {
            return false
        }
        val resolver = context.contentResolver
        for (type in SelectionSpec.getInstance().mimeTypeSet) {
            if (type.checkType(resolver, item.contentUri)) {
                return true
            }
        }
        return false
    }

    public fun shouldRotate(resolver: ContentResolver, uri: Uri): Boolean {
        val exif: ExifInterface
        exif = try {
            ExifInterfaceCompat.newInstance(getPath(resolver, uri))
        } catch (e: IOException) {
            Log.e(
                TAG,
                "could not read exif info of the image: $uri"
            )
            return false
        }
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
        return (orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270)
    }

    fun getSizeInMB(sizeInBytes: Long): Float {
        val df = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        df.applyPattern("0.0")
        var result = df.format((sizeInBytes.toFloat() / 1024 / 1024).toDouble())
        Log.e(
            TAG,
            "getSizeInMB: $result"
        )
        result = result.replace(",".toRegex(), ".") // in some case , 0.0 will be 0,0
        return java.lang.Float.valueOf(result)
    }

}