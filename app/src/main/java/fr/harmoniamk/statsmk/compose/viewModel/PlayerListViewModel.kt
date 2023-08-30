package fr.harmoniamk.statsmk.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import fr.harmoniamk.statsmk.extension.bind
import fr.harmoniamk.statsmk.extension.isTrue
import fr.harmoniamk.statsmk.fragment.playerSelect.UserSelector
import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.User
import fr.harmoniamk.statsmk.repository.AuthenticationRepositoryInterface
import fr.harmoniamk.statsmk.repository.DatabaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.FirebaseRepositoryInterface
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PlayerListViewModel @AssistedInject constructor(
    @Assisted private val id: String,
    private val preferencesRepository: PreferencesRepositoryInterface,
    private val databaseRepository: DatabaseRepositoryInterface,
    private val authenticationRepository: AuthenticationRepositoryInterface,
    private val firebaseRepository: FirebaseRepositoryInterface
): ViewModel() {


    private val _sharedPlayers = MutableStateFlow<List<UserSelector>?>(null)
    private val _sharedUsersSelected = MutableStateFlow<List<User>?>(null)
    private val _sharedOfficial = MutableSharedFlow<Boolean>()
    private val _sharedButtonEnabled = MutableSharedFlow<Boolean>()
    private val _sharedWarName = MutableStateFlow<String?>(null)

    val sharedButtonEnabled = _sharedButtonEnabled.asSharedFlow()
    val sharedPlayers = _sharedPlayers.asStateFlow()
    val sharedWarName = _sharedWarName.asStateFlow()
    val sharedUsersSelected = _sharedUsersSelected.asStateFlow()
    val sharedOfficial = _sharedOfficial.asSharedFlow()


    private val _sharedStarted = MutableSharedFlow<Unit>()
    private val _sharedTeamSelected = MutableSharedFlow<String?>()
    private val _sharedAlreadyCreated = MutableSharedFlow<Unit>()
    private val _sharedLoading = MutableStateFlow<Boolean>(false)

    val sharedStarted = _sharedStarted.asSharedFlow()
    val sharedTeamSelected = _sharedTeamSelected.asSharedFlow()
    val sharedAlreadyCreated = _sharedAlreadyCreated.asSharedFlow()
    val sharedLoading = _sharedLoading.asSharedFlow()

    val usersSelected = mutableListOf<User>()
    val date = SimpleDateFormat("dd/MM/yyyy - HH'h'mm", Locale.FRANCE).format(Date())



    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            id: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(id) as T
            }
        }

        @Composable
        fun viewModel(id: String?): PlayerListViewModel {
            val factory: Factory = EntryPointAccessors.fromApplication<ViewModelFactoryProvider>(context = LocalContext.current).playerListViewModel
            return androidx.lifecycle.viewmodel.compose.viewModel(
                factory = provideFactory(
                    assistedFactory = factory,
                    id = id
                )
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(id: String?): PlayerListViewModel
    }

    fun selectUser(user: UserSelector) {
        val temp = mutableListOf<UserSelector>()
        _sharedPlayers.value?.forEach {
            when (it.user?.mid == user.user?.mid) {
                true -> temp.add(user)
                else -> temp.add(it)
            }
        }
        _sharedPlayers.value = temp
    }

    fun createWar() {
        _sharedLoading.value = true
        val war =  NewWar(
            mid = System.currentTimeMillis().toString(),
            teamHost = preferencesRepository.currentTeam?.mid,
            playerHostId = authenticationRepository.user?.uid,
            teamOpponent = id,
            createdDate = date,
            isOfficial = false
        )
        viewModelScope.launch {
            _sharedPlayers.value?.filter { it.isSelected.isTrue }?.mapNotNull { it.user }?.forEach { user ->
                val new = user.apply { this.currentWar = war.mid }
                firebaseRepository.writeUser(new).first()
            }
            preferencesRepository.currentWar = war
            firebaseRepository.writeCurrentWar(war).first()
            _sharedStarted.emit(Unit)
        }
    }

    init {
        databaseRepository.getUsers()
            .map { list ->
                val temp = mutableListOf<UserSelector>()
               // temp.add(UserSelector(isCategory = true))
                temp.addAll(list.filter { user -> user.team == preferencesRepository.currentTeam?.mid }
                    .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                    .map { UserSelector(it, false) })
               // temp.add(UserSelector(isCategory = true))
                //temp.addAll(list.filterNot { user -> user.team == preferencesRepository.currentTeam?.mid }
                  //  .sortedBy { it.name?.toLowerCase(Locale.ROOT) }
                   // .map { UserSelector(it, false) })
                temp
            }.bind(_sharedPlayers, viewModelScope)

        databaseRepository.getTeam(id)
            .onEach {
                _sharedWarName.value = "${preferencesRepository.currentTeam?.shortName} - ${it?.shortName}"
            }.launchIn(viewModelScope)
    }

}