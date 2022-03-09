package com.dev.podo.event.ui.event_prompts

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.dev.podo.R
import com.dev.podo.common.model.entities.EventType
import com.dev.podo.common.ui.RecyclerItemDecoration
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.services.OnPromptAcceptEvent
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.EmptyTitledViewBinding
import com.dev.podo.databinding.EventPromptItemBinding
import com.dev.podo.databinding.FragmentEventPromptsBinding
import com.dev.podo.event.model.entities.EventShortData
import com.dev.podo.event.model.entities.prompt.Prompt
import com.dev.podo.event.model.entities.prompt.PromptComparator
import com.dev.podo.event.ui.adapter.EmptyPromptsAdapter
import com.dev.podo.event.ui.adapter.ReposLoadStateAdapter
import com.dev.podo.event.ui.adapter.prompt.PromptAdapter
import com.dev.podo.event.viewmodel.EventPromptsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy

@AndroidEntryPoint
class FragmentEventPrompts : BaseFragment(R.layout.fragment_event_prompts) {

    private val viewModel: EventPromptsViewModel by viewModels()
    private val viewBinding: FragmentEventPromptsBinding by viewBinding()
    private val navArguments: FragmentEventPromptsArgs by navArgs()

    private val promptViewBinding: ViewBindingGetter<EventPromptItemBinding> = { parent ->
        EventPromptItemBinding.inflate(LayoutInflater.from(requireContext()), parent, false)
    }

    private val emptyClubPromptsBinding: ViewBindingGetter<EmptyTitledViewBinding> = { parent ->
        val binding: EmptyTitledViewBinding =
            EmptyTitledViewBinding.inflate(LayoutInflater.from(requireContext()), parent, false)
        binding.title.text = getString(R.string.event_on_club_moderation)
        binding
    }

    private val emptyPromptsBinding: ViewBindingGetter<EmptyTitledViewBinding> = { parent ->
        val binding: EmptyTitledViewBinding =
            EmptyTitledViewBinding.inflate(LayoutInflater.from(requireContext()), parent, false)
        binding.title.text = getString(R.string.waiting_for_first_responses)
        binding.image.visibility = View.VISIBLE
        Glide.with(requireContext())
            .load(R.drawable.podo_normal)
            .into(binding.image)
        binding
    }

    private val retryPromptListener = object : OnRecyclerItemClick {
        override fun invoke(pos: Int) {
            retryPromptsFetch()
        }
    }

    private val promptAdapter by lazy {
        PromptAdapter(
            retryPromptListener,
            this::promptAction,
            this::navigatePublicProfile,
            PromptComparator,
            promptViewBinding
        )
    }

    private var promptConcatAdapter = ConcatAdapter(
        promptAdapter.withLoadStateFooter(
            ReposLoadStateAdapter {
                retryPromptsFetch()
            }
        )
    )

    private var emptyPromptAdapter: EmptyPromptsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventData = navArguments.eventShortData
        Log.e("LOGGING", "BEFORE VM CALL")
        viewModel.fetchEventResponses(eventData.id)
        Log.e("LOGGING", "AFTER VM CALL")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureActionBar()
        parseNavArgs()
        viewBinding.bind()
        configureObservers()
    }

    private fun FragmentEventPromptsBinding.bind() {
        handleListeners()
        configurePromptsRecycler()
        parseNavArgs()
    }

    private fun parseNavArgs() {
        val eventData: EventShortData = navArguments.eventShortData
        eventData.run {
            viewBinding.place.text = city
            viewBinding.title.text = title
            viewBinding.type.text = requireContext().getString(type.titleRes)
        }
        configureEmptyAdapter()
    }

    private fun configureEmptyAdapter(isClubEvent: Boolean = false) {
        if (isClubEvent) {
            emptyPromptAdapter = EmptyPromptsAdapter({}, emptyClubPromptsBinding)
            return
        }
        emptyPromptAdapter = EmptyPromptsAdapter({}, emptyPromptsBinding)
    }

    private fun FragmentEventPromptsBinding.configurePromptsRecycler() {
        eventPromptRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(RecyclerItemDecoration(requireContext(), bottom = 24))
        }
        eventPromptRecycler.adapter = promptConcatAdapter

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            promptAdapter.submitData(PagingData.from(Prompt.fakeFactory(1)))
            viewModel.promptFlow
                .distinctUntilChangedBy { it }
                .collectLatest { pagingData ->
                    promptAdapter.submitData(pagingData)
                }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            promptAdapter.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .collect { loadState ->

                    if (loadState.refresh is LoadState.Loading) {
                        promptAdapter.enableSkeleton()
                    } else {
                        eventPromptSwipeRefreshLayout.isRefreshing = false
                        promptAdapter.disableSkeleton()
                    }

                    val errorState = loadState.source.append as? LoadState.Error
                        ?: loadState.source.prepend as? LoadState.Error
                        ?: loadState.append as? LoadState.Error
                        ?: loadState.prepend as? LoadState.Error
                        ?: loadState.refresh as? LoadState.Error
                    errorState?.error?.let {
                        val errorMessage = it.localizedMessage
                            ?: it.message
                        errorMessage?.let { message ->
                            showErrorAlert(message)
                        }
                    }

                    val isListEmpty: Boolean =
                        (loadState.refresh is LoadState.NotLoading) &&
                                (errorState == null) &&
                                loadState.append.endOfPaginationReached &&
                                (promptAdapter.itemCount == 0)

                    showEmptyList(isListEmpty)
                }
        }
    }

    private fun promptAction(prompt: Prompt, actionType: Int, position: Int) {
        prompt.id?.let {
            viewModel.performPromptAction(it, actionType, position)
        }
    }

    private fun navigatePublicProfile(prompt: Prompt) {
        prompt.userId?.let {
            val action = FragmentEventPromptsDirections.actionEventPromptsToPublicProfile(it)
            findNavController().navigate(action)
        }
    }

    private fun retryPromptsFetch() {
        promptAdapter.retry()
    }

    private fun configureObservers() {
        viewModel.promptActionData.observe(viewLifecycleOwner) { state ->
            when (state.result) {
                is ResultState.Success -> {
                    sendAnalytic(promptAdapter.data[state.position])
                    viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                        promptAdapter.removePrompt(state.position)
                    }
                }
                is ResultState.Error -> {
                    state.result.exception.message?.let { showErrorAlert(it) }
                }
                is ResultState.InProgress -> {}
            }
        }
    }

    private fun sendAnalytic(prompt: Prompt) {
        if (prompt.eventId == null) {
            return
        }
        OnPromptAcceptEvent().reportEvent(
            prompt.message?.isEmpty() ?: true,
            prompt.user?.sex,
            Storage.user?.sex,
            prompt.user?.description?.isEmpty() ?: true,
            prompt.user?.media?.count() ?: 0,
            prompt.eventId
        )
    }

    private fun FragmentEventPromptsBinding.showEmptyList(isVisible: Boolean) {
        emptyPromptAdapter?.let {
            if (isVisible) {
                promptConcatAdapter.addAdapter(it)
                return
            }
            promptConcatAdapter.removeAdapter(it)
        }
    }

    private fun FragmentEventPromptsBinding.handleListeners() {
        eventPromptSwipeRefreshLayout.setOnRefreshListener {
            promptAdapter.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        /** Update options in menu */
        requireActivity().invalidateOptionsMenu()
    }

    private fun configureActionBar() {
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event_responses_settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_event_settings -> {
                val event = navArguments.eventShortData
                val action = when (event.type) {
                    EventType.LIVE_EVENT -> {
                        FragmentEventPromptsDirections
                            .actionEventResponsesToEditPodoNowEvent(event.id)
                    }
                    EventType.DELAYED_EVENT -> {
                        FragmentEventPromptsDirections
                            .actionEventResponsesToEditEvent(event.id)
                    }
                }
                action.let { findNavController().navigate(it) }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
