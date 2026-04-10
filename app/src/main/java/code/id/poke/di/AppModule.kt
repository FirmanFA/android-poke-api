package code.id.poke.di

import code.id.poke.data.local.SessionManager
import code.id.poke.data.repository.PokeRepositoryImpl
import code.id.poke.data.repository.UserRepositoryImpl
import code.id.poke.domain.repository.PokeRepository
import code.id.poke.domain.repository.UserRepository
import code.id.poke.ui.PokeViewModel
import code.id.poke.ui.auth.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SessionManager(androidContext()) }
    single<PokeRepository> { PokeRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }

    viewModel { PokeViewModel(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
