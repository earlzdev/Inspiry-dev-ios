package app.inspiry.edit.socialIconsSelector

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.R
import app.inspiry.core.util.parseAssetsPath
import app.inspiry.utils.dpToPxInt
import com.airbnb.lottie.LottieAnimationView


class SocialIconsAdapter(
    var stickers: List<String>,
    val activity: AppCompatActivity,
    val onClickListener: (String) -> Unit,
    val onPickImageClickListener: (() -> Unit),
    val onDisableIconClickListener: (() -> Unit),

) : RecyclerView.Adapter<LottieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LottieViewHolder {

        val lottie = LottieAnimationView(parent.context)
        //layout params for each icon
        lottie.layoutParams = ViewGroup.MarginLayoutParams(40.dpToPxInt(), 40.dpToPxInt()).apply {
            setMargins(3.dpToPxInt(), 6.dpToPxInt(), 5.dpToPxInt(), 3.dpToPxInt())
        }

        return LottieViewHolder(lottie)
    }

    override fun onBindViewHolder(holder: LottieViewHolder, position: Int) {


        when (position) {
            //"+" drawable at the first position
            0 -> holder.lottie.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this.context,
                        R.drawable.ic_palette_from_gallery
                    )
                )
                setPadding(3.dpToPxInt(), 3.dpToPxInt(), 3.dpToPxInt(), 3.dpToPxInt())
                setOnClickListener {
                    onPickImageClickListener.invoke()
                }
            }
            //"disable" drawable at the second position
            1 -> holder.lottie.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this.context,
                        R.drawable.ic_remove_color
                    )
                )
                setPadding(3.dpToPxInt(), 3.dpToPxInt(), 3.dpToPxInt(), 3.dpToPxInt())
                setOnClickListener {
                    onDisableIconClickListener.invoke()
                }
            }
            else -> {
                val path = stickers[position - 2]

                holder.lottie.apply {
                    setIgnoreDisabledSystemAnimations(true)
                    setCacheComposition(false)
                    setAnimation(path.parseAssetsPath())
                    progress = 1f
                    setOnClickListener {
                        onClickListener(path)
                    }
                }
            }
        }
    }

    override fun getItemCount() = stickers.size + 2

}

class LottieViewHolder(val lottie: LottieAnimationView) : RecyclerView.ViewHolder(lottie)