package com.dev.podo.event.ui.create_event

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dev.podo.R
import com.dev.podo.common.adapters.choice.ChoiceSelectableAdapter
import com.dev.podo.common.adapters.selectable.ItemClickListener
import com.dev.podo.common.model.entities.Choice
import com.dev.podo.common.model.entities.EventType
import com.dev.podo.common.ui.*
import com.dev.podo.common.utils.exceptions.SubscriptionRestrictException
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.services.SubscriptionIsNeededModalEvent
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.databinding.CreateEventFragmentBinding
import com.dev.podo.event.viewmodel.ChooseEventTypeFragmentViewModel
import com.dev.podo.podoplus.ui.SubscriptionDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateEventFragment : BaseFragment(R.layout.create_event_fragment) {

    private val viewModel: ChooseEventTypeFragmentViewModel by viewModels()
    private val binding: CreateEventFragmentBinding by viewBinding()
    private lateinit var nestedNavController: NavController
    private lateinit var eventTypeClickListener: ItemClickListener
    private val eventTypeRecyclerAdapter by lazy {
        ChoiceSelectableAdapter(eventTypeClickListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureNavController()
        configureObservers()
        configureClickListeners()
        configureEventTypesRecycler()
    }

    private fun configureNavController() {
        val nestedNavHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_crate_event_fragment) as? NavHostFragment
        nestedNavController = nestedNavHostFragment?.navController!!
    }

    private fun configureObservers() {
        viewModel.userEventsResponse.observe(viewLifecycleOwner) {
            ProgressBarHelper.showOnLoading(binding.loader.root, it)
            when (it) {
                is ResultState.Error -> {
                    eventTypeRecyclerAdapter.clearSelection()
                    if (it.exception is SubscriptionRestrictException) {
                        showSubscriptionDialog()
                        return@observe
                    }
                    showErrorAlert(it.exception)
                }
                ResultState.InProgress -> {}
                is ResultState.Success -> {
                    val selectedTypePos = eventTypeRecyclerAdapter.selectedItems.firstOrNull()
                    selectedTypePos?.let {
                        navigate(EventType.values()[selectedTypePos])
                    }
                }
            }
        }
    }

    private fun configureClickListeners() {
        eventTypeClickListener =
            ItemClickListener { position ->
                eventTypeRecyclerAdapter.toggleSelection(position)
                nestedNavController.navigateUp()
                if (Storage.user?.hasSubscription == true) {
                    navigate(EventType.values()[position])
                    return@ItemClickListener
                }
                when (EventType.values()[position]) {
                    EventType.LIVE_EVENT -> {
                        viewModel.checkEventCreatePermission(
                            EventType.values()[position]
                        )
                    }
                    EventType.DELAYED_EVENT -> {
                        viewModel.checkEventCreatePermission(
                            EventType.values()[position]
                        )
                    }
                }
            }
    }

    private fun showSubscriptionDialog() {
        SubscriptionIsNeededModalEvent().reportEvent("SecondEvent")
        SubscriptionDialog(
            requireContext(),
            findNavController(),
            R.string.for_event_creation_subscription_needed
        ).safelyShow(childFragmentManager, "SUBSCRIPTION_RESTRICT_MODAL")
    }

    private fun navigate(eventType: EventType) {
        when (eventType) {
            EventType.DELAYED_EVENT -> {
                nestedNavController.navigate(R.id.create_delayed_event_fragment)
            }
            EventType.LIVE_EVENT -> {
                nestedNavController.navigate(R.id.create_live_event_fragment)
            }
        }
    }

    private fun configureEventTypesRecycler() {
        val eventTypes = arrayListOf<Choice>()
        eventTypes.addAll(
            EventType.values().map {
                Choice(
                    it.ordinal,
                    it.getLabel(requireContext())
                )
            }
        )
        binding.eventTypesRecycler.apply {
            layoutManager = RecyclerAdapterHelper.getFlexLayoutManager(requireContext())
            adapter = eventTypeRecyclerAdapter
            addItemDecoration(RecyclerItemDecoration(requireContext(), right = 10))
        }
        eventTypeRecyclerAdapter.submitList(
            eventTypes
        )
    }

    enum class NavigationPlace {
        MAIN_SCREEN_PODONOW_CAROUSEL,
        MAIN_SCREEN_PODONOW_PLACEHOLDER,
        MAIN_SCREEN_DELAYED_EVENT_PLACEHOLDER,
        MANAGE_EVENTS_CREATE_BUTTON
    }
}
