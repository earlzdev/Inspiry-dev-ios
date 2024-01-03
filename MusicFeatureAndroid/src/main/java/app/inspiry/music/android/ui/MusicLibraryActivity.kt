package app.inspiry.music.android.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.util.Size
import android.view.View
import android.view.Window
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.inspiry.MR
import app.inspiry.core.ActivityRedirector
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.*
import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.music.android.R
import app.inspiry.music.android.client.BaseAudioStatePlayer
import app.inspiry.music.android.client.ExoAudioStatePlayer
import app.inspiry.music.android.util.isUnknownArtist
import app.inspiry.music.android.util.localizedUnknownArtist
import app.inspiry.music.model.*
import app.inspiry.music.provider.ITunesMusicLibraryProvider
import app.inspiry.music.provider.LocalMusicLibraryProvider
import app.inspiry.music.provider.MusicLibraryProvider
import app.inspiry.music.provider.RemoteLibraryMusicProvider
import app.inspiry.music.ui.MusicColors
import app.inspiry.music.ui.MusicDarkColors
import app.inspiry.music.util.TrackUtils
import app.inspiry.music.viewmodel.BaseMusicViewModel
import app.inspiry.music.viewmodel.MusicDownloadingViewModel
import app.inspiry.subscribe.viewmodel.SUBSCRIBE_SOURCE_ROYALTY_FREE_MUSIC
import app.inspiry.utilities.toCColor
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.github.krottv.compose.sliders.DefaultTrack
import com.github.krottv.compose.sliders.SliderValueHorizontal
import dev.icerock.moko.permissions.*
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt

class MusicLibraryActivity : AppCompatActivity() {

    val analyticsManager: AnalyticsManager by inject()
    val player: BaseAudioStatePlayer by lazy {
        ExoAudioStatePlayer(this, get(), lifecycleScope)
    }

    val colors: MusicColors =
        MusicDarkColors

    private val loggerGetter: LoggerGetter by inject()
    private val imageLoader: ImageLoader by inject()
    private val remoteConfig: InspRemoteConfig by inject()
    private val errorHandler: ErrorHandler by inject()
    private val activityRedirector: ActivityRedirector by inject()
    private val licenseManager: LicenseManager by inject()

    private val iTunesMusicLibraryProvider: ITunesMusicLibraryProvider by inject()
    private val remoteLibraryProvider: RemoteLibraryMusicProvider by inject()
    private val localLibraryProvider: LocalMusicLibraryProvider by inject()
    private lateinit var downloadViewModel: MusicDownloadingViewModel
    private lateinit var permissionsController: PermissionsController

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialData: TemplateMusic? = intent.getParcelableExtra(EXTRA_INITIAL_STATE)
        val showItunesLibrary = remoteConfig.getBoolean("itunes_music")
        intent.removeExtra(EXTRA_INITIAL_STATE)

        permissionsController = PermissionsController(applicationContext = this.applicationContext)
        permissionsController.bind(lifecycle, supportFragmentManager)

        downloadViewModel =
            ViewModelProvider(
                this,
                MusicDownloadingViewModelFactory(
                    get(),
                    get(),
                    FileSystem.SYSTEM,
                    get(),
                    remoteConfig,
                    get(),
                    licenseManager
                )
            )[MusicDownloadingViewModel::class.java]

        setContent {
            MaterialTheme(colors = DarkColors) {

                SystemUi(windows = window)

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                    Main(initialData, showItunesLibrary)

                    val downloadFlow = remember(downloadViewModel.downloadingState, lifecycle) {
                        downloadViewModel.downloadingState.flowWithLifecycle(lifecycle)
                    }

                    val downloadingPr by downloadFlow.collectAsState(InspResponseNothing())

                    if (downloadingPr !is InspResponseNothing) {
                        if (downloadingPr is InspResponseError<TemplateMusic>) {
                            val error =
                                (downloadingPr as InspResponseError<TemplateMusic>).throwable
                            LaunchedEffect(error) {
                                error.printStackTrace()
                                errorHandler.toastError(error)
                            }

                            downloadViewModel.onPickMusicHandled()
                        } else if (downloadingPr is InspResponseData) {
                            LaunchedEffect((downloadingPr as InspResponseData<TemplateMusic>).data) {
                                onMusicPicked((downloadingPr as InspResponseData<TemplateMusic>).data)
                            }

                            downloadViewModel.onPickMusicHandled()

                        } else if (downloadingPr is InspResponseLoading) {
                            AnimatedVisibility(visible = true) {
                                ProgressDialog(
                                    progress = (downloadingPr as InspResponseLoading<TemplateMusic>).progress
                                        ?: 0f, colors
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private val DarkColors = darkColors(
        background = colors.background.toCColor(),
    )


    @Composable
    fun RowScope.TabText(
        text: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Box(modifier = Modifier
            .clickable { onClick() }
            .weight(1f),
            contentAlignment = Alignment.Center)
        {
            Text(
                text = text, maxLines = 1, fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) colors.tabTextActive.toCColor() else colors.tabTextInactive.toCColor(),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(vertical = 15.dp)
            )
        }
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
    fun Main(initialData: TemplateMusic? = null, showItunesLibrary: Boolean = true) {

        var initialData by rememberSaveable(init = { mutableStateOf(initialData) })

        var currentItem by rememberSaveable {

            var num: MusicTab? = null

            if (initialData != null && (initialData?.tab != MusicTab.ITUNES || showItunesLibrary)) {
                num = initialData?.tab
            }

            mutableStateOf(num ?: if (showItunesLibrary) MusicTab.ITUNES else MusicTab.LIBRARY)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {

            MusicTabs(
                currentItem = currentItem,
                showItunesLibrary = showItunesLibrary,
                onCurrentItemChange = { tabClick ->
                    if (tabClick == MusicTab.MY_MUSIC &&
                        !permissionsController.isPermissionGranted(Permission.WRITE_STORAGE)
                    ) {

                        lifecycleScope.launch {

                            try {
                                permissionsController.providePermission(Permission.WRITE_STORAGE)
                                currentItem = tabClick

                            } catch (deniedAlways: DeniedAlwaysException) {
                                // Permission is always denied.
                            } catch (denied: DeniedException) {
                                // Permission was denied.
                            } catch (e: RequestCanceledException) {

                            }
                        }

                    } else {
                        currentItem = tabClick
                    }
                })

            val initialDataForItem =
                if (currentItem == initialData?.tab) initialData else null

            initialData = null

            player.pause()

            when (currentItem) {
                MusicTab.ITUNES -> MusicContentPreview(initialData = initialDataForItem)
                MusicTab.LIBRARY -> MusicContentLibrary(initialData = initialDataForItem)
                MusicTab.MY_MUSIC -> MusicContentMyMusic(initialData = initialDataForItem)
            }
        }
    }

    class MusicViewModelFactory(
        private val initialData: TemplateMusic?,
        private val provider: MusicLibraryProvider, private val loggerGetter: LoggerGetter
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BaseMusicViewModel(
                initialData?.albumId ?: -1,
                true, provider, loggerGetter
            ) as T
        }
    }

    @Composable
    fun MusicContentPreview(initialData: TemplateMusic?) {
        val viewModel: BaseMusicViewModel =
            viewModel(
                factory = MusicViewModelFactory(
                    initialData,
                    iTunesMusicLibraryProvider, loggerGetter
                ), key = "PreviewViewModel"
            )

        TabContent(
            initialTitle = initialData?.title,
            initialArtist = initialData?.artist,
            viewModel = viewModel,
            hasSearchPanel = false
        ) {
            Text(
                text = stringResource(app.inspiry.projectutils.R.string.music_preview_warning),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp, start = 10.dp, bottom = 13.dp),
                color = colors.headerPreviewText.toCColor(), fontSize = 12.sp,
                fontWeight = FontWeight.Light, textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun MusicContentLibrary(initialData: TemplateMusic?) {
        val viewModel: BaseMusicViewModel =
            viewModel(
                factory = MusicViewModelFactory(
                    initialData,
                    remoteLibraryProvider, loggerGetter
                ), key = "ContentViewModel"
            )

        TabContent(
            initialTitle = initialData?.title,
            initialArtist = initialData?.artist,
            viewModel = viewModel,
            hasSearchPanel = false
        ) {

            val context: Context = LocalContext.current
            val hasLicense: Boolean by licenseManager.hasPremiumState.collectAsState()

            Text(
                text = MR.strings.music_royalty_free_description.desc().toString(context),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(bottom = 7.dp),
                color = colors.searchTextInactive.toCColor(), fontSize = 13.sp,
                fontWeight = FontWeight.Light, textAlign = TextAlign.Center
            )

            if (!hasLicense) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(IntrinsicSize.Max)
                        .padding(bottom = 14.dp)
                ) {

                    Text(
                        downloadViewModel.getTextRoyaltyFreeTracksLeft {
                            it.desc().toString(context)
                        },
                        modifier = Modifier
                            .padding(bottom = 3.dp),
                        color = colors.headerMusicCountText.toCColor(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .height(2.dp)
                            .background(
                                colors.headerProgressTrack.toCColor(),
                                RoundedCornerShape(2.dp)
                            )

                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = downloadViewModel.getRoyaltyFreeTracksLeftProgress())
                                .fillMaxHeight()
                                .background(
                                    colors.headerProgress.toCColor(),
                                    RoundedCornerShape(2.dp)
                                )
                        ) {}
                    }
                }
            }
        }
    }

    @Composable
    fun MusicContentMyMusic(initialData: TemplateMusic?) {
        val viewModel: BaseMusicViewModel =
            viewModel(
                factory = MusicViewModelFactory(
                    initialData,
                    localLibraryProvider, loggerGetter
                ), key = "LocalViewModel"
            )

        TabContent(
            initialTitle = initialData?.title,
            initialArtist = initialData?.artist,
            viewModel = viewModel,
            hasSearchPanel = true
        )
    }

    @Composable
    fun MusicTabs(
        currentItem: MusicTab,
        showItunesLibrary: Boolean,
        onCurrentItemChange: (MusicTab) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),

            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .height(50.dp)
                    .width(40.dp)
                    .clickable { onBackPressedDispatcher.onBackPressed() }
                    .padding(start = 10.dp),

                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(id = R.drawable.ic_music_back), contentDescription = null
                )
            }

            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .width(0.dp)
                    .weight(1f)
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (showItunesLibrary) {
                    TabText(
                        text = stringResource(app.inspiry.projectutils.R.string.music_tab_preview),
                        isSelected = currentItem == MusicTab.ITUNES,
                        onClick = { onCurrentItemChange(MusicTab.ITUNES) })
                }

                TabText(
                    text = stringResource(app.inspiry.projectutils.R.string.music_tab_library),
                    isSelected = currentItem == MusicTab.LIBRARY,
                    onClick = { onCurrentItemChange(MusicTab.LIBRARY) })


                TabText(
                    text = stringResource(app.inspiry.projectutils.R.string.music_tab_my),
                    isSelected = currentItem == MusicTab.MY_MUSIC,
                    onClick = { onCurrentItemChange(MusicTab.MY_MUSIC) })
            }

            Spacer(
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
            )
        }
    }

    private fun onMusicPicked(music: TemplateMusic?) {

        analyticsManager.onMusicPickedFromLibrary(music)
        setResult(RESULT_OK, Intent().putExtra("data", music as Parcelable))
        finish()
    }

    @Composable
    fun ContentImage(
        url: String?,
        width: Dp,
        height: Dp,
        cornerRadius: Dp,
        onModifier: Modifier.() -> Modifier,
        placeholder: @Composable () -> Unit
    ) {
        var bitmap by remember(key1 = url) {
            mutableStateOf<InspResponse<Bitmap>?>(
                null
            )
        }

        if (url != null && bitmap == null) {

            val size = Size(
                LocalDensity.current.run { width.toPx() }.roundToInt(),
                LocalDensity.current.run { height.toPx() }.roundToInt()
            )

            val formatedUrl = remember(key1 = size, key2 = url) {
                if (url.startsWith("https")) {

                    url
                        .replace("{w}", size.width.toString())
                        .replace("{h}", size.height.toString())
                } else {
                    url
                }
            }

            Log.i("tag", "MusicLibraryActivity $formatedUrl")

            val context = LocalContext.current
            LaunchedEffect(formatedUrl) {
                //bitmap = InspResponseLoading()

                val res = withContext(Dispatchers.IO) {
                    imageLoader.execute(
                        ImageRequest.Builder(context = context)
                            .size(size.width, size.height)
                            .scale(Scale.FILL)
                            .listener(onError = { req, error ->
                                error.throwable.printStackTrace()
                            })
                            .data(Uri.parse(formatedUrl))
                            .build()

                    )
                        .drawable
                }

                if (res == null || res !is BitmapDrawable) {
                    bitmap = InspResponseError(NullPointerException())
                } else {
                    bitmap = InspResponseData(res.bitmap)
                }
            }
        }


        if (bitmap is InspResponseData) {
            Image(
                bitmap = (bitmap as InspResponseData<Bitmap>).data.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .onModifier()
                    .size(width, height)
                    .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Crop
            )
        } else {
            placeholder()
        }
    }

    @Composable
    fun ContentProgress(modifier: Modifier) {
        Box(
            modifier = modifier, contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.searchEditCursor.toCColor())
        }
    }


    @Composable
    fun ColumnScope.AlbumsPanel(viewModel: BaseMusicViewModel) {

        val selectedAlbumId by viewModel.selectedAlbumIdState.collectAsState()

        val albums by viewModel.albumsState.collectAsState(
            context = lifecycleScope.coroutineContext,
            initial = InspResponseLoading()
        )
        val height = 105.dp

        if (albums is InspResponseLoading) {

            ContentProgress(
                Modifier
                    .fillMaxWidth()
                    .height(height)
            )

        } else if (albums is InspResponseError) {

            ContentErrorMusic(
                error = (albums as InspResponseError<*>).throwable,
                onClick = viewModel::retryAlbumsOnError
            )

        } else {

            val actualAlbums = (albums as InspResponseData<AlbumsResponse>).data

            ContentAlbums(actualAlbums.albums, height, selectedAlbumId) {
                viewModel.loadTrackOnClickAlbum(it)
            }
        }
    }

    @Composable
    fun ContentAlbums(
        actualAlbums: List<Album> = listOf(Album(1, "First"), Album(2, "Second")),
        height: Dp = 105.dp, selectedAlbumId: Long = -1,
        onSelectedAlbumIdChange: (Long) -> Unit = {}
    ) {

        val selectedAlbum = actualAlbums.indexOfFirst { it.id == selectedAlbumId }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
            state = rememberLazyListState(
                initialFirstVisibleItemIndex = kotlin.math.max(
                    0,
                    selectedAlbum
                )
            ),
            content = {

                itemsIndexed(actualAlbums) { index, item ->

                    val isSelected = selectedAlbumId == item.id

                    Column(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .width(87.dp)
                            .clickable {
                                onSelectedAlbumIdChange(item.id)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            Modifier
                                .height(71.dp)
                                .width(76.dp)
                                .background(
                                    if (isSelected) colors.albumBorderActive.toCColor()
                                    else colors.albumBorderInactive.toCColor(),
                                    RoundedCornerShape(12.dp)
                                ),

                            contentAlignment = Alignment.Center
                        ) {

                            ContentImage(
                                url = item.image,
                                70.dp,
                                65.dp,
                                10.dp, onModifier = { this }) { PlaceHolderAlbum() }
                        }


                        Text(
                            item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 6.dp, top = 5.dp, end = 6.dp),
                            maxLines = 1,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) colors.albumTextActive.toCColor() else
                                colors.albumTextInactive.toCColor(),
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            })
    }

    @Composable
    fun PlaceHolderAlbum() {
        Image(
            painterResource(id = R.drawable.ic_music_placeholder_album),
            contentDescription = null,
            modifier = Modifier
                .size(70.dp, 65.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    }


    @Composable
    fun SearchPanel(query: String, onQueryChange: (String) -> Unit) {
        val isBlank = query.isBlank()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 23.5f.dp, end = 27.5f.dp, top = 5.dp, bottom = 15.dp)
                .height(30.dp)
                .background(colors.searchBg.toCColor(), shape = RoundedCornerShape(10.dp)),

            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                painterResource(id = R.drawable.ic_music_search), contentDescription = "Search",

                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 11f.dp)
            )

            if (isBlank) {
                Text(
                    text = stringResource(app.inspiry.projectutils.R.string.music_search_hint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp, end = 10.dp),
                    color = colors.searchTextInactive.toCColor(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1
                )
            }

            BasicTextField(
                value = query, onValueChange = { onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 15.dp),
                cursorBrush = SolidColor(colors.searchEditCursor.toCColor()),
                textStyle = TextStyle(
                    textDecoration = TextDecoration.None,
                    color = colors.searchTextActive.toCColor(),
                    fontSize = 16.sp, fontWeight = FontWeight.Normal,
                ),
                singleLine = true,

                )
        }
    }

    @Composable
    fun ColumnScope.TracksPanel(
        initialTitle: String?, initialArtist: String?,
        viewModel: BaseMusicViewModel, hasSearchPanel: Boolean
    ) {

        val tracks by viewModel.tracksState.collectAsState(
            context = lifecycleScope.coroutineContext,
            initial = InspResponseLoading()
        )


        @Suppress("BlockingMethodInNonBlockingContext")
        if (tracks is InspResponseLoading) {
            ContentProgress(modifier = Modifier.fillMaxSize(1f))

        } else if (tracks is InspResponseError) {

            ContentErrorMusic(
                error = (tracks as InspResponseError<TracksResponse>).throwable,
                onClick = viewModel::retryTracksOnError
            )

        } else {
            val tracksData = (tracks as InspResponseData<TracksResponse>).data
            ContentTracks(
                actualTracks = tracksData.tracks, album = tracksData.album,
                initialTitle, initialArtist, hasSearchPanel, viewModel
            )
        }
    }

    @Composable
    fun ColumnScope.ContentErrorMusic(error: Throwable, onClick: () -> Unit) {

        ContentError(
            Modifier.fillMaxSize(fraction = 0.8f).align(CenterHorizontally),
            error = error,
            buttonTextColor = colors.albumTextInactive.toCColor(),
            buttonBgColor = colors.errorButtonBg.toCColor(),
            textInfoColor = colors.trackTextTitle.toCColor(),
            alwaysShowError = false,
            onClick = onClick
        )
    }

    @Composable
    fun ContentTracks(
        actualTracks: List<Track>, album: Album, initialTitle: String?, initialArtist: String?,
        hasSearchPanel: Boolean, viewModel: BaseMusicViewModel
    ) {

        val playingState by player.playingState.collectAsState(Dispatchers.Main)
        val searchQuery by viewModel.searchQueryState.collectAsState()

        val filteredTracks = TrackUtils.filterTracks(actualTracks, searchQuery)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState(
                initialFirstVisibleItemIndex = kotlin.math.max(
                    0,
                    TrackUtils.findSelectedTrackIndex(filteredTracks, initialTitle, initialArtist)
                )
            ),
            content = {


                if (hasSearchPanel) {
                    item {
                        SearchPanel(
                            query = searchQuery,
                            onQueryChange = { viewModel.searchQueryState.value = it })
                    }
                }

                itemsIndexed(filteredTracks) { index, item ->

                    val isSelected = playingState.url == item.url

                    val backgroundColor = if (isSelected) {
                        colors.trackSelectedBg.toCColor()
                    } else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(backgroundColor)
                            .clickable {
                                if (isSelected) {
                                    if (playingState.isPlaying) {
                                        player.pause()

                                    } else {
                                        player.play()
                                    }
                                } else {
                                    player.prepare(item.url, true)
                                }

                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        if (isSelected) {

                            SelectedTrackContent(
                                item = item,
                                album = album,
                                isPlaying = playingState.isPlaying,
                                isLoading = playingState.isLoading,
                                onModifier = { weight(1f) }, viewModel = viewModel
                            )

                        } else {

                            UnselectedTrackContent(item = item, onModifier = { weight(1f) })
                        }
                    }
                }
            })
    }

    @Composable
    fun SelectedTrackContent(
        item: Track,
        album: Album,
        isPlaying: Boolean,
        isLoading: Boolean,
        viewModel: BaseMusicViewModel,
        onModifier: Modifier.() -> Modifier
    ) {

        Box(
            modifier = Modifier
                .padding(start = 24.dp)
                .size(40.dp, 40.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {

            ContentImage(
                url = item.image,
                width = 40.dp,
                height = 40.dp,
                cornerRadius = 8.dp,
                onModifier = { this }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp, 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.trackPlayPauseBgActivePlaceholder.toCColor())
                ) {}
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.trackPlayPauseBgActive.toCColor()),
                contentAlignment = Alignment.Center
            ) {

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.searchEditCursor.toCColor(),
                        strokeWidth = 2.dp
                    )
                } else {
                    Image(
                        painterResource(
                            id = if (isPlaying) R.drawable.ic_music_pause_track else
                                R.drawable.ic_music_play_track
                        ),
                        contentDescription = null
                    )
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(0.dp)
                .onModifier()
                .padding(start = 10.dp, end = 0.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.Center
        ) {

            val seekBarPaddingHorizontal = 6.dp

            Row(
                Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = seekBarPaddingHorizontal),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    item.title, modifier = Modifier
                        .padding(end = 8.dp),
                    maxLines = 1, color = colors.trackTextTitle.toCColor(),
                    fontWeight = FontWeight.Medium, fontSize = 11.sp
                )

                if (!item.artist.isUnknownArtist()) {
                    Text(
                        item.artist, modifier = Modifier.fillMaxWidth(),
                        maxLines = 1, color = colors.trackTextSubtitle.toCColor(),
                        fontWeight = FontWeight.Normal, fontSize = 9.sp
                    )
                }
            }


            val duration by player.durationState.collectAsState(initial = 0L, Dispatchers.Main)
            val currentTime by player.currentTimeState.collectAsState(
                initial = 0L,
                Dispatchers.Main
            )

            val progress =
                if (duration == 0L) 0f else currentTime / duration.toFloat()
            var sliderOwnProgress by remember { mutableStateOf(-1f) }

            val coroutineScope = rememberCoroutineScope { Dispatchers.Main }
            SliderValueHorizontal(
                value = if (sliderOwnProgress == -1f) progress else sliderOwnProgress,
                onValueChange = {
                    player.cancelCurrentTimeJob()
                    sliderOwnProgress = it
                },
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .fillMaxWidth()
                    .height(20.dp),
                thumbHeightMax = true,
                track = { p1, p2, p3, p4, p5 ->
                    DefaultTrack(
                        p1, p2, p3, p4, p5,
                        height = 2.dp,
                        colorTrack = Color(0xffc4c4c4),
                        colorProgress = Color(0xffD552EB)
                    )
                },
                thumb = { modifier, p2, interactionSource, p4, p5 ->

                    SliderThumb2Layers(
                        modifier,
                        DpSize(8.dp, 8.dp),
                        Color(0x99D552EB),
                        Color(0xffC4C4C4),
                        interactionSource,
                        1.5f
                    )
                },
                thumbSizeInDp = DpSize(14.dp, 14.dp),
                onValueChangeFinished = {
                    player.seekTo((sliderOwnProgress * duration).toLong())

                    // because player.currentTimeState should be updated before it to prevent glitches.
                    coroutineScope.launch {
                        delay(1L)
                        sliderOwnProgress = -1f
                    }
                }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = seekBarPaddingHorizontal)
            ) {

                val textTime = if (sliderOwnProgress == -1f) currentTime
                else (sliderOwnProgress * duration).toLong()

                Text(
                    TrackUtils.convertTimeToString(textTime), modifier = Modifier.weight(1f),
                    maxLines = 1, color = colors.trackTextSubtitle.toCColor(),
                    fontWeight = FontWeight.Light, fontSize = 10.sp
                )

                Text(
                    TrackUtils.convertTimeToString(duration),
                    maxLines = 1, color = colors.trackTextSubtitle.toCColor(),
                    fontWeight = FontWeight.Light, fontSize = 10.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(end = 20.dp)
                .size(35.dp)
                .clickable {
                    if (downloadViewModel.shouldOpenSubscribeOnPickMusic(
                            viewModel.provider.tab,
                            licenseManager.hasPremiumState.value
                        )
                    ) {
                        activityRedirector.openSubscribeActivity(
                            this@MusicLibraryActivity,
                            SUBSCRIBE_SOURCE_ROYALTY_FREE_MUSIC
                        )
                    } else {
                        downloadViewModel.pickMusic(
                            item,
                            album,
                            player.getDurationMillis(),
                            viewModel.provider.tab
                        )
                    }

                }, contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colors.trackAddBg.toCColor(), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_music_add),
                    contentDescription = "Pick music"
                )
            }
        }
    }

    @Composable
    fun UnselectedTrackContent(item: Track, onModifier: Modifier.() -> Modifier) {

        ContentImage(
            url = item.image,
            width = 40.dp,
            height = 40.dp,
            cornerRadius = 8.dp,
            onModifier = { padding(start = 24.dp) }
        ) { PlaceholderTrack(res = R.drawable.ic_music_placeholder_track) }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(0.dp)
                .onModifier()
                .padding(start = 16.dp, end = 5.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                item.title, modifier = Modifier.fillMaxWidth(),
                maxLines = 1, color = colors.trackTextTitle.toCColor(),
                fontWeight = FontWeight.Medium, fontSize = 14.sp
            )

            Text(
                item.artist.localizedUnknownArtist(LocalContext.current),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1, color = colors.trackTextSubtitle.toCColor(),
                fontWeight = FontWeight.Normal, fontSize = 10.sp
            )
        }
    }

    @Composable
    fun PlaceholderTrack(res: Int) {
        Box(
            modifier = Modifier
                .padding(start = 24.dp)
                .size(40.dp, 40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.trackPlayPauseBgInactive.toCColor()),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painterResource(id = res),
                contentDescription = null
            )
        }

    }

    @Composable
    fun TabContent(
        initialTitle: String?,
        initialArtist: String?,
        viewModel: BaseMusicViewModel,
        hasSearchPanel: Boolean,
        Header: @Composable() ColumnScope.() -> Unit = { Spacer(modifier = Modifier.height(15.dp)) }
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            Header()
            AlbumsPanel(viewModel)
            TracksPanel(initialTitle, initialArtist, viewModel, hasSearchPanel)
        }
    }

    companion object {
        const val EXTRA_INITIAL_STATE = "initial_state"

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentError(
    modifier: Modifier,
    error: Throwable,
    buttonTextColor: Color,
    buttonBgColor: Color,
    textInfoColor: Color,
    alwaysShowError: Boolean,
    onClick: () -> Unit
) {
    error.printStackTrace()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        val errorMessage =
            stringResource(app.inspiry.projectutils.R.string.error_general_check_internet)
        var showDetailedError by remember {
            mutableStateOf(alwaysShowError)
        }

        BasicText(
            text = errorMessage,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp),
            style = TextStyle(
                color = textInfoColor, fontSize = 13.sp,
                textAlign = TextAlign.Center
            ),
        )

        BasicText(
            text = stringResource(app.inspiry.projectutils.R.string.music_error_button),
            Modifier
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(buttonBgColor)
                .combinedClickable(onClick = onClick, onLongClick = {
                    showDetailedError = true
                })
                .padding(horizontal = 18.dp, vertical = 7.dp), maxLines = 1,
            style = TextStyle(color = buttonTextColor, fontSize = 14.sp)
        )

        if (!error.message.isNullOrEmpty() && showDetailedError) {
            BasicText(
                text = error.message!!,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                style = TextStyle(
                    color = textInfoColor.copy(alpha = textInfoColor.alpha * 0.65f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            )
        }

    }
}

@Preview
@Composable
private fun ProgressDialog(progress: Float = 0f, colors: MusicColors = MusicDarkColors) {
    Dialog(
        onDismissRequest = {}, properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false, securePolicy = SecureFlagPolicy.SecureOff
        )
    ) {

        Column(
            modifier = Modifier
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(5.dp))
                .background(
                    colors.downloadDialogBg.toCColor(),
                    shape = RoundedCornerShape(5.dp)
                )
                .padding(horizontal = 25.dp, vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                stringResource(app.inspiry.projectutils.R.string.music_progress_downloading_message),
                color = colors.albumTextActive.toCColor(), fontSize = 14.sp, modifier = Modifier
                    .padding(bottom = 12.dp, start = 10.dp, end = 10.dp)
            )

            if (progress == 0f) {
                CircularProgressIndicator(color = colors.searchEditCursor.toCColor())
            } else {
                CircularProgressIndicator(
                    color = colors.searchEditCursor.toCColor(),
                    progress = progress
                )
            }
        }
    }
}