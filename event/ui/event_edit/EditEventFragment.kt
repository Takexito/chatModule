package com.dev.podo.event.ui.event_edit

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.dev.podo.R
import com.dev.podo.common.model.entities.Media
import com.dev.podo.common.ui.ProgressBarHelper
import com.dev.podo.common.utils.DateFormatter
import com.dev.podo.common.utils.visibility
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.core.ui.MainActivity
import com.dev.podo.core.ui.adapter.BaseAdapter
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.FragmentEditEventBinding
import com.dev.podo.databinding.PageDetailedEventBinding
import com.dev.podo.event.viewmodel.EventEditViewModel
import com.dev.podo.home.model.entities.HomeEventModel
import com.dev.podo.home.model.entities.Tag
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditEventFragment : BaseFragment(R.layout.fragment_edit_event) {

    private val viewModel: EventEditViewModel by viewModels()
    private val viewBinding: FragmentEditEventBinding by viewBinding()
    private val navArgs: EditEventFragmentArgs by navArgs()
    private var eventArchived = false

    private var recyclerBinding: ViewBindingGetter<PageDetailedEventBinding> = { parent ->
        PageDetailedEventBinding.inflate(LayoutInflater.from(requireContext()), parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleClickListeners()
        configureObservers()
        handleBackPress()
        viewModel.fetchDetailedEvent(navArgs.eventId, navArgs.isTrashed)
    }

    private fun configureObservers() {
        viewModel.event.observe(viewLifecycleOwner) { state ->
            ProgressBarHelper.showOnLoading(viewBinding.loader.root, state)
            when (state) {
                is ResultState.Error -> {
                    changeButtonsClickable()
                    state.exception.message?.let { showErrorAlert(it) }
                }
                ResultState.InProgress -> {
                    changeButtonsClickable(true)
                }
                is ResultState.Success -> {
                    changeButtonsClickable()
                    initView(state.data)
                }
            }
        }
        viewModel.archiveEventResult.observe(viewLifecycleOwner) { state ->
            ProgressBarHelper.showOnLoading(viewBinding.loader.root, state)
            when (state) {
                is ResultState.Error -> {
                    changeButtonsClickable()
                    state.exception.message?.let { showErrorAlert(it) }
                }
                is ResultState.InProgress -> {
                    changeButtonsClickable(true)
                }
                is ResultState.Success -> {
                    changeButtonsClickable()
                    showArchivedLayout()
                    eventArchived = true
                }
            }
        }
    }

    private fun changeButtonsClickable(isLoading: Boolean = false) {
        viewBinding.eventManageArchiveButton.isClickable = !isLoading
    }

    private fun showArchivedLayout() {
        viewBinding.eventManageArchiveButton.visibility = View.GONE
        viewBinding.eventArchivedLayout.visibility = View.VISIBLE
    }

    private fun handleClickListeners() {
        viewBinding.apply {
            eventManageArchiveButton.setOnClickListener {
                viewModel.archiveEvent()
            }
            eventManageOptionsButton.setOnClickListener {
                // TODO::implement logic after designer response
            }
        }
    }

    private fun initView(model: HomeEventModel) {
        viewBinding.apply {
            bindUserInfoView(model)
            bindImage(model.user?.media ?: listOf())
            bindSimpleViews(model)
            model.tags.forEach { tag ->
                Log.d("LOGGING", "TAG: $tag")
                bindTag(tag)
            }
            model.deletedAt?.let {
                if (it.isEmpty()) { return@let }
                showArchivedLayout()
            }
        }
    }

    private fun bindTag(
        tag: Tag
    ) {
        val tagView = MaterialTextView(requireContext())
        val textStyle =
            if (tag.groupName == null) R.style.AppText_Button else R.style.AppText_Subtitle1_Semibold

        tagView.apply { initTagView(tag, textStyle) }
        viewBinding.eventManageTagLayout.addView(tagView)
    }

    private fun MaterialTextView.initTagView(
        tag: Tag,
        textStyle: Int
    ) {
        setPadding(
            0,
            0,
            context.resources.getDimensionPixelSize(R.dimen.small_content_margin),
            context.resources.getDimensionPixelSize(R.dimen.small_medium_content_margin)
        )

        text = tag.title

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(textStyle)
            setTextColor(
                context.resources.getColor(
                    R.color.dark_green,
                    context.theme
                )
            )
        } else {
            setTextAppearance(context, textStyle)
            setTextColor(context.resources.getColor(R.color.dark_green))
        }
    }

    private fun FragmentEditEventBinding.bindSimpleViews(model: HomeEventModel) {
        val startAtDate =
            DateFormatter.getStringByDate(model.startAt, HomeEventModel.startAtDateFormat)
        eventManageDate.text = startAtDate
        eventManageTitle.text = model.title
        eventManagePlace.text = model.place
        eventManageAboutMe.text = model.user?.description
        eventManageDescription.text = model.description

        val isPlaceEmpty = model.place.isEmpty()
        val isAboutMeEmpty = model.user?.description.isNullOrEmpty()
        eventManagePlace.visibility(!isPlaceEmpty)
        eventManagePlaceLabel.visibility(!isPlaceEmpty)
        eventManageAboutMe.visibility(!isAboutMeEmpty)
        eventManageAboutMeLabel.visibility(!isAboutMeEmpty)
    }

    private fun FragmentEditEventBinding.bindUserInfoView(model: HomeEventModel) {
        eventManageUserInfoView.apply {
            user = model.user
            city = model.city
        }
    }

    private fun FragmentEditEventBinding.bindImage(media: List<Media>) {
        val adapter: BaseAdapter<PageDetailedEventBinding, Media?> =
            object : BaseAdapter<PageDetailedEventBinding, Media?>({}, recyclerBinding) {
            override fun bind(viewBinding: PageDetailedEventBinding, position: Int) {
                data[position]?.url?.thumb?.let { mediaSrc ->
                    Glide
                        .with(requireContext())
                        .load(mediaSrc)
                        .centerCrop()
                        .placeholder(R.drawable.podo_bench_image_short)
                        .into(viewBinding.eventDetailedPageImage)
                } ?: run {
                    viewBinding.eventDetailedPageImage.setImageResource(R.drawable.podo_bench_image_short)
                }
            }
        }
        eventManageImagePager.adapter = adapter
        TabLayoutMediator(eventManageImagePagerIndicator, eventManageImagePager) { _, _ ->
        }.attach()
        eventManageImagePager.offscreenPageLimit = 2
        adapter.data = if (media.isNullOrEmpty()) listOf(null) else media
    }

    override fun onResume() {
        super.onResume()
        requireActivity().invalidateOptionsMenu()
    }

    private fun handleBackPress() {
        (requireActivity() as MainActivity).onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (eventArchived) {
                val action = EditEventFragmentDirections.actionEditEventToMangeEvents()
                findNavController().navigate(action)
                return@addCallback
            }
            findNavController().navigateUp()
        }
    }
}
