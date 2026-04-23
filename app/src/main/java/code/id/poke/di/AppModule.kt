package code.id.poke.di

import code.id.poke.data.local.SessionManager
import code.id.poke.data.remote.PokemonRemoteMediator
import code.id.poke.data.repository.PokeRepositoryImpl
import code.id.poke.data.repository.UserRepositoryImpl
import code.id.poke.domain.repository.PokeRepository
import code.id.poke.domain.repository.UserRepository
import code.id.poke.domain.usecase.GetPokemonDetailUseCase
import code.id.poke.domain.usecase.GetPokemonListUseCase
import code.id.poke.domain.usecase.LoginUseCase
import code.id.poke.domain.usecase.RegisterUseCase
import code.id.poke.domain.usecase.SearchPokemonUseCase
import code.id.poke.ui.MainViewModel
import code.id.poke.ui.PokemonDetailViewModel
import code.id.poke.ui.PokemonListViewModel
import code.id.poke.ui.auth.AuthViewModel
import code.id.poke.util.PasswordHasher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SessionManager(androidContext()) }
    single { PasswordHasher() }

    factory { PokemonRemoteMediator(get(), get()) }

    single<PokeRepository> { PokeRepositoryImpl(get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }

    single { GetPokemonListUseCase(get()) }
    single { GetPokemonDetailUseCase(get()) }
    single { SearchPokemonUseCase(get()) }
    single { LoginUseCase(get(), get()) }
    single { RegisterUseCase(get(), get()) }

    viewModel { MainViewModel(get()) }
    viewModel { PokemonListViewModel(get(), get()) }
    viewModel { PokemonDetailViewModel(get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
}
