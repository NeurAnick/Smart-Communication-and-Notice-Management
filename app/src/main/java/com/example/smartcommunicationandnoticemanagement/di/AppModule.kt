package com.example.smartcommunicationandnoticemanagement.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.smartcommunicationandnoticemanagement.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideFAQRepository(
        firestore: FirebaseFirestore
    ): FAQRepository = FAQRepository(firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository = AuthRepository(auth, firestore)

    @Provides
    @Singleton
    fun provideNoticeRepository(
        firestore: FirebaseFirestore
    ): NoticeRepository = NoticeRepository(firestore)

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore
    ): UserRepository = UserRepository(firestore)

    @Provides
    @Singleton
    fun provideChatRepository(
        database: FirebaseDatabase
    ): ChatRepository = ChatRepository(database)

    @Provides
    @Singleton
    fun provideRoutineRepository(
        firestore: FirebaseFirestore
    ): RoutineRepository = RoutineRepository(firestore)

    @Provides
    @Singleton
    fun provideRealtimeNotificationListener(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        database: FirebaseDatabase
    ): com.example.smartcommunicationandnoticemanagement.utils.RealtimeNotificationListener {
        return com.example.smartcommunicationandnoticemanagement.utils.RealtimeNotificationListener(context, firestore, database)
    }
}
