package app.inspiry.stories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.inspiry.activities.MainActivity
import app.inspiry.core.data.TemplatePath
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.manager.InstagramSubscribeHolder
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.databinding.FragmentTemplatesBinding
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

abstract class AbsStoriesFragment : Fragment() {
    var adapterGlobal: RecyclerView.Adapter<*>? = null
    var adapter: TemplatesAdapter? = null

    val licenseManager: LicenseManager by inject()
    val instagramSubscribeHolder: InstagramSubscribeHolder by inject()
    val settings: Settings by inject()
    val json: Json by inject()
    val templateSaver: TemplateReadWrite by inject()
    val loggerGetter: LoggerGetter by inject()

    abstract val myStories: Boolean
    abstract fun loadData()

    val mainView: CoordinatorLayout
        get() = (activity as MainActivity).binding.main

    lateinit var binding: FragmentTemplatesBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTemplatesBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager =
            GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)


        return binding.root
    }

    open fun notifyAdapter(it: MutableList<TemplatePath>) {
        if (adapter == null) {
            val (adapterTemplates, adapterGlobal) = createNewAdapter(it)

            (binding.recyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object :
                GridLayoutManager.SpanSizeLookup() {

                override fun getSpanSize(position: Int): Int {

                    return if (myStories && adapterGlobal.itemCount == 1)
                        2
                    else if (adapterGlobal.getItemViewType(position) == TemplatesAdapter.ITEM_TYPE_CATEGORY)
                        2
                    else 1
                }
            }

            adapter = adapterTemplates
            binding.recyclerView.adapter = adapterGlobal
        } else {
            adapter?.updateTemplates(it)
            adapter?.notifyDataSetChanged()
        }
    }

    abstract fun createNewAdapter(it: MutableList<TemplatePath>): Pair<TemplatesAdapter, RecyclerView.Adapter<*>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            instagramSubscribeHolder.subscribed.collectLatest {
                adapter?.setInstSubscribeChanged(it)
            }
        }

        if (adapterGlobal != null)
            binding.recyclerView.adapter = adapterGlobal
        else {
            loadData()
        }


    }
}