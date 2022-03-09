package com.dev.podo.event.ui.create_event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dev.podo.R
import com.dev.podo.common.adapters.choice.ChoiceSelectableAdapter
import com.dev.podo.common.adapters.selectable.ItemClickListener
import com.dev.podo.common.adapters.tag.TagChooseListener
import com.dev.podo.common.model.entities.Choice
import com.dev.podo.common.model.entities.Section
import com.dev.podo.common.model.entities.tag.TagGroup
import com.dev.podo.common.ui.CustomImageDialogBuilder
import com.dev.podo.common.ui.ProgressBarHelper
import com.dev.podo.common.ui.RecyclerAdapterHelper
import com.dev.podo.common.ui.bottomSheetChoose.TagChooseBottomSheet
import com.dev.podo.common.utils.ModalViewHelper
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.CreateDelayedEventFragmentBinding
import com.dev.podo.databinding.RecyclerViewChipItemSelectedBinding
import com.dev.podo.event.ui.adapter.SelectedTagsAdapter
import com.dev.podo.event.viewmodel.CreateDelayedEventViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.FormatWatcher
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

@AndroidEntryPoint
class CreateDelayedEventFragment : BaseFragment(R.layout.create_delayed_event_fragment) {

    private val viewModel: CreateDelayedEventViewModel by viewModels()
    private val binding: CreateDelayedEventFragmentBinding by viewBinding()
    private var recyclerTagsItemBinding: ViewBindingGetter<RecyclerViewChipItemSelectedBinding> =
        { parent ->
            RecyclerViewChipItemSelectedBinding.inflate(
                LayoutInflater.from(requireContext()),
                parent,
                false
            )
        }
    private lateinit var tagChooseListener: TagChooseListener
    private lateinit var tagSelectedClickListener: OnRecyclerItemClick
    private lateinit var sexChooseListener: ItemClickListener
    private val tagsSelectedAdapter by lazy {
        SelectedTagsAdapter(tagSelectedClickListener, recyclerTagsItemBinding)
    }
    private val sexChooseAdapter by lazy {
        ChoiceSelectableAdapter(sexChooseListener)
    }
    private lateinit var tagChooserBottomSheet: TagChooseBottomSheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleClickListeners()
        configureInputWatchers()
        configureSexChooseRecycler()
        configureSelectedTagsRecycler()
        configureTagBottomSheet()
        configureObservers()
    }

    private fun configureObservers() {
        viewModel.tagGroupsResponse.observe(viewLifecycleOwner, {
            when (it) {
                is ResultState.Error -> showErrorAlert(it.exception)
                is ResultState.InProgress -> {}
                is ResultState.Success -> {
                    val tagSections = TagGroup.convertToSections(it.data)
                    viewModel.updateTags(tagSections.toCollection(arrayListOf()))
                    tagChooserBottomSheet.submitList(tagSections)
                }
            }
        })
        viewModel.tagSections.observe(viewLifecycleOwner, {
            updateSelectedTags(it)
        })
        viewModel.eventResponse.observe(viewLifecycleOwner, {
            parentFragment?.parentFragment?.view?.findViewById<CardView>(R.id.loader)?.let { loader ->
                ProgressBarHelper.showOnLoading(loader, it)
            }
            disableClicks(it is ResultState.InProgress)
            when (it) {
                is ResultState.Error -> showErrorAlert(it.exception)
                is ResultState.InProgress -> {
                }
                is ResultState.Success -> showEventCreatedModal()
            }
        })
    }

    private fun showEventCreatedModal() {
        disableClicks(true)
        val dialogFragment = CustomImageDialogBuilder()
            .title(resources.getString(R.string.event_created))
            .message(resources.getString(R.string.now_wait_for_responses))
            .imageResource(R.drawable.podo_smile_image)
            .actionButtonTitle(resources.getString(R.string.continueString))
            .onAction {
                val mainNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
                val action = CreateEventFragmentDirections.actionCreateEventToEventManage()
                mainNavController.navigate(action)
            }
            .build()
        dialogFragment.isCancelable = false
        dialogFragment.show(childFragmentManager, "EventResponseDialog")
    }

    private fun disableClicks(isLoading: Boolean = false) {
        binding.publishButton.isClickable = !isLoading
    }

    private fun configureInputWatchers() {
        val dateSlots = UnderscoreDigitSlotsParser().parseSlots(
            requireContext().getString(R.string.date_mask_underline)
        )
        val timeSlots = UnderscoreDigitSlotsParser().parseSlots(
            requireContext().getString(R.string.time_mask_underline)
        )
        val formatDateWatcher: FormatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(dateSlots)
        )
        val formatTimeWatcher: FormatWatcher = MaskFormatWatcher(
            MaskImpl.createTerminated(timeSlots)
        )
        formatDateWatcher.installOn(binding.dateInput)
        formatTimeWatcher.installOn(binding.timeInput)
    }

    private fun configureSexChooseRecycler() {
        binding.sexRecyclerView.apply {
            layoutManager = RecyclerAdapterHelper.getFlexLayoutManager(requireContext())
            adapter = sexChooseAdapter
        }
        val sexArray: ArrayList<String> =
            resources.getStringArray(R.array.sex).toCollection(arrayListOf())
        sexArray.add(getString(R.string.doesnt_matter))
        val b: Array<String> = sexArray.reversed().toTypedArray()
        sexChooseAdapter.data = Choice.getItemsByStringArray(b)
        sexChooseAdapter.toggleSelection(0)
    }

    private fun configureSelectedTagsRecycler() {
        binding.tagRecyclerView.apply {
            layoutManager = RecyclerAdapterHelper.getFlexLayoutManager(requireContext())
            adapter = tagsSelectedAdapter
        }
    }

    private fun getSavedTags(tags: MutableList<Section<Choice>>?) {
        tags?.let {
            viewModel.updateTags(tags)
        }
    }

    private fun updateSelectedTags(tags: MutableList<Section<Choice>>) {
        val selectedTags = arrayListOf<Choice>()
        tags.forEach {
            it.items.forEach { tag ->
                if (tag.isChosen) {
                    selectedTags.add(tag)
                }
            }
        }
        tagsSelectedAdapter.data = selectedTags
    }

    private fun configureTagBottomSheet() {
        tagChooserBottomSheet = TagChooseBottomSheet(
            tagChooseListener,
            ::getSavedTags
        )
    }

    private fun handleClickListeners() {
        sexChooseListener = ItemClickListener { position ->
            sexChooseAdapter.toggleSelection(position)
        }
        tagChooseListener = TagChooseListener { sectionId, itemId ->
            // TODO:: implement if needed
        }
        tagSelectedClickListener = object : OnRecyclerItemClick {
            override fun invoke(pos: Int) {
                val updatedList = viewModel.tagSections.value
                updatedList?.let { it ->
                    it.forEach { section ->
                        section.items.forEach { tag ->
                            if (tag.id == pos) {
                                tag.apply {
                                    isChosen = !isChosen
                                }
                            }
                        }
                    }
                    viewModel.updateTags(it)
                }
            }
        }
        binding.tagTextView.setOnClickListener {
            ModalViewHelper.safelyShow(
                tagChooserBottomSheet,
                requireActivity().supportFragmentManager,
                "TAG"
            )
        }
        binding.clubTextView.setOnClickListener {
            // TODO:: open bottom sheet with clubs
        }
        binding.publishButton.setOnClickListener {
            updateEventData()
        }
    }

    private fun updateEventData() {
        if (binding.timeInput.text.toString().isEmpty()) {
            binding.timeInput.setText("12:00")
        }
        viewModel.event.apply {
            title = binding.titleInput.text.toString()
            description = binding.descriptionInput.text.toString()
            startAt = binding.dateInput.text.toString()
            time = binding.timeInput.text.toString()
            preferred = viewModel.convertPreferred(sexChooseAdapter.selectedItems.first())
            place = binding.placeInput.text.toString()
            tags = tagsSelectedAdapter.data
            // club = binding.clubTextView.text.toString()
        }
        viewModel.createEvent()
    }
}
