package com.dev.podo.event.ui.event_edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dev.podo.R
import com.dev.podo.common.ui.ProgressBarHelper
import com.dev.podo.common.utils.callOnLifecycle
import com.dev.podo.common.utils.files.CacheDataSourceFactory
import com.dev.podo.common.utils.files.ExoPlayerFacade
import com.dev.podo.core.model.entities.ResultState
import com.dev.podo.core.model.entities.User
import com.dev.podo.core.ui.BaseFragment
import com.dev.podo.core.ui.MainActivity
import com.dev.podo.databinding.FragmentEditPodoNowBinding
import com.dev.podo.event.viewmodel.EventEditViewModel
import com.dev.podo.home.model.entities.HomeEventModel
import com.dev.podo.podoNow.ui.PodoNowFragmentDirections
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPodoNowFragment : BaseFragment(R.layout.fragment_edit_podo_now) {

    private val viewModel: EventEditViewModel by viewModels()
    private val binding: FragmentEditPodoNowBinding by viewBinding()
    private val navArgs: EditPodoNowFragmentArgs by navArgs()
    private var playerView: PlayerView? = null
    private val videoPlayerComponentLayout: Int
        get() = R.layout.player_view

    private var exoPlayer: ExoPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCollectors()
        initView()
        configureObservers()
    }

    private fun initCollectors() {
        callOnLifecycle {
            val eventId = navArgs.eventId
            viewModel.fetchDetailedEvent(eventId)
        }
    }

    private fun initView() {
        (activity as MainActivity).supportActionBar?.hide()
        binding.podoNowLayout.apply {
            respondActionsLayout.visibility = View.GONE
        }
        binding.handleClickListeners()
        addPlayerView()
    }

    private fun addPlayerView() {
        playerView = LayoutInflater.from(requireContext())
            .inflate(videoPlayerComponentLayout, null, false) as PlayerView?
        playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        playerView?.videoSurfaceView?.setOnClickListener {
            exoPlayer?.run {
                playWhenReady = !playWhenReady
            }
        }
        binding.podoNowLayout.playerContainer.addView(playerView)
    }

    private fun FragmentEditPodoNowBinding.handleClickListeners() {
        headerActionsLayout.closeActionImage.setOnClickListener {
            findNavController().navigateUp()
        }
        podoNowLayout.finishButton.setOnClickListener {
            viewModel.archiveEvent()
        }
    }

    private fun configureObservers() {
        viewModel.event.observe(viewLifecycleOwner) { state ->
            ProgressBarHelper.showOnLoading(binding.podoNowLayout.loader.root, state)
            when (state) {
                is ResultState.Error -> {
                    showErrorAlert(state.exception)
                }
                ResultState.InProgress -> {}
                is ResultState.Success -> {
                    binding.apply {
                        if (state.data.deletedAt == null) {
                            changeArchiveVisibility(true)
                        }
                        changeLoaderVisibility(true)
                        setUpUserInfo(state.data)
                        setupVideo(state.data)
                    }
                }
            }
        }
        viewModel.archiveEventResult.observe(viewLifecycleOwner) { state ->
            ProgressBarHelper.showOnLoading(binding.podoNowLayout.loader.root, state)
            when (state) {
                is ResultState.Error -> {
                    state.exception.message?.let { showErrorAlert(it) }
                    binding.changeArchiveVisibility(true)
                }
                is ResultState.InProgress,
                is ResultState.Success -> {
                    binding.changeArchiveVisibility(false)
                }
            }
        }
    }

    private fun FragmentEditPodoNowBinding.changeArchiveVisibility(isVisible: Boolean) {
        podoNowLayout.finishActionLayout.visibility =
            if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    private fun FragmentEditPodoNowBinding.setUpUserInfo(model: HomeEventModel) {
        podoNowLayout.apply {
            userInfoView.user = model.user
            if (model.place.isNotEmpty()) userInfoView.city = model.place
            context?.resources?.getColor(R.color.label_five)?.let {
                userInfoView.nameTextView?.setTextColor(it)
                userInfoView.ageTextView?.setTextColor(it)
            }
            userInfoView.onClickListener = { view: View, user: User ->
                user.id?.let { id ->
                    Navigation.findNavController(view).navigate(
                        PodoNowFragmentDirections.actionPodoNowFragmentToPublicProfileFragment(id)
                    )
                }
            }
            infoTitleView.text = model.title
            infoTagView.text = model.tags.firstOrNull()?.title ?: ""
        }
    }

    private fun setupVideo(event: HomeEventModel) {
        val videoURL = event.media?.first()?.url?.fullSize ?: return
        val cacheDataSourceFactory = CacheDataSourceFactory().createDataSource()
        val playerFacade = ExoPlayerFacade()
        exoPlayer = playerFacade.createPlayer(requireContext(), videoURL, cacheDataSourceFactory)
        playerView?.player = exoPlayer
        exoPlayer?.let {
            playerFacade.setup(it, autoplay = true)
            it.addListener(playerListener)
        }
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.apply {
            addListener(playerListener)
            playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.apply {
            removeListener(playerListener)
            playWhenReady = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.apply {
            removeListener(playerListener)
            stop()
            clearMediaItems()
        }
    }

    private fun getCurrentPosition() {
        exoPlayer?.run {
            if (isPlaying) {
                val percentByTime = duration / 100f
                val percent = currentPosition / percentByTime
                binding.podoNowLayout.progressView.progress = (percent).toInt()
                binding.podoNowLayout.playerContainer.postDelayed(this@EditPodoNowFragment::getCurrentPosition, 100)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                binding.podoNowLayout.playerContainer.postDelayed(::getCurrentPosition, 100)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Log.e("LOGGING", "Error on video play: ${error.message} ${error.stackTrace}")
        }

        @SuppressLint("SwitchIntDef")
        override fun onPlaybackStateChanged(@PlaybackStateCompat.State playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_BUFFERING -> {
                    changeLoaderVisibility(true)
                }
                PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.STATE_STOPPED -> {
                    changeLoaderVisibility(false)
                }
            }
        }
    }

    private fun changeLoaderVisibility(visible: Boolean) {
        binding.podoNowLayout.loader.root.visibility = if (visible) View.VISIBLE else View.GONE
    }
}
