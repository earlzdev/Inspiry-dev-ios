package app.inspiry.video.player.creator

import android.graphics.SurfaceTexture
import android.view.Surface
import app.inspiry.core.opengl.VideoPlayerParams

class VideoPlayerCreator(
    sourceUri: String,
    surfaceTexture: SurfaceTexture,
    surface: Surface,
    playerParams: VideoPlayerParams
) : BasePlayerCreator<VideoPlayerParams>(sourceUri, surfaceTexture, surface, playerParams)