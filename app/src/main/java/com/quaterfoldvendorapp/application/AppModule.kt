package com.quaterfoldvendorapp.application

import com.quaterfoldvendorapp.domain.ApiViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val AppModule = module {

    single { createLoginUseCase(get()) }
    viewModel { ApiViewModel(get()) }
}