package com.powergrid.exemployee.di

import com.powergrid.exemployee.data.repository.AuthRepositoryImpl
import com.powergrid.exemployee.data.repository.EmployeeRepositoryImpl
import com.powergrid.exemployee.domain.repository.AuthRepository
import com.powergrid.exemployee.domain.repository.EmployeeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindEmployeeRepository(impl: EmployeeRepositoryImpl): EmployeeRepository
}
