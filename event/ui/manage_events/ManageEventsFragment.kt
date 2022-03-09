package com.dev.podo.event.ui.manage_events

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dev.podo.R
import com.dev.podo.common.adapters.selectable.ItemClickListener
import com.dev.podo.common.utils.collectOnLifecycle
import com.dev.podo.common.utils.px
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.core.viewmodel.AnalyticsViewModel
import com.dev.podo.databinding.EventChatItemBinding
import com.dev.podo.databinding.FragmentManageEventsBinding
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.event.model.entities.EventShortData
import com.dev.podo.event.ui.adapter.ChatAdapter
import com.dev.podo.event.ui.adapter.UserEventAdapter
import com.dev.podo.event.ui.create_event.CreateEventFragment
import com.dev.podo.event.viewmodel.ChatViewModel
import com.dev.podo.event.viewmodel.ManageEventsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageEventsFragment : BaseFragment(R.layout.fragment_manage_events) {

    private val viewModel: ManageEventsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val analyticsViewModel: AnalyticsViewModel by viewModels()
    private val viewBinding: FragmentManageEventsBinding by viewBinding()
    private var eventClickListener: ItemClickListener? = null
    val eventsViewPagerAdapter: UserEventAdapter by lazy {
        UserEventAdapter(eventClickListener!!)
    }

    private var chatRecyclerBinding: ViewBindingGetter<EventChatItemBinding> = { parent ->
        EventChatItemBinding.inflate(LayoutInflater.from(requireContext()), parent, false)
    }

    private val chatRecyclerAdapter = ChatAdapter(::navigateChat, chatRecyclerBinding).apply {
        skeletonCount = 3
        emptyItem = ChatEvent.ChatBlock.emptyChat()
    }

    private fun navigateChat(pos: Int) {
        val chatId = chatRecyclerAdapter.data[pos].id
        val action =
            ManageEventsFragmentDirections.actionNavigationManageEventsToChatFragment(chatId)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.fetchUserChats()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        handleClickListeners()
        configureObservers()
        configureViewPager()
        initCollectors()
    }

    private fun initRecycler() {
        viewBinding.eventDialogsRecycler.apply {
            adapter = chatRecyclerAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val padding = 16.px
                    val itemPosition = parent.getChildLayoutPosition(view)
                    if (itemPosition == RecyclerView.NO_POSITION) {
                        return
                    }
                    val itemCount = state.itemCount
                    if (itemCount > 0 && itemPosition == itemCount - 1) {
                        outRect.bottom = padding
                    }
                }
            })
        }
    }

    private fun initCollectors() {
        collectOnLifecycle(Lifecycle.State.STARTED, chatViewModel.newMessageFlow) { result ->
            result?.let {
                chatRecyclerAdapter.newMessage(it)
//                viewModel.fetchUserChats()
            }
        }
    }

    private fun changeViewPagerIndicatorVisibility(isVisible: Boolean = false) {
        viewBinding.userEventsViewPagerIndicator.visibility =
            (if (isVisible) View.VISIBLE else View.INVISIBLE)
    }

    private fun handleClickListeners() {
        eventClickListener = object : ItemClickListener {
            override fun onItemClicked(position: Int) {
                val event = eventsViewPagerAdapter.data[position]
                event?.let {
                    navigateEventPage(it.toShortData())
                    return
                }
                navigateCreateEvent()
            }
        }
    }

    private fun configureObservers() {
        viewModel.eventResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ResultState.Error -> {
                    changeViewPagerIndicatorVisibility()
                    eventsViewPagerAdapter.changeState(false)
                }
                is ResultState.InProgress -> {
                    changeViewPagerIndicatorVisibility()
                    eventsViewPagerAdapter.changeState(true)
                }
                is ResultState.Success -> {
                    eventsViewPagerAdapter.changeState(false)
                    eventsViewPagerAdapter.data = it.data.toMutableList()
                    if (it.data.count() >= 1) {
                        changeViewPagerIndicatorVisibility(true)
                    }
                }
            }
        }

        viewModel.chats.observe(viewLifecycleOwner) {
            when (it) {
                is ResultState.Error -> {
                    chatRecyclerAdapter.changeState(false)
                }
                is ResultState.InProgress -> {
                    chatRecyclerAdapter.changeState(true)
                }
                is ResultState.Success -> {
                    chatRecyclerAdapter.changeState(false)
                    chatRecyclerAdapter.data = it.data.toMutableList()
                }
            }
        }
    }

    private fun configureViewPager() {
        viewBinding.userEventsViewPager.apply {
            adapter = eventsViewPagerAdapter
        }
        viewBinding.userEventsViewPagerIndicator.apply {
            attachToPager(viewBinding.userEventsViewPager)
        }
    }

    private fun navigateCreateEvent() {
        analyticsViewModel.reportEventCreateButtonClickEvent(
            CreateEventFragment.NavigationPlace.MANAGE_EVENTS_CREATE_BUTTON
        )
        val action = ManageEventsFragmentDirections.actionManageEventsToCreateEvent()
        findNavController().navigate(action)
    }

    private fun navigateEventPage(eventData: EventShortData) {
        val action = ManageEventsFragmentDirections.actionManageEventsToEventPrompts(eventData)
        findNavController().navigate(action)
    }
}
