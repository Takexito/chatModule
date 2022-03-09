package com.dev.podo.event.ui.chat

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.dev.podo.R
import com.dev.podo.common.ui.CustomDialogBuilder
import com.dev.podo.common.ui.CustomMultichoiceDialogFragment
import com.dev.podo.common.ui.listeners.EndlessRecyclerViewScrollListener
import com.dev.podo.common.utils.clear
import com.dev.podo.common.utils.collectOnLifecycle
import com.dev.podo.common.utils.presentError
import com.dev.podo.core.ImageUpload
import com.dev.podo.core.ImageUploader
import com.dev.podo.core.datasource.Storage
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.core.ui.MainActivity
import com.dev.podo.databinding.EventChatFragmentBinding
import com.dev.podo.event.model.entities.ChatEvent
import com.dev.podo.event.viewmodel.ChatViewModel
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment(R.layout.event_chat_fragment), ImageUpload {

    private val TAG = ChatFragment::class.java.canonicalName
    val viewBinding: EventChatFragmentBinding by viewBinding()
    val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private val imageUploader = ImageUploader(this)

    var endlessScrollListener: EndlessRecyclerViewScrollListener? = null

    private val bindings: ChatBinding by lazy { ChatBinding(requireContext()) }
    val adapters: ChatAdapter by lazy { ChatAdapter(bindings) }
    private val chatLogic: ChatLogic = ChatLogic(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViewModel()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchMessages(endlessScrollListener?.currentPage ?: 1)

    }

    private fun initViewModel() {
        viewModel.chatId = args.chatId
        viewModel.getChatInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMessageRecycler()
        chatLogic.initCollectors()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreDefaultActionBar()
    }

    private fun restoreDefaultActionBar() {
        val activity = (requireActivity() as MainActivity)
        activity.supportActionBar?.apply {
            clear()
            val imageView = activity.findViewById<ShapeableImageView>(R.id.main_toolbar_round_image)
            imageView.visibility = View.GONE
        }
    }

    private fun initAddFileButton(){
        viewBinding.eventChatAppendFileButton.setOnClickListener {
            CustomMultichoiceDialogFragment(
                "Фото",
                listOf(
                    "Снять",
                    "Выбрать из галереи"
                ),
                { pos ->
                    when(pos){
                        0 -> {
                            imageUploader.takePhoto(0)
                        }
                        else -> {
                            imageUploader.openGallery(0)
                        }
                    }
                },
                resources.getString(R.string.cancel)
            ).show(childFragmentManager, null)
        }
    }

    private fun initMessageRecycler() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        endlessScrollListener = object : EndlessRecyclerViewScrollListener(
            layoutManager = linearLayoutManager
        ) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.fetchMessages(page)
            }
        }

        viewBinding.apply {
            eventChatRecycler.apply {
                layoutManager = linearLayoutManager
                adapter = adapters.messageConcatAdapter
            }
            endlessScrollListener?.let {
                eventChatRecycler.addOnScrollListener(it)
            }
        }
    }

    fun initToolBar(chatBlock: ChatEvent.ChatBlock) {
        val activity = (requireActivity() as MainActivity)
        activity.supportActionBar?.apply {
            title = chatBlock.userName
            subtitle = chatBlock.eventTitle
            activity.initUserButton(chatBlock)
        }
    }

    private fun MainActivity.initUserButton(
        chatBlock: ChatEvent.ChatBlock,
    ) {
        val imageView = findViewById<ShapeableImageView>(R.id.main_toolbar_round_image)
        imageView.visibility = View.VISIBLE
        Glide
            .with(requireContext())
            .load(chatBlock.eventImage)
            .centerCrop()
            .into(imageView)

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        toolbar?.setOnClickListener {
            val list = listOf("Страница события", "Профиль пользователя", "Удалить чат")
            CustomMultichoiceDialogFragment("", list, onActionButtons = { index: Int ->
                when (index) {
                    0 -> { navigateDetailEvent(chatBlock) }
                    1 -> { navigateCompanionPublicProfile(chatBlock) }
                    2 -> { viewModel.deleteChat() }
                    else -> { }
                }
            }).show(childFragmentManager, "")
        }
    }

    private fun navigateCompanionPublicProfile(chatBlock: ChatEvent.ChatBlock) {
        val companionUserId = chatBlock.users.firstOrNull {
            it.id != Storage.user?.id
        }?.id
        companionUserId?.let {
            val action = ChatFragmentDirections.actionChatToPublicProfile(it)
            findNavController().navigate(action)
        }
    }

    private fun navigateDetailEvent(chatBlock: ChatEvent.ChatBlock) {
        chatBlock.eventId?.let { id ->
            val action = ChatFragmentDirections.actionChatToDetailedEvent(id, true)
            findNavController().navigate(action)
        }
    }

    fun navigateBack() {
        findNavController().popBackStack()
    }

    fun selectViewByChatStatus(chatBlock: ChatEvent.ChatBlock) {
        when (chatBlock.state) {
            ChatEvent.ChatState.AWAIT -> {
                awaitView()
            }
            ChatEvent.ChatState.DECLINE -> {
                declineView()
            }
            ChatEvent.ChatState.TIMEOUT -> {
                timeoutView()
            }
            ChatEvent.ChatState.ACTIVE -> {
                activeView()
                initMessageLayout()
            }
            ChatEvent.ChatState.END -> {
                endView()
                initEndLayout()
            }
            ChatEvent.ChatState.END_AWAIT -> {
                setEndAwaitLayout()
            }
        }
    }

    fun setEndAwaitLayout() {
        endAwait()
        initEndAwaitLayout()
    }

    fun initMessageLayout() {
        initAddFileButton()
        chatLogic.collectSendMessageFlow()
        chatLogic.collectSendAttachmentMessageFlow()
        viewBinding.eventChatSendMessageButton.setOnClickListener {
            chatLogic.sendMessage()
        }

        viewBinding.eventChatSendMessageButtonRepeat.setOnClickListener {
            chatLogic.sendMessage()
        }
    }

    private fun initEndLayout() {
        viewBinding.eventChatEventEndContinueButton.setOnClickListener {
            viewModel.continueChat()
        }
        viewBinding.eventChatEventEndDeleteButton.setOnClickListener {
            askForChatDeletion()
        }
    }

    private fun askForChatDeletion() {
        DeleteChatDialog(resources) {
            viewModel.deleteChat()
            navigateBack()
        }.show(childFragmentManager)
    }

    private fun initEndAwaitLayout() {
        viewBinding.eventChatEventEndAwaitDeleteButton.setOnClickListener {
            viewModel.deleteChat()
            navigateBack()
        }
    }

    private fun endAwait() {
        viewBinding.eventChatResponseAwaitLayout.visibility = View.GONE
        viewBinding.eventChatEventEndLayout.visibility = View.GONE
        viewBinding.eventChatEventEndAwaitLayout.visibility = View.VISIBLE
        viewBinding.eventChatMessageLayout.visibility = View.GONE
    }

    private fun endView() {
        viewBinding.eventChatResponseAwaitLayout.visibility = View.GONE
        viewBinding.eventChatEventEndLayout.visibility = View.VISIBLE
        viewBinding.eventChatEventEndAwaitLayout.visibility = View.GONE
        viewBinding.eventChatMessageLayout.visibility = View.GONE
    }

    fun sendErrorView() {
        viewBinding.eventChatSendMessageProgress.visibility = View.GONE
        viewBinding.eventChatSendMessageButtonRepeat.visibility = View.VISIBLE
        viewBinding.eventChatSendMessageButton.visibility = View.GONE
    }

    private fun activeView() {
        viewBinding.eventChatResponseAwaitLayout.visibility = View.GONE
        viewBinding.eventChatEventEndLayout.visibility = View.GONE
        viewBinding.eventChatEventEndAwaitLayout.visibility = View.GONE
        viewBinding.eventChatMessageLayout.visibility = View.VISIBLE
    }

    fun sendSuccessView() {
        viewBinding.eventChatSendMessageProgress.visibility = View.GONE
        viewBinding.eventChatSendMessageButtonRepeat.visibility = View.GONE
        viewBinding.eventChatSendMessageButton.visibility = View.VISIBLE
    }

    private fun declineView() {
        viewBinding.eventChatResponseAwaitLayout.visibility = View.GONE
        viewBinding.eventChatEventEndLayout.visibility = View.GONE
        viewBinding.eventChatEventEndAwaitLayout.visibility = View.GONE
        viewBinding.eventChatMessageLayout.visibility = View.GONE
    }

    private fun timeoutView() {
        viewBinding.eventChatResponseAwaitLayout.visibility = View.GONE
        viewBinding.eventChatEventEndLayout.visibility = View.GONE
        viewBinding.eventChatEventEndAwaitLayout.visibility = View.GONE
        viewBinding.eventChatMessageLayout.visibility = View.GONE
    }

    private fun awaitView() {
        viewBinding.eventChatResponseAwaitLayout.visibility = View.VISIBLE
        viewBinding.eventChatEventEndLayout.visibility = View.GONE
        viewBinding.eventChatEventEndAwaitLayout.visibility = View.GONE
        viewBinding.eventChatMessageLayout.visibility = View.GONE
    }

    fun sendProgressView() {
        viewBinding.eventChatSendMessageProgress.visibility = View.VISIBLE
        viewBinding.eventChatSendMessageButtonRepeat.visibility = View.GONE
        viewBinding.eventChatSendMessageButton.visibility = View.GONE
    }

    override fun getFragment(): Fragment = this

    override fun updateImageData(position: Int, uri: Uri) {
        chatLogic.sendAttachment(uri)
//        viewModel.uploadImages.add(position, uri)
    }

    override fun showError(message: String) {
        showErrorAlert(message)
    }
}
