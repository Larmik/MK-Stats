package fr.harmoniamk.statsmk.features.welcome.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.databinding.FragmentConnectUserBinding
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.clicks
import fr.harmoniamk.statsmk.extension.onTextChanged
import fr.harmoniamk.statsmk.features.welcome.viewmodel.ConnectUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ConnectUserFragment(val onFinish: MutableSharedFlow<Unit>, val onNoCode: MutableSharedFlow<Unit>): Fragment(R.layout.fragment_connect_user) {

    private val binding: FragmentConnectUserBinding by viewBinding()
    private val viewModel: ConnectUserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bind(
            binding.nameCodeEt.onTextChanged(),
            binding.alreadySubBtn.clicks()
        )

        viewModel.sharedNext.bind(onFinish, lifecycleScope)
        viewModel.sharedNoCode.bind(onNoCode, lifecycleScope)
    }

}