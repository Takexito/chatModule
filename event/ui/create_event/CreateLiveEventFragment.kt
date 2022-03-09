package com.dev.podo.event.ui.create_event

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.podo.R
import com.dev.podo.camer.ui.VideoPreviewFragment
import com.dev.podo.camer.util.MediaStorage
import com.dev.podo.camer.util.VideoHelper
import com.dev.podo.common.adapters.choice.ChoiceSelectableAdapter
import com.dev.podo.common.adapters.selectable.ItemClickListener
import com.dev.podo.common.adapters.tag.TagChooseListener
import com.dev.podo.common.model.entities.Choice
import com.dev.podo.common.model.entities.tag.TagGroup
import com.dev.podo.common.ui.CustomImageDialogBuilder
import com.dev.podo.common.ui.CustomMultichoiceDialogFragment
import com.dev.podo.common.ui.ProgressBarHelper
import com.dev.podo.common.ui.RecyclerAdapterHelper
import com.dev.podo.common.ui.bottomSheetChoose.TagSingleChooseBottomSheet
import com.dev.podo.common.utils.FileUtils
import com.dev.podo.common.utils.ModalViewHelper
import com.dev.podo.common.utils.collectOnLifecycle
import com.dev.podo.common.utils.contracts.OpenVideoGalleryContract
import com.dev.podo.common.utils.contracts.OpenVideoGalleryData
import com.dev.podo.common.utils.contracts.PositionedContractData
import com.dev.podo.common.utils.services.SentryUtil
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.core.ui.adapter.OnRecyclerItemClick
import com.dev.podo.core.ui.adapter.ViewBindingGetter
import com.dev.podo.databinding.CreateLiveEventFragmentBinding
import com.dev.podo.databinding.RecyclerViewChipItemSelectedBinding
import com.dev.podo.event.ui.adapter.SelectedTagsAdapter
import com.dev.podo.event.viewmodel.CreateLiveEventViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry

@AndroidEntryPoint
class CreateLiveEventFragment : BaseFragment(R.layout.create_live_event_fragment) {

    private val TAG = CreateLiveEventFragment::class.java.canonicalName
    private val viewModel: CreateLiveEventViewModel by viewModels()
    private val binding: CreateLiveEventFragmentBinding by viewBinding()
    private lateinit var tagChooseListener: TagChooseListener
    private lateinit var tagSelectedClickListener: OnRecyclerItemClick
    private var recyclerTagsItemBinding: ViewBindingGetter<RecyclerViewChipItemSelectedBinding> =
        { parent ->
            RecyclerViewChipItemSelectedBinding.inflate(
                LayoutInflater.from(requireContext()),
                parent,
                false
            )
        }
    private lateinit var sexChooseListener: ItemClickListener
    private val tagsSelectedAdapter by lazy {
        SelectedTagsAdapter(
            tagSelectedClickListener,
            recyclerTagsItemBinding
        )
    }
    private val sexChooseAdapter by lazy {
        ChoiceSelectableAdapter(sexChooseListener)
    }
    private lateinit var tagChooserBottomSheet: TagSingleChooseBottomSheet
    private lateinit var openGalleryResultLauncher: ActivityResultLauncher<PositionedContractData<OpenVideoGalleryData>>
    private var rootNavController: NavController? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        openGalleryResultLauncher = registerForActivityResult(
            OpenVideoGalleryContract()
        ) { resultData ->
            resultData?.let {
                if (resultData.isSuccessful) {
                    resultData.inputData?.let {
                        Log.d(TAG, "Video data uri: ${it.path}")
                        viewModel.videoUri = it
                        parseVideo(it)
                    }
                    return@registerForActivityResult
                }
                showErrorAlert(getString(R.string.error_on_video_gallery_choose))
            }
        }
    }

    private fun parseVideo(uri: Uri) {
        val mMDR = MediaMetadataRetriever()
        try {
            mMDR.setDataSource(requireContext(), uri)
            val time = mMDR.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillisec = time?.toLong()
            timeInMillisec?.let {
                if (it < VideoHelper.MAX_VIDEO_DURATION_MS) {
                    val bitmap = mMDR.getFrameAtTime(0L)
                    if (bitmap == null) {
                        Log.e(TAG, "VideoPreviewThumbnail is empty")
                    }
                    binding.showVideoThumb(uri, bitmap)
                    return@let
                }
                trimVideoNavigate(uri)
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Log.e(TAG, "Exception on video parsing : ${e.message}")
        } finally {
            mMDR.release()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootNavController = Navigation.findNavController(
            requireActivity(),
            R.id.nav_host_fragment_activity_main
        )
        handleClickListeners()
        configureSelectedTagsRecycler()
        configureSexChooseRecycler()
        configureTabBottomSheet()
        configureObservers()
    }

    private fun handleClickListeners() {
        sexChooseListener = ItemClickListener { position ->
            sexChooseAdapter.toggleSelection(position)
        }
        tagChooseListener = TagChooseListener { sectionId, itemId ->
            val tag = tagChooserBottomSheet.tagsSections
                .firstOrNull { it.id == sectionId }?.items
                ?.firstOrNull { it.id == itemId }
            tag?.let {
                viewModel.updateSelectedTag(tag)
            }
        }
        tagSelectedClickListener = object : OnRecyclerItemClick {
            override fun invoke(pos: Int) {
                viewModel.updateSelectedTag()
            }
        }
        binding.apply {
            tagTextView.setOnClickListener {
                ModalViewHelper.safelyShow(
                    tagChooserBottomSheet,
                    childFragmentManager,
                    "TAG_CHOOSE_BOTTOM_SHEET"
                )
            }
            publishButton.setOnClickListener {
                updateEventData()
                viewModel.initiateEventCreation(
                    requireContext().contentResolver,
                )
            }
            videoFrame.apply {
                root.setOnClickListener { showVideoChooseModal() }
                cancelButton.setOnClickListener {
                    cancelButton.visibility = View.GONE
                    loadingView.root.visibility = View.GONE
                    chooseButton.visibility = View.VISIBLE
                    image.setImageDrawable(null)
                    image.setBackgroundResource(R.color.background_gray)
                }
            }
        }
    }

    private fun captureVideo() {
        val action = CreateEventFragmentDirections.actionCreateEventToVideoCapture()
        rootNavController?.navigate(action)
    }

    private fun trimVideoNavigate(uri: Uri) {
        val action = CreateEventFragmentDirections.actionCreateLiveEventToTrimVideo(uri)
        rootNavController?.navigate(action)
    }

    private fun openGallery() {
        openGalleryResultLauncher.launch(
            PositionedContractData(
                0,
                OpenVideoGalleryData(
                    "video/*",
                    FileUtils.getAllowedVideoMimeTypes()
                )
            )
        )
    }

    private fun CreateLiveEventFragmentBinding.showVideoThumb(
        uri: Uri,
        previewBitmap: Bitmap? = null
    ) {
        var thumbnail: Bitmap? = previewBitmap
        try {
            if (thumbnail == null) {
                thumbnail = MediaStorage.videoThumbnail(requireContext(), uri)
            }
        } catch (e: SecurityException) {
            SentryUtil.captureException(e, resources.getString(R.string.error_on_video_gallery_choose))
            showErrorAlert(resources.getString(R.string.error_on_video_gallery_choose))
        } catch (e: Exception) {
            showErrorAlert(resources.getString(R.string.error_on_video_gallery_choose))
        }
        if (thumbnail == null) {
            return
        }
        videoFrame.apply {
            cancelButton.visibility = View.VISIBLE
            loadingView.root.visibility = View.VISIBLE
            chooseButton.visibility = View.INVISIBLE
        }
        Glide.with(requireContext())
            .load(thumbnail)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    videoFrame.apply {
                        cancelButton.visibility = View.GONE
                        loadingView.root.visibility = View.GONE
                        chooseButton.visibility = View.VISIBLE
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    videoFrame.apply {
                        loadingView.root.visibility = View.GONE
                    }
                    return false
                }
            })
            .into(videoFrame.image)
    }

    private fun configureSexChooseRecycler() {
        binding.sexRecyclerView.apply {
            layoutManager = RecyclerAdapterHelper.getFlexLayoutManager(requireContext())
            adapter = sexChooseAdapter
        }
        // TODO:: separate event sex choices to constants
        val dataArray: Array<String> = resources.getStringArray(R.array.event_sex)
        sexChooseAdapter.data = Choice.getItemsByStringArray(dataArray)
        sexChooseAdapter.toggleSelection(0)
    }

    private fun configureSelectedTagsRecycler() {
        binding.tagRecyclerView.apply {
            layoutManager = RecyclerAdapterHelper.getFlexLayoutManager(requireContext())
            adapter = tagsSelectedAdapter
        }
    }

    private fun configureTabBottomSheet() {
        tagChooserBottomSheet = TagSingleChooseBottomSheet().apply {
            chooseListener = tagChooseListener
        }
    }

    private fun updateSelectedTag(tag: Choice?) {
        val selectedTags = arrayListOf<Choice>()
        tag?.let {
            selectedTags.add(tag)
        }
        tagsSelectedAdapter.data = selectedTags
    }

    private fun configureObservers() {
        viewModel.tagGroupsResponse.observe(viewLifecycleOwner, {
            when (it) {
                is ResultState.Error -> showErrorAlert(it.exception)
                is ResultState.Success -> {
                    val tagSections = TagGroup.convertToSections(it.data)
                    viewModel.updateTags(tagSections.toCollection(arrayListOf()))
                    tagChooserBottomSheet.submitList(tagSections)
                }
            }
        })
        collectOnLifecycle(flow = viewModel.selectedTag) { choice ->
            updateSelectedTag(choice)
        }
        collectOnLifecycle(flow = viewModel.eventResponse) { result ->
            parentFragment?.parentFragment?.view?.findViewById<CardView>(R.id.loader)
                ?.let { loader ->
                    ProgressBarHelper.showOnLoading(loader, result)
                }
            disableClicks(result is ResultState.InProgress)
            when (result) {
                is ResultState.Error -> showErrorAlert(result.exception)
                is ResultState.Success -> {
                    disableClicks(true)
                    val dialogFragment = CustomImageDialogBuilder()
                        .title(resources.getString(R.string.event_created))
                        .message(resources.getString(R.string.now_wait_for_responses))
                        .imageResource(R.drawable.podo_smile_image)
                        .actionButtonTitle(resources.getString(R.string.continueString))
                        .onAction { navigateEventManage() }
                        .build()
                    dialogFragment.isCancelable = false
                    ModalViewHelper.safelyShow(
                        dialogFragment,
                        childFragmentManager,
                        "EVENT_UPLOAD_RESPONSE_MODAL"
                    )
                }
            }
        }
        rootNavController?.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Uri>(VideoPreviewFragment.capturedVideoKey)
            ?.observe(viewLifecycleOwner) { result ->
                result?.let {
                    Log.e(TAG, "CapturedVideoUri: $it")
                    binding.showVideoThumb(it)
                    viewModel.videoUri = it
                    rootNavController?.currentBackStackEntry?.savedStateHandle?.remove<Uri>(
                        VideoPreviewFragment.capturedVideoKey
                    )
                }
            }
    }

    private fun navigateEventManage() {
        val mainNavController = Navigation.findNavController(
            requireActivity(),
            R.id.nav_host_fragment_activity_main
        )
        val action = CreateEventFragmentDirections.actionCreateEventToEventManage()
        mainNavController.navigate(action)
    }

    private fun showVideoChooseModal() {
        val options = mutableListOf(
            resources.getString(R.string.capture_video),
            resources.getString(R.string.choose_from_gallery)
        )
        val dialogFragment = CustomMultichoiceDialogFragment(
            title = getString(R.string.video_invite),
            actionButtonsTitles = options,
            onActionButtons = { optionId ->
                when (optionId) {
                    0 -> captureVideo()
                    1 -> openGallery()
                }
            },
            cancelButtonTitle = getString(R.string.cancel)
        )
        ModalViewHelper.safelyShow(
            dialogFragment,
            childFragmentManager,
            "VIDEO_UPLOAD_CHOOSE_MODAL"
        )
    }

    private fun updateEventData() {
        viewModel.event.apply {
            title = binding.titleInput.text.toString()
            preferred = viewModel.convertPreferred(sexChooseAdapter.selectedItems.first())
            tags = tagsSelectedAdapter.data
        }
    }

    private fun disableClicks(isLoading: Boolean = false) {
        binding.publishButton.isClickable = !isLoading
    }
}
