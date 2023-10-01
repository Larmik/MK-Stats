package fr.harmoniamk.statsmk.repository.mock

import fr.harmoniamk.statsmk.model.firebase.NewWar
import fr.harmoniamk.statsmk.model.firebase.NewWarTrack
import fr.harmoniamk.statsmk.model.firebase.Team
import fr.harmoniamk.statsmk.repository.PreferencesRepositoryInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PreferencesRepositoryMock : PreferencesRepositoryInterface {
    override var currentTeam: Team?
        get() = null
        set(value) {}
    override var currentWar: NewWar?
        get() = null
        set(value) {}
    override var currentWarTrack: NewWarTrack?
        get() = null
        set(value) {}
    override var currentTheme: Int
        get() = 0
        set(value) {}
    override var authEmail: String?
        get() = null
        set(value) {}
    override var authPassword: String?
        get() = null
        set(value) {}
    override var firstLaunch: Boolean
        get() = false
        set(value) {}
    override var indivEnabled: Boolean
        get() = true
        set(value) {}
    override var fcmToken: String?
        get() = null
        set(value) {}
}