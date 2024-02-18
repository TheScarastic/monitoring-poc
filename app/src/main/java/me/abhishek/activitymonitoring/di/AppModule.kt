package me.abhishek.activitymonitoring.di


import android.content.Context
import android.hardware.SensorManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun providesSensorManager(@ApplicationContext context: Context): SensorManager =
        context.getSystemService(SensorManager::class.java)

    @Singleton
    @Provides
    fun providesFirebaseFirestore() = Firebase.firestore
}