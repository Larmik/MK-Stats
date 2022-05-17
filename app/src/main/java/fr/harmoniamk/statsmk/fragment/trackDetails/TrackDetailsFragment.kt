package fr.harmoniamk.statsmk.fragment.trackDetails

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentTrackDetailsBinding
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.fragment.editWarTrack.EditWarTrackFragment
import fr.harmoniamk.statsmk.ui.TrackView
import fr.harmoniamk.statsmk.fragment.warTrackResult.WarTrackResultAdapter
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class TrackDetailsFragment : Fragment(R.layout.fragment_track_details) {

    private val binding: FragmentTrackDetailsBinding by viewBinding()
    private val viewModel: TrackDetailsViewModel by viewModels()
    private var war: NewWar? = null
    private var index: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        war = arguments?.get("war") as? NewWar
        index = arguments?.getInt("index")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WarTrackResultAdapter()
        binding.resultRv.adapter = adapter
        war?.let { war ->
                val item = MKWarTrack(war.warTracks?.get(index ?: 0))
                val trackView = TrackView(requireContext())
                trackView.bind(item.track?.trackIndex)
                binding.trackView.addView(trackView)
                binding.title.text = "${war.name}\nCourse ${(index ?: 0) + 1}/12"
                binding.trackScore.text = item.displayedResult
                binding.trackDiff.text = item.displayedDiff

                item.track?.let { viewModel.bind(it, binding.editTrackBtn.clicks(), binding.resetPositionsBtn.clicks()) }

                viewModel.sharedPositions
                    .onEach { adapter.addResults(it) }
                    .launchIn(lifecycleScope)

                viewModel.sharedEditTrackClick
                    .onEach {
                        val dialog = EditWarTrackFragment(war, index ?: 0)
                        if (!dialog.isAdded)
                            dialog.show(childFragmentManager, null)
                        dialog.onDismiss
                            .onEach {
                                dialog.dismiss()
                                trackView.bind(it)
                            }.launchIn(lifecycleScope)
                    }.launchIn(lifecycleScope)

            viewModel.sharedButtonsVisible
                .onEach {
                    binding.editTrackBtn.isVisible = it
                    binding.resetPositionsBtn.isVisible = it
                }.launchIn(lifecycleScope)
            }


        }

    }
