package app.inspiry

import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import app.inspiry.dialog.rating.RateUsDialog

@RunWith(AndroidJUnit4::class)
@SmallTest
class RateUsUiTest {

    @Test
    fun uiTest() {
        val scenario = launchFragment(
            null,
            R.style.RatingDialog,
            initialState = Lifecycle.State.INITIALIZED
        ) {
            RateUsDialog.create(
                popupString = getInstrumentation().targetContext.getString(app.inspiry.projectutils.R.string.rating_dialog_start_text)
            )
        }
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withText(app.inspiry.projectutils.R.string.rating_dialog_start_text))

        scenario.recreate()
    }
}