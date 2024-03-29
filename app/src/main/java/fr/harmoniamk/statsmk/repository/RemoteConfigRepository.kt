package fr.harmoniamk.statsmk.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

interface RemoteConfigRepositoryInterface {
    val loadConfig: Flow<Unit>
    val minimumVersion: Int
    val multiTeamEnabled: Boolean
}

@Module
@InstallIn(SingletonComponent::class)
interface RemoteConfigRepositoryModule {
    @Singleton
    @Binds
    fun bind(impl: RemoteConfigRepository): RemoteConfigRepositoryInterface
}

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteConfigRepository @Inject constructor() : RemoteConfigRepositoryInterface {

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    override val loadConfig
        get() = callbackFlow {
            val config = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            remoteConfig.setConfigSettingsAsync(config)
            remoteConfig
                .fetch(0)
                .addOnCompleteListener {
                    remoteConfig.activate()
                    if (isActive) offer(Unit)
                }
            awaitClose { }
        }

    override val minimumVersion: Int
        get() = remoteConfig.getString("minimum_version").toIntOrNull() ?: 0

    override val multiTeamEnabled: Boolean
        get() = remoteConfig.getBoolean("multi_team")
}