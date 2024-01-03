package app.inspiry.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.inspiry.R

abstract class BaseThemeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        //ViewCompat.getWindowInsetsController(window.decorView)?.isAppearanceLightStatusBars = true
    }
}