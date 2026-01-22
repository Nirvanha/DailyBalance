package com.dailybalance.app.di

import android.content.Context
import androidx.room.Room
import com.dailybalance.app.data.ActionRecordRepository
import com.dailybalance.app.data.AppDatabase
import com.dailybalance.app.data.DailyExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideActionRecordRepository(db: AppDatabase): ActionRecordRepository {
        return ActionRecordRepository(db.actionRecordDao())
    }

    @Provides
    @Singleton
    fun provideDailyExpenseRepository(db: AppDatabase): DailyExpenseRepository {
        return DailyExpenseRepository(db.dailyExpenseDao())
    }
}
