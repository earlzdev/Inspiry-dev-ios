package app.inspiry.bfpromo.ui

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.inspiry.MR
import app.inspiry.R
import app.inspiry.font.helpers.getCurrentLocale
import app.inspiry.utilities.toCColor
import app.inspiry.utils.capitalized
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import java.util.*


private fun Long.toLocalizedText(locale: Locale): String {

    val date = SimpleDateFormat("d MMMM", locale)
    val res: String = date.format(Date(this))

    return res.capitalized(locale)
}


@Composable
@Preview(heightDp = 50)
fun BFPromoBannerUI(
    worksUntilUnixDate: Long = (DateTime.now() + 2.0.days).unixMillisLong,
    colors: BFPromoColors = BFPromoColorsDark(),
    onClickRemoveBanner: () -> Unit = {},
    onClickBanner: () -> Unit = {}
) {
    MaterialTheme(colors = MaterialTheme.colors.copy(isLight = false)) {

        Row(
            modifier = Modifier.fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClickBanner)
                .background(
                    Color.Black.copy(alpha = 0.8f)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val context = LocalContext.current

            Image(
                painterResource(R.drawable.ic_bottom_banner_close),
                contentDescription = "close banner",
                contentScale = ContentScale.Inside,
                colorFilter = ColorFilter.tint(Color(0xff939393)),
                modifier = Modifier
                    .graphicsLayer(scaleY = 0.75f, scaleX = 0.75f)
                    .fillMaxHeight()
                    .width(45.dp)
                    .clickable(onClick = onClickRemoveBanner)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                val boldMont = FontFamily(MR.fonts.mont.bold.getTypeface(context)!!)

                val stringTitl1 =
                    stringResource(app.inspiry.projectutils.R.string.category_black_friday)
                val stringTitle2 =
                    stringResource(app.inspiry.projectutils.R.string.bf_banner_title_second)

                val string = buildAnnotatedString {
                    append(
                        AnnotatedString(
                            stringTitl1,
                            SpanStyle(color = colors.bannerTitleText.toCColor())
                        )
                    )
                    append(AnnotatedString(" - $stringTitle2", SpanStyle(color = Color.White)))
                }


                var textSize by remember { mutableStateOf(17f) }
                var readyToDraw by remember { mutableStateOf(false) }

                Text(string,
                    modifier = Modifier
                        .drawWithContent {
                            if (readyToDraw) drawContent()
                        },
                    fontFamily = boldMont,
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize.sp,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.didOverflowWidth) {
                            textSize *= 0.9f
                        } else {
                            readyToDraw = true
                        }
                    }
                )

                Text(
                    "${stringResource(app.inspiry.projectutils.R.string.bf_banner_until)} ${
                        worksUntilUnixDate.toLocalizedText(
                            context.getCurrentLocale()
                        )
                    }",
                    modifier = Modifier,
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(35.dp))
        }
    }
}