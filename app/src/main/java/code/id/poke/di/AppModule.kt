package code.id.poke.di

import code.id.poke.data.local.SessionManager
import code.id.poke.data.repository.PokeRepositoryImpl
import code.id.poke.data.repository.UserRepositoryImpl
import code.id.poke.domain.repository.PokeRepository
import code.id.poke.domain.repository.UserRepository
import code.id.poke.ui.PokeViewModel
import code.id.poke.ui.auth.AuthViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single { SessionManager(get()) }
    single<PokeRepository> { PokeRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { PokeViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
}

val appModule: List<Module> = listOf(
    networkModule,
    databaseModule,
    repositoryModule,
    viewModelModule
)
