package code.id.poke.di

import androidx.room.Room
import code.id.poke.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "poke_database"
        ).fallbackToDestructiveMigration(true).build()
    }

    single { get<AppDatabase>().pokemonDao() }
    single { get<AppDatabase>().userDao() }
}
