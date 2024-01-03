package app.inspiry.stickers.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.setMargins
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.log.KLogger
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Media
import app.inspiry.core.media.Template
import app.inspiry.core.util.getFileName
import app.inspiry.core.util.removeExt
import app.inspiry.core.ActivityRedirector
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.media.MediaVector
import app.inspiry.core.serialization.MediaSerializer
import app.inspiry.stickers.StickersViewModel
import app.inspiry.stickers.helpers.StickersViewModelFactory
import app.inspiry.stickers.providers.MediaWithPath
import app.inspiry.stickers.providers.StickersProvider
import app.inspiry.stickers.util.StickerAndroidUtil
import app.inspiry.textanim.TemplateViewHolder
import app.inspiry.utilities.toCColor
import app.inspiry.utils.autoScroll
import app.inspiry.utils.dpToPxInt
import app.inspiry.utils.getPostMessageCompat
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.infoview.InfoViewColorsLight
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.InspTemplateViewCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class StickersActivity : AppCompatActivity() {

    lateinit var viewModel: StickersViewModel
    val colors: StickersColors = StickersDarkColors()

    val logger: KLogger by inject {
        parametersOf("stickers-activity")
    }

    val licenseManger: LicenseManager by inject()
    val activityRedirector: ActivityRedirector by inject()
    val analyticsManager: AnalyticsManager by inject()
    val json: Json by inject()
    val provider: StickersProvider by inject()
    val unitsConverter: BaseUnitsConverter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            StickersViewModelFactory(
                provider,
                savedInstanceState?.getString("current_category", null),
                savedInstanceState?.getInt("current_sticker_index", -1)
            )
        )[StickersViewModel::class.java]


        setContent {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(
                    background = colors.background.toCColor(),
                    isLight = false
                )
            ) {
                SystemUi(windows = window)
                Main()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_category", viewModel.currentCategory.value)
        outState.putInt("current_sticker_index", viewModel.currentStickerIndex.value)
    }

    @Composable
    fun SystemUi(windows: Window) {

        windows.statusBarColor = MaterialTheme.colors.background.toArgb()
        windows.navigationBarColor = MaterialTheme.colors.background.toArgb()

        if (Build.VERSION.SDK_INT >= 26) {
            @Suppress("DEPRECATION")
            if (MaterialTheme.colors.background.luminance() > 0.5f) {
                windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

            @Suppress("DEPRECATION")
            if (MaterialTheme.colors.background.luminance() > 0.5f) {
                windows.decorView.systemUiVisibility = windows.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
    }


    @Composable
    fun Main() {
        Column(
            Modifier
                .fillMaxSize()
                .clickable(enabled = false) {}
                .background(colors.background.toCColor())
        ) {
            TopBar()
            Tabs(tabIds = viewModel.categories)
            Stickers()
        }

    }

    @Composable
    fun TopBar() {
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Row(
                    Modifier
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .clickable { onBackPressedDispatcher.onBackPressed() }
                        .padding(start = 28.dp, end = 10.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.ic_arrow_back_edit),
                        contentDescription = "back",
                        colorFilter = ColorFilter.tint(
                            colors.topBarText.toCColor(),
                            androidx.compose.ui.graphics.BlendMode.SrcAtop
                        ),
                        modifier = Modifier.padding(end = 9.dp)
                    )

                    BasicText(
                        text = stringResource(id = app.inspiry.projectutils.R.string.back),
                        overflow = TextOverflow.Ellipsis, maxLines = 1,
                        style = TextStyle(
                            color = colors.topBarText.toCColor(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }


            Box(modifier = Modifier
                .fillMaxHeight(1f)
                .wrapContentWidth()
                .clickable {

                    saveCurrentSticker()

                }
                .padding(horizontal = 30.dp), contentAlignment = Alignment.Center) {

                BasicText(
                    text = stringResource(id = app.inspiry.projectutils.R.string.save),
                    overflow = TextOverflow.Ellipsis, maxLines = 1,
                    style = TextStyle(
                        color = colors.topBarText.toCColor(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),

                    )
            }
        }
    }

    private fun saveCurrentSticker() {
        val currentSticker = viewModel.getCurrentSticker()
        if (currentSticker != null) {

            val stickerMedia = currentSticker.media
            val currentStickerPath = currentSticker.path

            if (stickerMedia.forPremium && !licenseManger.hasPremiumState.value) {
                activityRedirector.openSubscribeActivity(this, "sticker")
            } else {

                if (stickerMedia is MediaVector && currentSticker.changeLoopStateBeforeSaving)
                    stickerMedia.isLoopEnabled = stickerMedia.isLoopEnabled != true

                val category = viewModel.currentCategory.value
                val forPremium = stickerMedia.forPremium
                val stickerName = currentStickerPath.getFileName().removeExt()

                analyticsManager.onStickerPicked(stickerName, category, forPremium)

                lifecycleScope.launch(Dispatchers.Main) {

                    val stickerJson = withContext(Dispatchers.IO) {
                        json.encodeToString(MediaSerializer, stickerMedia)
                    }

                    setResult(RESULT_OK, Intent().putExtra(EXTRA_STICKER_PATH, stickerJson))
                    finish()
                }
            }
        }
    }


    @Composable
    fun Tabs(tabIds: List<String>) {

        val currentCategory by viewModel.currentCategory.collectAsState()
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        LazyRow(
            modifier = Modifier
                .padding(top = 13.dp, bottom = 13.dp)
                .height(30.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            state = listState
        ) {


            itemsIndexed(tabIds, key = { index, item -> item }) { index, item ->

                val isSelected = item == currentCategory
                if (isSelected) listState.autoScroll(scope, index)
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        viewModel.load(item)
                    }
                    .background(if (isSelected) colors.tabBgActive.toCColor() else colors.tabBgInactive.toCColor())
                    .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center) {

                    BasicText(
                        text = StickerAndroidUtil.localizeTab(item, LocalContext.current),
                        overflow = TextOverflow.Ellipsis, maxLines = 1,
                        style = TextStyle(
                            color = if (isSelected) colors.tabTextActive.toCColor() else colors.tabTextInactive.toCColor(),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Light else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }

    @Composable
    fun Stickers() {
        val stickers by viewModel.currentStickers.collectAsState(Dispatchers.Main)

        if (stickers is InspResponseLoading) {

        } else if (stickers is InspResponseData) {

            val actualStickers = (stickers as InspResponseData).data

            // StickerComposeItems(actualStickers = actualStickers)
            StickerAndroidItems(actualStickers = actualStickers)
        } else if (stickers is InspResponseError) {

            logger.error((stickers as InspResponseError<*>).throwable)
        } else {
            throw IllegalStateException("got unexpected response $stickers")
        }
    }

    @Composable
    fun StickerAndroidItems(actualStickers: List<MediaWithPath>) {

        val currentStickerIndex by viewModel.currentStickerIndex.collectAsState()
        val hasPremium by licenseManger.hasPremiumState.collectAsState()

        AndroidView(
            factory = {
                val recycler = RecyclerView(it)

                val layoutManager = GridLayoutManager(it, 3)
                recycler.setHasFixedSize(true)
                recycler.layoutManager = layoutManager

                recycler
            }, modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 9.5f.dp)
        ) {

            if (it.adapter == null) {
                it.adapter = AnimationsAdapter(
                    actualStickers.map { it.media }, { viewModel.setCurrentStickerIndex(it) },
                    currentStickerIndex, hasPremium = hasPremium, unitsConverter = unitsConverter
                )
            } else {
                (it.adapter as AnimationsAdapter).changeAnimations(
                    actualStickers.map { it.media },
                    hasPremium,
                    currentStickerIndex
                )
            }

        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun StickerComposeItems(
        actualStickers: Pair<List<Media>, List<String>>
    ) {

        val currentStickerIndex by viewModel.currentStickerIndex.collectAsState()
        val hasPremium by licenseManger.hasPremiumState.collectAsState()

        LazyVerticalGrid(
            cells = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 9.5f.dp)
        ) {

            itemsIndexed(actualStickers.first) { index, item ->

                val isSelected = currentStickerIndex == index

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.5f.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) colors.stickerBgActive.toCColor() else colors.stickerBgInactive.toCColor())
                        .border(
                            1.dp,
                            if (isSelected) colors.stickerStrokeActive.toCColor() else colors.stickerStrokeInactive.toCColor(),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            viewModel.setCurrentStickerIndex(index)
                        },
                    contentAlignment = Alignment.TopEnd
                ) {

                    AndroidView(factory = {
                        val innerGroupZView =
                            BaseGroupZView(it, templateView = null, unitsConverter = unitsConverter)
                        val templateView = InspTemplateViewCreator
                            .createInspTemplateView(
                                innerGroupZView,
                                colors = InfoViewColorsLight(),
                                displayInfoText = false
                            )

                        val template = Template(medias = mutableListOf(item), timeForEdit = 0)

                        templateView.shouldHaveBackground = false
                        templateView.loadTemplate(template)
                        templateView.startPlaying(false, false)

                        innerGroupZView
                    }, modifier = Modifier.fillMaxSize()) {

                    }

                    if (!hasPremium && item.forPremium) {
                        BasicText(
                            text = "PRO", maxLines = 1,
                            style = TextStyle(
                                color = colors.proText.toCColor(),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 5.dp)
                                .border(
                                    0.4.dp,
                                    color = colors.proStroke.toCColor(),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(top = 5.dp, end = 6.dp)
                        )
                    }
                }
            }
        }
    }


    class AnimationsAdapter(
        var medias: List<Media>,
        val onClick: (Int) -> Unit,
        var selectedAnimationIndex: Int,
        var hasPremium: Boolean,
        val unitsConverter: BaseUnitsConverter

    ) :
        RecyclerView.Adapter<TemplateViewHolder>() {


        private val mHandler by lazy { Handler(Looper.getMainLooper()) }
        private val viewHolders = mutableSetOf<TemplateViewHolder>()

        @SuppressLint("NotifyDataSetChanged")
        fun changeAnimations(medias: List<Media>, hasPremium: Boolean, currentStickerIndex: Int) {

            var notifyAll = false
            if (this.medias != medias || this.hasPremium != hasPremium) {
                notifyAll = true
            }
            this.medias = medias
            this.hasPremium = hasPremium
            this.selectedAnimationIndex = currentStickerIndex

            if (notifyAll)
                notifyDataSetChanged()
            else {
                selectNewItem(currentStickerIndex)
            }
        }

        private fun selectNewItem(index: Int) {
            viewHolders.forEach {
                it.root.isActivated = it.adapterPosition == index
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {

            val frameLayout = FrameLayout(parent.context)
            frameLayout.layoutParams =
                ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100.dpToPxInt())
                    .apply {
                        setMargins(
                            5.dpToPxInt(),
                            5.dpToPxInt(),
                            5.dpToPxInt(),
                            5.dpToPxInt()
                        )
                    }

            val innerGroupZView =
                BaseGroupZView(parent.context, templateView = null, unitsConverter = unitsConverter)
            innerGroupZView.isDuplicateParentStateEnabled = true
            innerGroupZView.setBackgroundResource(R.drawable.grid_text_animations)

            val templateView = InspTemplateViewCreator.createInspTemplateView(
                innerGroupZView,
                colors = InfoViewColorsLight(),
                displayInfoText = false
            )
            templateView.shouldHaveBackground = false
            templateView.template =
                Template(medias = mutableListOf(), timeForEdit = 0, preferredDuration = 10000)

            frameLayout.addView(
                innerGroupZView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            val textPro = TextView(parent.context)
            textPro.setBackgroundResource(R.drawable.text_pro_background)
            textPro.text = "PRO"
            textPro.textSize = 9f
            textPro.gravity = Gravity.CENTER
            textPro.translationZ = 100f
            textPro.setTextColor(0xffbdbdbd.toInt())

            val textLp = FrameLayout.LayoutParams(26.dpToPxInt(), 14.dpToPxInt(), Gravity.END)
            textLp.setMargins(6.dpToPxInt())

            frameLayout.addView(textPro, textLp)

            return TemplateViewHolder(frameLayout, textPro, templateView, innerGroupZView)
        }

        override fun onViewRecycled(holder: TemplateViewHolder) {
            super.onViewRecycled(holder)
            holder.templateView.unbind(false)
            viewHolders.remove(holder)
        }

        override fun getItemCount() = medias.size

        fun previewAnimation(media: Media, templateView: InspTemplateView, delay: Long) {

            mHandler.removeCallbacksAndMessages(templateView)
            templateView.removeViews()

            mHandler.sendMessageDelayed(mHandler.getPostMessageCompat({
                templateView.template.medias.clear()
                templateView.template.medias.add(media)

                templateView.loadTemplate(templateView.template)
                templateView.startPlaying()
            }, templateView), delay)
        }

        override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {

            val media = medias[position]
            holder.root.isActivated = position == selectedAnimationIndex

            previewAnimation(
                media,
                holder.templateView,
                if (position % 2 == 0) 0 else (FRAME_IN_MILLIS * 10).toLong()
            )

            if (media.forPremium && !hasPremium) {
                holder.textPro.visibility = View.VISIBLE

            } else holder.textPro.visibility = View.GONE

            holder.templateAndroidView.setOnClickListener {
                onClick(position)
            }
            viewHolders.add(holder)
        }
    }

    companion object {
        const val EXTRA_STICKER_PATH = "sticker_path"
    }
}