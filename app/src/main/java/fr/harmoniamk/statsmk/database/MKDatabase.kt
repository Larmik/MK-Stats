package fr.harmoniamk.statsmk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.harmoniamk.statsmk.database.dao.PlayedTrackDao
import fr.harmoniamk.statsmk.database.dao.TournamentDao
import fr.harmoniamk.statsmk.model.local.MKTournamentTrack
import fr.harmoniamk.statsmk.model.local.MKTournament
import kotlinx.coroutines.FlowPreview

@Database(entities = [MKTournament::class, MKTournamentTrack::class], version = 1)
abstract class MKDatabase : RoomDatabase() {

    abstract fun tournamentDao(): TournamentDao
    abstract fun playedTrackDao(): PlayedTrackDao

    @FlowPreview
    companion object {

        @Volatile
        private var instance: MKDatabase? = null

        @Synchronized
        fun getInstance(context: Context): MKDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    MKDatabase::class.java,
                    "mk_db"
                ).allowMainThreadQueries()
                    .build()

                instance = newInstance
                newInstance
            }
        }
    }
}