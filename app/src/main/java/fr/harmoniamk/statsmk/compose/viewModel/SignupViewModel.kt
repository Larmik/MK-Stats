package fr.harmoniamk.statsmk.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.model.firebase.AuthUserResponse
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepositoryInterface,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface
) : ViewModel() {

    private val _sharedNext = MutableSharedFlow<Unit>()
    private val _sharedToast = MutableSharedFlow<String>()
    private val _sharedDialogValue = MutableStateFlow<MKDialogState?>(null)

    val sharedNext = _sharedNext.asSharedFlow()
    val sharedDialogValue = _sharedDialogValue.asStateFlow()
    val sharedToast = _sharedToast.asSharedFlow()

    fun onSignup(email: String, password: String, name: String, fc: String) {
        _sharedDialogValue.value = MKDialogState.Loading(R.string.creating_user)
        val imageUrl = "https://firebasestorage.googleapis.com/v0/b/stats-mk.appspot.com/o/mk_stats_logo.png?alt=media&token=930c6fdb-9e42-4b23-a9de-3c069d2f982b"
        val createUser = authenticationRepository.createUser(email, password).shareIn(viewModelScope, SharingStarted.Lazily)
        createUser
            .mapNotNull { (it as? AuthUserResponse.Success)?.user?.uid }
            .mapNotNull { name }
            .flatMapLatest { authenticationRepository.updateProfile(it, imageUrl) }
            .mapNotNull { authenticationRepository.user }
            .map { User(it, imageUrl) }
            .onEach {
                preferencesRepository.authEmail = email
                preferencesRepository.authPassword = password
                preferencesRepository.firstLaunch = false
            }
            .flatMapLatest { firebaseRepository.writeUser(it) }
            .onEach { _sharedDialogValue.value = null }
            .bind(_sharedNext, viewModelScope)

        createUser
            .mapNotNull { (it as? AuthUserResponse.Error)?.message }
            .onEach { _sharedDialogValue.value = null }
            .bind(_sharedToast, viewModelScope)
    }

}