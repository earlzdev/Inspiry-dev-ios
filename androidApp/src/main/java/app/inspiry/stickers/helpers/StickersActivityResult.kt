package app.inspiry.stickers.helpers

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import app.inspiry.stickers.ui.StickersActivity


class StickersActivityResult : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, request: Unit): Intent {

        return Intent(context, StickersActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {

        if (resultCode != AppCompatActivity.RESULT_OK || intent == null)
            return null


        return intent.getStringExtra(StickersActivity.EXTRA_STICKER_PATH)
    }
}