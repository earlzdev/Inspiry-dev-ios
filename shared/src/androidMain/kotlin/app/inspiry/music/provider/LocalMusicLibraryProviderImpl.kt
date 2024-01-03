package app.inspiry.music.provider

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import app.inspiry.MR
import app.inspiry.music.model.Album
import app.inspiry.music.model.AlbumsResponse
import app.inspiry.music.model.Track
import app.inspiry.music.model.TracksResponse
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class LocalMusicLibraryProviderImpl : LocalMusicLibraryProvider, KoinComponent {
    val context: Context by inject()

    private val albumsFields = if (Build.VERSION.SDK_INT >= 29) {
        arrayOf(
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums._ID
        )
    } else {
        arrayOf(
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART
        )
    }
    private val albumsSortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"
    private val tracksSortOrder = Media.TITLE + " ASC"

    private val allTracksAlbum: Album
        get() = Album(
            -1,
            StringDesc.Resource(MR.strings.music_album_all_tracks).toString(context),
            null,
            0
        )

    private val contentUriMedia: Uri by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }
    private val contentUriAlbums: Uri by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }
    }

    private val trackFields = arrayOf(
        Media._ID,
        Media.TITLE, Media.ARTIST, Media.ALBUM_ID
    )

    private fun getAlbumsQuery(projection: Array<String> = albumsFields): Cursor? {
        return context.contentResolver.query(
            contentUriAlbums, projection,
            null, null, albumsSortOrder
        )
    }

    override suspend fun getAlbums(): AlbumsResponse {

        val allAlbums = getAlbumsQuery().parseAlbums()

        allAlbums.add(0, allTracksAlbum)

        return AlbumsResponse(allAlbums)
    }

    private fun Cursor?.parseAlbums(): MutableList<Album> {
        return parseCursor { cursor ->
            val title: String = cursor.getString(0) ?: MediaStore.UNKNOWN_STRING
            val artist: String = cursor.getString(1) ?: MediaStore.UNKNOWN_STRING
            val numberOfSongs: Int = cursor.getInt(2)
            val id = cursor.getLong(3)

            val imageUrl = if (Build.VERSION.SDK_INT >= 29) {
                ContentUris.withAppendedId(contentUriAlbums, id)
                    .toString()
            } else {
                cursor.getString(4)
            }
            Album(
                id, title, artist, numberOfSongs, imageUrl
            )
        }
    }

    private fun Cursor?.parseTracks(): MutableList<Track> {
        return parseCursor { cursor ->

            val id = cursor.getLong(0)
            val title: String = cursor.getString(1)
            val artist: String = cursor.getString(2)
            val albumId: Long = cursor.getLong(3)

            val coverArt = if (Build.VERSION.SDK_INT >= 29) ContentUris.withAppendedId(
                contentUriAlbums,
                albumId
            )
                .toString()
            else
                ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
                    .toString()

            Track(
                ContentUris.withAppendedId(contentUriMedia, id).toString(),
                title, artist, coverArt

            )
        }
    }

    private fun <T> Cursor?.parseCursor(parse: (Cursor) -> T): MutableList<T> {
        val list = mutableListOf<T>()
        this?.use {
            if (moveToFirst()) {

                do {
                    val item = parse(this)
                    list.add(item)
                } while (moveToNext())
            }
        }
        return list
    }

    private fun ContentResolver.queryWithLimit(
        uri: Uri, projection: Array<String>, selection: String? = null,
        selectionArgs: Array<String>? = null, sortOrder: String? = null, limit: Int = 1
    ): Cursor? {
        return if (Build.VERSION.SDK_INT >= 26) {
            val bundleLimit = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            }
            query(uri, projection, bundleLimit, null)
        } else {
            val sort = if (sortOrder == null) "limit $limit" else " $sortOrder limit $limit"
            query(uri, projection, selection, selectionArgs, sort)
        }
    }

    private fun findAlbum(albumId: Long): Album? {
        val queryAlbum = context.contentResolver.queryWithLimit(
            contentUriAlbums,
            albumsFields,
            "${MediaStore.Audio.Albums._ID} = ?",
            arrayOf(albumId.toString()),
            albumsSortOrder, 1
        )

        val parsed = queryAlbum.parseAlbums()

        return parsed.getOrNull(0)
    }


    @SuppressLint("Recycle")
    private fun allTracksResponse(): TracksResponse {
        val tracks = context.contentResolver.query(
            contentUriMedia,
            trackFields,
            "${Media.IS_MUSIC} != ?",
            arrayOf("0"), tracksSortOrder
        ).parseTracks()

        val album = allTracksAlbum

        return TracksResponse(album, tracks)
    }

    @SuppressLint("Recycle")
    override suspend fun getTracks(albumId: Long): TracksResponse {

        if (albumId == -1L)
            return allTracksResponse()

        val album = findAlbum(albumId)

        val tracks: List<Track>
        if (album != null) {

            tracks = context.contentResolver.query(
                contentUriMedia,
                trackFields,
                "${Media.IS_MUSIC} != ? AND ${Media.ALBUM_ID} = ?",
                arrayOf("0", album.id.toString()), Media.TITLE + " ASC"
            ).parseTracks()

        } else {
            return TracksResponse(Album(albumId, "unknown album"), emptyList())
        }

        return TracksResponse(album, tracks)
    }
}