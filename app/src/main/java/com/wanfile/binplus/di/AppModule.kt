package com.wanfile.binplus.di

import android.content.Context
import androidx.room.Room
import com.wanfile.binplus.data.converter.BinConverter
import com.wanfile.binplus.data.local.AppDatabase
import com.wanfile.binplus.data.local.ConvertRecordDao
import com.wanfile.binplus.data.local.ConvertRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideConvertRecordDao(db: AppDatabase): ConvertRecordDao = db.convertRecordDao()

    @Provides
    @Singleton
    fun provideRepository(dao: ConvertRecordDao): ConvertRepository =
        ConvertRepository(dao)

    @Provides
    @Singleton
    fun provideBinConverter(@ApplicationContext context: Context): BinConverter =
        BinConverter(context)
}
