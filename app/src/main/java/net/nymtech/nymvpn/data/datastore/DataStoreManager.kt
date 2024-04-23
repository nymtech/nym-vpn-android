package net.nymtech.nymvpn.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException

class DataStoreManager(private val context: Context) {

	// preferences
	private val preferencesKey = "preferences"
	private val Context.dataStore by
		preferencesDataStore(
			name = preferencesKey,
		)

	suspend fun <T> saveToDataStore(key: Preferences.Key<T>, value: T) {
		try {
			context.dataStore.edit { it[key] = value }
		} catch (e: IOException) {
			Timber.e(e)
		}
	}

	fun <T> getFromStoreFlow(key: Preferences.Key<T>) = context.dataStore.data.map { it[key] }

	suspend fun <T> getFromStore(key: Preferences.Key<T>): T? {
		return try {
			context.dataStore.data.map { it[key] }.first()
		} catch (e: IOException) {
			Timber.e(e)
			null
		}
	}

	fun <T> getFromStoreBlocking(key: Preferences.Key<T>) = runBlocking {
		context.dataStore.data.map { it[key] }.first()
	}

	val preferencesFlow: Flow<Preferences?> = context.dataStore.data
}
