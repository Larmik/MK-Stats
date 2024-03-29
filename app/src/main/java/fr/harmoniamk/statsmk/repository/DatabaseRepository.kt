package fr.harmoniamk.statsmk.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmoniamk.statsmk.database.entities.TopicEntity
import fr.harmoniamk.statsmk.datasource.TeamLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.TopicLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.UserLocalDataSourceInterface
import fr.harmoniamk.statsmk.datasource.WarLocalDataSourceInterface
import fr.harmoniamk.statsmk.model.firebase.*
import fr.harmoniamk.statsmk.model.local.MKWar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

interface DatabaseRepositoryInterface {

    fun getUsers(): Flow<List<User>>
    fun getTeams(): Flow<List<Team>>
    fun getWars(): Flow<List<MKWar>>
    fun getTopics(): Flow<List<TopicEntity>>

    fun getUser(id: String?): Flow<User?>
    fun getTeam(id: String?): Flow<Team?>
    fun getWar(id: String?): Flow<MKWar?>

    fun writeUsers(list: List<User>): Flow<Unit>
    fun writeTeams(list: List<Team>): Flow<Unit>
    fun writeWars(list: List<MKWar>): Flow<Unit>

    fun writeUser(user: User): Flow<Unit>
    fun writeTeam(team: Team): Flow<Unit>
    fun writeWar(war: MKWar): Flow<Unit>
    fun writeTopic(topic: TopicEntity): Flow<Unit>

    fun deleteUser(user: User): Flow<Unit>
    fun deleteTeam(team: Team): Flow<Unit>
    fun deleteWar(war: MKWar): Flow<Unit>
    fun deleteTopic(topic: String): Flow<Unit>

    fun clearWars(): Flow<Unit>
    fun clearUsers(): Flow<Unit>
    fun clearTeams(): Flow<Unit>
}

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
interface DatabaseRepositoryModule {
    @Binds
    @Singleton
    fun bindRepository(impl: DatabaseRepository): DatabaseRepositoryInterface
}

@FlowPreview
@ExperimentalCoroutinesApi
class DatabaseRepository @Inject constructor(private val userDataSource: UserLocalDataSourceInterface, private val teamDataSource: TeamLocalDataSourceInterface, private val warDataSource: WarLocalDataSourceInterface, private val topicDataSource: TopicLocalDataSourceInterface) : DatabaseRepositoryInterface {


    override fun getUsers(): Flow<List<User>> = userDataSource.getAll()

    override fun getTeams(): Flow<List<Team>> = teamDataSource.getAll()

    override fun getWars(): Flow<List<MKWar>>  = warDataSource.getAll()

    override fun getTopics(): Flow<List<TopicEntity>> = topicDataSource.getAll()

    override fun getUser(id: String?): Flow<User?> = id?.let { userDataSource.getById(it) } ?: flowOf(null)

    override fun getTeam(id: String?): Flow<Team?> = id?.let { teamDataSource.getById(it) } ?: flowOf(null)

    override fun getWar(id: String?): Flow<MKWar?> = id?.let { warDataSource.getById(it) } ?: flowOf(null)

    override fun writeUsers(list: List<User>) = userDataSource.insert(list)

    override fun writeTeams(list: List<Team>) = teamDataSource.insert(list)

    override fun writeWars(list: List<MKWar>): Flow<Unit> = warDataSource.insert(list)

    override fun writeUser(user: User): Flow<Unit> = userDataSource.insert(user)

    override fun writeTeam(team: Team): Flow<Unit> = teamDataSource.insert(team)

    override fun writeWar(war: MKWar): Flow<Unit> = warDataSource.insert(war)

    override fun writeTopic(topic: TopicEntity): Flow<Unit> = topicDataSource.insert(topic)

    override fun deleteUser(user: User): Flow<Unit> = userDataSource.delete(user)

    override fun deleteTeam(team: Team): Flow<Unit> = teamDataSource.delete(team)

    override fun deleteWar(war: MKWar): Flow<Unit> = warDataSource.delete(war)

    override fun deleteTopic(topic: String): Flow<Unit> = topicDataSource.delete(topic)

    override fun clearWars(): Flow<Unit> = warDataSource.clear()

    override fun clearUsers(): Flow<Unit> = userDataSource.clear()

    override fun clearTeams(): Flow<Unit> = teamDataSource.clear()

}