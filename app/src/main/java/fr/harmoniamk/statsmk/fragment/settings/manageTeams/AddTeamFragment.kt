package fr.harmoniamk.statsmk.fragment.settings.manageTeams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.databinding.FragmentAddTeamBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddTeamFragment(val teamWithLeader: Boolean = false) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentAddTeamBinding
    private val viewModel: AddTeamViewModel by viewModels()

    val onTeamAdded = MutableSharedFlow<Unit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            teamWithLeader,
            binding.teamnameEt.onTextChanged(),
            binding.shortnameEt.onTextChanged(),
            binding.nextBtn.clicks()
        )
        viewModel.sharedTeamAdded.bind(onTeamAdded, lifecycleScope)
        viewModel.sharedButtonEnabled
            .onEach { binding.nextBtn.isEnabled = it }
            .launchIn(lifecycleScope)
    }

}