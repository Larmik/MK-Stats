package fr.harmoniamk.statsmk.fragment.currentWar

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentCurrentWarBinding
import fr.harmoniamk.statsmk.extension.backPressedDispatcher
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.isResumed
import fr.harmoniamk.statsmk.fragment.addPenalty.AddPenaltyFragment
import fr.harmoniamk.statsmk.fragment.popup.PopupFragment
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CurrentWarFragment : Fragment(R.layout.fragment_current_war) {

    private val binding : FragmentCurrentWarBinding by viewBinding()
    private val viewModel: CurrentWarViewModel by viewModels()
    private var war: MKWar? = null
    private val popup by lazy { PopupFragment("Êtes-vous sûr de vouloir supprimer le match ?", "Supprimer")}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CurrentWarTrackAdapter()
        val penaltiesAdapter = PenaltyAdapter()
        binding.currentTracksRv.adapter = adapter
        binding.penaltiesRv.adapter = penaltiesAdapter
        viewModel.bind(
            onBack = requireActivity().backPressedDispatcher(viewLifecycleOwner),
            onNextTrack = binding.nextTrackBtn.clicks(),
            onTrackClick = adapter.sharedClick,
            onDelete = popup.onPositiveClick,
            onPopup = flowOf(popup.onNegativeClick.map { false }, binding.deleteWarBtn.clicks().map { true }).flattenMerge(),
            onPenalty = binding.penaltyBtn.clicks()
        )

        viewModel.sharedCurrentWar
            .filter { lifecycle.isResumed }
            .onEach {
                war = it
                binding.warTitleTv.text = it.name
                binding.warDateTv.text = it.war?.createdDate
                binding.currentWarTv.text = it.displayedState
                binding.scoreTv.text = it.displayedScore
                binding.diffScoreTv.text = it.displayedDiff
                val textColor = when  {
                    it.displayedDiff.contains("-") -> R.color.lose
                    it.displayedDiff.contains("+") -> R.color.green
                    else -> R.color.harmonia_dark
                }
                binding.diffScoreTv.setTextColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requireContext().getColor(textColor)
                        else ContextCompat.getColor(requireContext(), textColor)
                )
            }.launchIn(lifecycleScope)

        viewModel.sharedButtonVisible
            .filter { lifecycle.isResumed }
            .onEach {
                binding.nextTrackBtn.isVisible = it
                binding.deleteWarBtn.isVisible = it
            }.launchIn(lifecycleScope)

        viewModel.sharedQuit
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().popBackStack() }
            .launchIn(lifecycleScope)
        viewModel.sharedBackToWars
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.backToWars()) }
            .launchIn(lifecycleScope)

        viewModel.sharedSelectTrack
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .mapNotNull { war?.war?.mid }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.addTrack(it)) }
            .launchIn(lifecycleScope)

        viewModel.sharedTracks
            .filter { lifecycle.isResumed }
            .onEach {
                binding.playedLabel.isVisible = it.isNotEmpty()
                binding.emptyTrack.isVisible = it.isEmpty()
                adapter.addTracks(it)
            }
            .launchIn(lifecycleScope)


        viewModel.sharedWarPlayers
            .filter { lifecycle.isResumed }
            .onEach {
                it.forEachIndexed { index, s ->
                    when (index) {
                        0 ->  {
                            binding.player1.text = s.first
                            binding.player1score.text = s.second.toString()
                        }
                        1 ->  {
                            binding.player2.text = s.first
                            binding.player2score.text = s.second.toString()
                        }
                        2 ->  {
                            binding.player3.text = s.first
                            binding.player3score.text = s.second.toString()
                        }
                        3 ->  {
                            binding.player4.text = s.first
                            binding.player4score.text = s.second.toString()
                        }
                        4 ->  {
                            binding.player5.text = s.first
                            binding.player5score.text = s.second.toString()
                        }
                        else ->  {
                            binding.player6.text = s.first
                            binding.player6score.text = s.second.toString()
                        }

                    }
                }
            }.launchIn(lifecycleScope)

        viewModel.sharedTrackClick
            .filter { findNavController().currentDestination?.id == R.id.currentWarFragment }
            .onEach { findNavController().navigate(CurrentWarFragmentDirections.toTrackDetails(war = war?.war, index = it)) }
            .launchIn(lifecycleScope)

        viewModel.sharedPopupShowing
            .onEach {
                when (it) {
                    true -> popup.takeIf { !it.isAdded }?.show(childFragmentManager, null)
                    else -> popup.dismiss()
                }
            }
            .launchIn(lifecycleScope)

        viewModel.sharedAddPenalty
            .onEach {
                val penaltyFragment = AddPenaltyFragment(it)
                if (!penaltyFragment.isAdded)
                    penaltyFragment.show(childFragmentManager, null)
            }.launchIn(lifecycleScope)

        viewModel.sharedPenalties
            .filterNotNull()
            .onEach {
                binding.penaltiesLayout.isVisible = true
                penaltiesAdapter.addPenalties(it)
            }
            .launchIn(lifecycleScope)
    }

}