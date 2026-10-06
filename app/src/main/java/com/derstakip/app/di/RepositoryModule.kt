package com.derstakip.app.di

import com.derstakip.app.data.repository.PlaylistRepositoryImpl
import com.derstakip.app.data.repository.ScheduleRepositoryImpl
import com.derstakip.app.domain.repository.PlaylistRepository
import com.derstakip.app.domain.repository.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository
}
