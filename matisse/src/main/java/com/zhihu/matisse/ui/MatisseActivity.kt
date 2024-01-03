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
package com.zhihu.matisse.ui

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.model.AlbumCollection
import com.zhihu.matisse.internal.model.AlbumCollection.AlbumCallbacks
import com.zhihu.matisse.internal.model.SelectedItemCollection
import com.zhihu.matisse.internal.ui.AlbumPreviewActivity
import com.zhihu.matisse.internal.ui.BasePreviewActivity
import com.zhihu.matisse.internal.ui.MediaSelectionFragment
import com.zhihu.matisse.internal.ui.MediaSelectionFragment.SelectionProvider
import com.zhihu.matisse.internal.ui.SelectedPreviewActivity
import com.zhihu.matisse.internal.ui.adapter.AlbumMediaAdapter.*
import com.zhihu.matisse.internal.ui.adapter.AlbumsAdapter
import com.zhihu.matisse.internal.ui.widget.AlbumsSpinner
import com.zhihu.matisse.internal.ui.widget.CheckRadioView
import com.zhihu.matisse.internal.ui.widget.IncapableDialog
import com.zhihu.matisse.internal.utils.MediaStoreCompat
import com.zhihu.matisse.internal.utils.PathUtils
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import com.zhihu.matisse.internal.utils.SingleMediaScanner
import kotlinx.coroutines.launch

/**
 * Main Activity to display albums and media content (images/videos) in each album
 * and also support media selecting operations.
 */
class MatisseActivity : AppCompatActivity(),
    AlbumCallbacks, AdapterView.OnItemSelectedListener, SelectionProvider, View.OnClickListener,
    CheckStateListener, OnMediaClickListener, OnPhotoCapture {
    private val mAlbumCollection = AlbumCollection()
    private lateinit var mMediaStoreCompat: MediaStoreCompat
    private val mSelectedCollection = SelectedItemCollection(this)
    private lateinit var mSpec: SelectionSpec
    private lateinit var mAlbumsSpinner: AlbumsSpinner
    private lateinit var mAlbumsAdapter: AlbumsAdapter
    private lateinit var mButtonPreview: TextView
    private lateinit var mButtonApply: TextView
    private lateinit var mContainer: View
    private lateinit var mEmptyView: View
    private lateinit var mOriginalLayout: LinearLayout
    private lateinit var mOriginal: CheckRadioView
    private var mOriginalEnable = false
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        // programmatically set theme before super.onCreate()
        mSpec = SelectionSpec.getInstance()
        super.onCreate(savedInstanceState)
        setTheme(mSpec.themeId)

        if (!mSpec.hasInited) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        setContentView(R.layout.activity_matisse)
        if (mSpec.needOrientationRestriction()) {
            requestedOrientation = mSpec.orientation
        }
        if (mSpec.capture) {
            mMediaStoreCompat = MediaStoreCompat(this)
            if (mSpec.captureStrategy == null) throw RuntimeException("Don't forget to set CaptureStrategy.")
            mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy)
        }

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayHomeAsUpEnabled(true)
        val navigationIcon = toolbar.navigationIcon
        val ta = theme.obtainStyledAttributes(intArrayOf(R.attr.album_element_color))
        val color = ta.getColor(0, 0)
        ta.recycle()
        navigationIcon!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        mButtonPreview = findViewById<View>(R.id.button_preview) as TextView
        mButtonApply = findViewById<View>(R.id.button_apply) as TextView
        mButtonPreview.setOnClickListener(this)
        mButtonApply.setOnClickListener(this)
        mContainer = findViewById(R.id.container)
        mEmptyView = findViewById(R.id.empty_view)
        mOriginalLayout = findViewById(R.id.originalLayout)
        mOriginal = findViewById(R.id.original)
        mOriginalLayout.setOnClickListener(this)
        mSelectedCollection.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE)
        }
        updateBottomToolbar()
        mAlbumsAdapter = AlbumsAdapter(this, null, false)
        mAlbumsSpinner = AlbumsSpinner(this)
        mAlbumsSpinner.setOnItemSelectedListener(this)
        mAlbumsSpinner.setSelectedTextView(findViewById<View>(R.id.selected_album) as TextView)
        mAlbumsSpinner.setPopupAnchorView(findViewById(R.id.toolbar))
        mAlbumsSpinner.setAdapter(mAlbumsAdapter)
        mAlbumCollection.onCreate(this, this)
        mAlbumCollection.onRestoreInstanceState(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mAlbumCollection.loadAlbums()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mSelectedCollection.onSaveInstanceState(outState)
        mAlbumCollection.onSaveInstanceState(outState)
        outState.putBoolean("checkState", mOriginalEnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAlbumCollection.onDestroy()
        mSpec.onCheckedListener = null
        mSpec.onSelectedListener = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            "matisse",
            "onActivityResult, code " + resultCode + ", uri " + mMediaStoreCompat.currentPhotoUri
        )
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == REQUEST_CODE_PREVIEW) {
            val resultBundle = data!!.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE)
            val selected =
                resultBundle!!.getParcelableArrayList<Item>(SelectedItemCollection.STATE_SELECTION) //todo merge and replace it from master
            mOriginalEnable =
                data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false)
            val collectionType = resultBundle.getInt( //todo merge and replace it from master
                SelectedItemCollection.STATE_COLLECTION_TYPE,
                SelectedItemCollection.COLLECTION_UNDEFINED
            )
            if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                val result = Intent()
                val selectedUris = ArrayList<Uri>()
                val selectedPaths = ArrayList<String>()
                val mimeTypes = ArrayList<String>()
                if (selected != null) {
                    for (item in selected) {
                        mimeTypes.add(item.mimeType)
                        selectedUris.add(item.contentUri)
                        selectedPaths.add(PathUtils.getPath(this, item.contentUri))
                    }
                }
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris)
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths)
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_TYPES, mimeTypes)
                result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
                setResult(Activity.RESULT_OK, result)
                finish()
            } else {
                mSelectedCollection.overwrite(selected, collectionType)
                val mediaSelectionFragment = supportFragmentManager.findFragmentByTag(
                    MediaSelectionFragment::class.java.simpleName
                )
                if (mediaSelectionFragment is MediaSelectionFragment) {
                    mediaSelectionFragment.refreshMediaGrid()
                }
                updateBottomToolbar()
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE) {
            // Just pass the data back to previous calling Activity.
            val contentUri = mMediaStoreCompat.currentPhotoUri
            val path = mMediaStoreCompat.currentPhotoPath
            SingleMediaScanner(this.applicationContext, path) {
                handler.post {
                    // File f = new File(path);
                    // mSelectedCollection.add(new Item(Item.ITEM_ID_CAPTURE, "image/jpg", f.length(), 0, contentUri));
                    // onUpdate();
                    reloadCurrentAlbum()
                }
            }
            //galleryAddPic(contentUri);


            /*

            ArrayList<Uri> selected = new ArrayList<>();
            selected.add(contentUri);
            ArrayList<String> selectedPath = new ArrayList<>();
            ArrayList<String> selectedTypes = new ArrayList<>();
            selectedPath.add(path);
            selectedTypes.add("image/jpg");
            Intent result = new Intent();

            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected);
            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath);
            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_TYPES, selectedTypes);
            result.putExtra(EXTRA_RESULT_FROM_CAPTURE, true);

            setResult(RESULT_OK, result);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                MatisseActivity.this.revokeUriPermission(contentUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            finish();*/
        }
    }

    private fun galleryAddPic(contentUri: Uri) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    private fun updateBottomToolbar() {
        if (!mSpec.showPreview) {
            val bottomToolbar = findViewById<ViewGroup>(R.id.bottom_toolbar)
            bottomToolbar.visibility = View.GONE
        }
        if (!mSpec.singleSelectionModeEnabled()) {
            val selectedCount = mSelectedCollection.count()
            if (selectedCount == 0) {
                mButtonPreview.isEnabled = false
                mButtonApply.text = getString(
                    R.string.button_apply,
                    selectedCount.toString() + "/" + mSpec!!.maxSelectable
                )
            } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
                mButtonPreview.isEnabled = true
                mButtonApply.setText(R.string.button_apply_default)
            } else {
                mButtonPreview.isEnabled = true
                mButtonApply.text = getString(
                    R.string.button_apply,
                    selectedCount.toString() + "/" + mSpec!!.maxSelectable
                )
            }
            if (!mSpec.showPreview) mButtonPreview.visibility =
                View.GONE else mButtonPreview.visibility =
                View.VISIBLE
            if (mSpec.originalable) {
                mOriginalLayout.visibility = View.VISIBLE
                updateOriginalState()
            } else {
                mOriginalLayout.visibility = View.INVISIBLE
            }
        } else {
            mButtonApply.setText(null)
        }
    }

    private fun updateOriginalState() {
        mOriginal.setChecked(mOriginalEnable)
        if (countOverMaxSize() > 0) {
            if (mOriginalEnable) {
                val incapableDialog = IncapableDialog.newInstance(
                    "",
                    getString(R.string.error_over_original_size, mSpec!!.originalMaxSize)
                )
                incapableDialog.show(
                    supportFragmentManager,
                    IncapableDialog::class.java.name
                )
                mOriginal.setChecked(false)
                mOriginalEnable = false
            }
        }
    }

    private fun countOverMaxSize(): Int {
        var count = 0
        val selectedCount = mSelectedCollection.count()
        for (i in 0 until selectedCount) {
            val item = mSelectedCollection.asList()[i]
            if (item.isImage) {
                val size = PhotoMetadataUtils.getSizeInMB(item.size)
                if (size > mSpec.originalMaxSize) {
                    count++
                }
            }
        }
        return count
    }

    private fun apply() {
        val result = Intent()
        val selectedUris = ArrayList<Uri>()
        val selectedPaths = ArrayList<String>()
        val selectedTypes = ArrayList<String>()
        val selectedSizes = ArrayList<Point>()
        var point: Point
        for (item in mSelectedCollection.asList()) {
            selectedPaths.add(PathUtils.getPath(this, item.contentUri))
            selectedTypes.add(item.mimeType)
            selectedUris.add(item.uri)
            lifecycleScope.launch {
                point = PhotoMetadataUtils.getBitmapBound(item.uri, this@MatisseActivity)
                selectedSizes.add(point)
            }
        }
        result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris)
        result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths)
        result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_TYPES, selectedTypes)
        result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_SIZES, selectedSizes)
        result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.button_preview) {
            val intent = Intent(
                this,
                SelectedPreviewActivity::class.java
            )
            intent.putExtra(
                BasePreviewActivity.EXTRA_DEFAULT_BUNDLE,
                mSelectedCollection.dataWithBundle
            )
            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
            startActivityForResult(intent, REQUEST_CODE_PREVIEW)
        } else if (v.id == R.id.button_apply) {
            apply()
        } else if (v.id == R.id.originalLayout) {
            val count = countOverMaxSize()
            if (count > 0) {
                val incapableDialog = IncapableDialog.newInstance(
                    "",
                    getString(R.string.error_over_original_count, count, mSpec!!.originalMaxSize)
                )
                incapableDialog.show(
                    supportFragmentManager,
                    IncapableDialog::class.java.name
                )
                return
            }
            mOriginalEnable = !mOriginalEnable
            mOriginal.setChecked(mOriginalEnable)
            if (mSpec.onCheckedListener != null) {
                mSpec.onCheckedListener.onCheck(mOriginalEnable)
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        mAlbumCollection.setStateCurrentSelection(position)
        mAlbumsAdapter.cursor.moveToPosition(position)
        val album = Album.valueOf(
            mAlbumsAdapter.cursor
        )
        if (album.isAll && SelectionSpec.getInstance().capture) {
            album.addCaptureCount()
        }
        onAlbumSelected(album)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onAlbumLoad(cursor: Cursor) {
        mAlbumsAdapter.swapCursor(cursor)
        // select default album.
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            cursor.moveToPosition(mAlbumCollection.currentSelection)
            mAlbumsSpinner.setSelection(
                this@MatisseActivity,
                mAlbumCollection.currentSelection
            )
            val album = Album.valueOf(cursor)
            if (album.isAll && SelectionSpec.getInstance().capture) {
                album.addCaptureCount()
            }
            onAlbumSelected(album)
        }
    }

    override fun onAlbumReset() {
        mAlbumsAdapter.swapCursor(null)
    }

    private fun onAlbumSelected(album: Album) {
        Log.d("matisse", "onAlbumSelected isAll " + album.isAll + " isEmpty " + album.isEmpty)
        if (album.isEmpty) {
            mContainer.visibility = View.GONE
            mEmptyView.visibility = View.VISIBLE
        } else {
            mContainer.visibility = View.VISIBLE
            mEmptyView.visibility = View.GONE
            reloadAlbum(album)
        }
    }

    fun reloadCurrentAlbum() {
        val f = supportFragmentManager.findFragmentByTag(
            MediaSelectionFragment::class.java.simpleName
        )
        if (f != null) {
            (f as MediaSelectionFragment).reload()
        }
    }

    fun reloadAlbum(album: Album?) {
        val f = supportFragmentManager.findFragmentByTag(
            MediaSelectionFragment::class.java.simpleName
        )
        if (f != null) {
            (f as MediaSelectionFragment).reloadAlbum(album)
        } else {
            val fragment: Fragment = MediaSelectionFragment.newInstance(album)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment, MediaSelectionFragment::class.java.simpleName)
                .commitAllowingStateLoss()
        }
    }

    override fun onUpdate() {
        if (mSelectedCollection.count() >= mSpec.maxSelectable) {
            apply()
            return
        }
        // notify bottom toolbar that check state changed.
        updateBottomToolbar()
        if (mSpec.onSelectedListener != null) {
            mSpec.onSelectedListener.onSelected(
                mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString()
            )
        }
    }

    override fun onMediaClick(album: Album, item: Item, adapterPosition: Int) {
        if (!mSpec.singleSelectionModeEnabled()) {
            val intent = Intent(this, AlbumPreviewActivity::class.java)
            intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album)
            intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item)
            intent.putExtra(
                BasePreviewActivity.EXTRA_DEFAULT_BUNDLE,
                mSelectedCollection.dataWithBundle
            )
            intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
            startActivityForResult(intent, REQUEST_CODE_PREVIEW)
        } else {
            mSelectedCollection.add(item)
            apply()
        }
    }

    override fun provideSelectedItemCollection(): SelectedItemCollection {
        return mSelectedCollection
    }

    override fun capture() {
        mMediaStoreCompat.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE)
    }

    companion object {
        const val EXTRA_RESULT_SELECTION = "extra_result_selection"
        const val EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path"
        const val EXTRA_RESULT_SELECTION_TYPES = "extra_result_selection_type"
        const val EXTRA_RESULT_SELECTION_SIZES = "extra_result_selection_size"
        const val EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable"
        const val EXTRA_RESULT_FROM_CAPTURE = "extra_result_from_capture"
        private const val REQUEST_CODE_PREVIEW = 23
        private const val REQUEST_CODE_CAPTURE = 24
        const val CHECK_STATE = "checkState"
    }
}