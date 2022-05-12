package fr.harmoniamk.statsmk.fragment.trackDetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentTrackDetailsBinding
import fr.harmoniamk.statsmk.ui.TrackView
import fr.harmoniamk.statsmk.fragment.warTrackResult.WarTrackResultAdapter
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.local.MKWarTrack
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class TrackDetailsFragment : Fragment(R.layout.fragment_track_details) {

    private val binding: FragmentTrackDetailsBinding by viewBinding()
    private val viewModel: TrackDetailsViewModel by viewModels()
    private var warTrack: NewWarTrack? = null
    private var warName: String? = null
    private var number: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        warTrack = arguments?.get("warTrack") as? NewWarTrack
        warName = arguments?.getString("warName")
        number = arguments?.getInt("number")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = WarTrackResultAdapter()
        binding.resultRv.adapter = adapter
        warTrack?.let { track ->
            val item = MKWarTrack(track)
            val trackView = TrackView(requireContext())
            trackView.bind(track.trackIndex)
            binding.trackView.addView(trackView)
            binding.title.text = "$warName\nCourse $number/12"
            binding.trackScore.text = item.displayedResult
            binding.trackDiff.text = item.displayedDiff
            viewModel.bind(track)
            viewModel.sharedPositions
                .onEach { adapter.addResults(it) }
                .launchIn(lifecycleScope)

        }

    }

}