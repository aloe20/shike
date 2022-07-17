package com.aloe.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.aloe.proto.Banner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
internal class LocalImpl @Inject constructor(@ApplicationContext val ctx: Context) : ILocal {
  private val settingsDataStore: DataStore<Preferences> =
    preferencesDataStore(name = "settings").getValue(ctx, Preferences::javaClass)

  override suspend fun getAssetsStr(name: String): String {
    return withContext(Dispatchers.IO) {
      async(Dispatchers.IO) {
        StringBuilder().apply {
          BufferedReader(InputStreamReader(ctx.assets.open(name))).use {
            var line = it.readLine()
            while (line != null) {
              append(line)
              line = it.readLine()
            }
          }
        }
      }.await().toString()
    }
  }

  override suspend fun putPrivacyVisible(isVisible: Boolean) {
    settingsDataStore.edit {
      it[booleanPreferencesKey("privacy")] = isVisible
    }
  }

  override suspend fun getPrivacyVisible(): Flow<Boolean> = settingsDataStore.data.map {
    it[booleanPreferencesKey("privacy")] ?: true
  }

  override suspend fun putBanner(banner: List<Banner>) {
    ctx.bannerDataStore.updateData { it?.toBuilder()?.addAllBanners(banner)?.build() }
  }

  override suspend fun getBanner(): Flow<MutableList<Banner>?> = ctx.bannerDataStore.data.map { it?.bannersList }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
