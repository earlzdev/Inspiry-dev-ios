package app.inspiry.video.player.creator

import android.graphics.SurfaceTexture
import android.view.Surface
import app.inspiry.core.opengl.PlayerParams

abstract class BasePlayerCreator<Params: PlayerParams>(
    val sourceUri: String,
    val surfaceTexture: SurfaceTexture,
    val surface: Surface,
    var playerParams: Params
)