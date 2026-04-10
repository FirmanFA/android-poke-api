package code.id.poke

import android.app.Application
import code.id.poke.di.appModule
import code.id.poke.di.databaseModule
import code.id.poke.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PokeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@PokeApplication)
            modules(appModule, databaseModule, networkModule)
        }
    }
}
