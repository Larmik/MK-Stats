package fr.harmoniamk.statsmk.fragment.warTrackResult

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentResultWarTrackBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.ui.TrackView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WarTrackResultFragment : Fragment(R.layout.fragment_result_war_track) {

    private val binding : FragmentResultWarTrackBinding by viewBinding()
    private val viewModel: WarTrackResultViewModel by viewModels()
    private var track: Int? = null
    private val popup by lazy { PopupFragment(R.string.creating_war, loading = true) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        track = arguments?.getInt("track").takeIf { it != -1 }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WarTrackResultAdapter()
        val shockAdapter = ShockAdapter()
        binding.resultRv.adapter = adapter
        binding.shockRv.adapter = shockAdapter
        viewModel.bind(
            onBack = requireActivity().backPressedDispatcher(viewLifecycleOwner),
            onValid = binding.validateBtn.clicks(),
            onShockAdded = adapter.onShockAdded,
            onShockRemoved = adapter.onShockRemoved
        )
        val trackView = TrackView(requireContext())
        trackView.bind(track)
        binding.trackView.addView(trackView)

        viewModel.sharedWarPos
            .onEach { adapter.addResults(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedShocks
            .onEach { shockAdapter.addItems(it) }
            .launchIn(lifecycleScope)

        viewModel.sharedScore
            .onEach {
                val textColor = when  {
                    it.displayedDiff.contains("-") -> R.color.lose
                    it.displayedDiff.contains("+") -> R.color.green
                    else -> R.color.harmonia_dark
                }
                binding.trackDiff.setTextColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requireContext().getColor(textColor)
                    else ContextCompat.getColor(requireContext(), textColor)
                )
                binding.trackScore.text = it.displayedResult
                binding.trackDiff.text = it.displayedDiff
            }.launchIn(lifecycleScope)

        viewModel.sharedBack
            .filter { findNavController().currentDestination?.id == R.id.warTrackResultFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)

        viewModel.sharedBackToCurrent
            .filter { findNavController().currentDestination?.id == R.id.warTrackResultFragment }
            .onEach { findNavController().navigate(WarTrackResultFragmentDirections.backToCurrent()) }
            .launchIn(lifecycleScope)



        viewModel.sharedLoading
            .onEach {
                popup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
            }.launchIn(lifecycleScope)
    }

}