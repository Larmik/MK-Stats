package fr.harmoniamk.statsmk.features.currentWar

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.features.quitWar.QuitWarDialogFragment
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentCurrentWarBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.clicks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CurrentWarFragment : Fragment(R.layout.fragment_current_war) {

    private val binding : FragmentCurrentWarBinding by viewBinding()
    private val viewModel: CurrentWarViewModel by viewModels()
    private var warId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(requireActivity().backPressedDispatcher(viewLifecycleOwner), binding.nextTrackBtn.clicks())

        viewModel.sharedHost
            .onEach {
                binding.waitingNextTrack.isVisible = false
                binding.nextTrackBtn.isVisible = true
            }.launchIn(lifecycleScope)

        viewModel.sharedCurrentWar
            .onEach {
                warId = it.mid
                binding.warTitleTv.text = it.name
                binding.warDateTv.text = it.createdDate
                binding.currentWarTv.text = it.displayedState
                binding.scoreTv.text = it.displayedScore
                binding.diffScoreTv.text = it.displayedDiff

            }.launchIn(lifecycleScope)

        viewModel.sharedBack
            .onEach {
                val dialog = QuitWarDialogFragment()
                viewModel.bindDialog(dialog.sharedWarLeft, dialog.sharedClose)
                if (!dialog.isAdded) dialog.show(childFragmentManager, null)
                viewModel.sharedCancel
                    .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
                    .onEach { dialog.dismiss() }
                    .launchIn(lifecycleScope)
            }.launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.backToWars()) }
            .launchIn(lifecycleScope)

        viewModel.sharedWaitingPlayers
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.waitPlayers()) }
            .launchIn(lifecycleScope)

        viewModel.sharedSelectTrack
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .mapNotNull { warId }
            .onEach {
                findNavController().navigate(CurrentWarFragmentDirections.addTrack(it))
            }
            .launchIn(lifecycleScope)

        viewModel.sharedGoToPos
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach {
                findNavController().navigate(CurrentWarFragmentDirections.enterPositions(it.trackIndex ?: -1, warTrackId = it.mid))
            }
            .launchIn(lifecycleScope)



    }

}