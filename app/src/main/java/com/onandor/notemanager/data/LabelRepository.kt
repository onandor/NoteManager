package com.onandor.notemanager.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.data.local.db.LabelDao
import com.onandor.notemanager.data.mapping.toExternal
import com.onandor.notemanager.data.mapping.toLocal
import com.onandor.notemanager.data.mapping.toRemote
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.LabelNotFound
import com.onandor.notemanager.data.remote.models.LocalLabelNotFound
import com.onandor.notemanager.data.remote.models.NotLoggedIn
import com.onandor.notemanager.data.remote.models.RemoteLabel
import com.onandor.notemanager.data.remote.sources.ILabelDataSource
import com.onandor.notemanager.di.ApplicationScope
import com.onandor.notemanager.di.DefaultDispatcher
import com.onandor.notemanager.utils.LabelColor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class LabelRepository @Inject constructor(
    private val localDataSource: LabelDao,
    private val remoteDataSource: ILabelDataSource,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
    private val settings: ISettings
) : ILabelRepository {

    override fun getLabelStream(labelId: UUID): Flow<Label?> {
        return localDataSource.observeById(labelId).map { it?.toExternal() }
    }

    override fun getLabelsStream(): Flow<List<Label>> {
        return localDataSource.observeAll().map { labels ->
            withContext(dispatcher) {
                labels.toExternal().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
            }
        }
    }

    override suspend fun getLabel(labelId: UUID): Label? {
        return localDataSource.getById(labelId)?.toExternal()
    }

    override suspend fun getLabels(): List<Label> {
        return withContext(dispatcher) {
            localDataSource.getAll().toExternal().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }
    }

    override suspend fun synchronizeSingle(labelId: UUID): Result<Unit, ApiError> {
        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId <= 0)
            return Err(NotLoggedIn)

        val localLabel = localDataSource.getById(labelId)?.toExternal()?.toRemote(userId)
            ?: return Err(LocalLabelNotFound)
        remoteDataSource.synchronize(localLabel)
        lateinit var remoteLabel: RemoteLabel
        remoteDataSource.getById(labelId)
            .onSuccess { _remoteLabel ->
                remoteLabel = _remoteLabel
            }
            .onFailure { apiError ->
                if (apiError == LabelNotFound) {
                    localDataSource.deleteById(labelId)
                }
                return Err(apiError)
            }
        if (localLabel.modificationDate == remoteLabel.modificationDate) {
            return Ok(Unit)
        }
        localDataSource.upsert(remoteLabel.toExternal().toLocal())
        return Ok(Unit)
    }

    override suspend fun synchronize(): Result<Unit, ApiError> {
        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId <= 0)
            return Err(NotLoggedIn)

        val localLabels = localDataSource.getAll().toExternal().toRemote(userId)
        remoteDataSource.synchronize(localLabels)
        lateinit var remoteLabels: List<RemoteLabel>
        remoteDataSource.getAll()
            .onSuccess { _remoteLabels ->
                remoteLabels = _remoteLabels
            }
            .onFailure { apiError ->
                return Err(apiError)
            }
        val modifiedLabels = remoteLabels.filterNot { remoteLabel ->
            localLabels.any { localLabel ->
                remoteLabel.id == localLabel.id
                        && remoteLabel.modificationDate == localLabel.modificationDate
            }
        }
        val deletedLabels = localLabels.filterNot { localLabel ->
            remoteLabels.any { remoteLabel -> localLabel.id == remoteLabel.id }
        }
        localDataSource.deleteAllSoftDeleted()
        deletedLabels.forEach { label -> localDataSource.deleteById(label.id) }
        localDataSource.upsertAll(modifiedLabels.toExternal().toLocal())
        return Ok(Unit)
    }

    override suspend fun createLabel(title: String, color: LabelColor): UUID {
        val labelId = withContext(dispatcher) {
            UUID.randomUUID()
        }
        val label = Label(
            id = labelId,
            title = title,
            color = color,
            deleted = false,
            creationDate = LocalDateTime.now(),
            modificationDate = LocalDateTime.now()
        )
        localDataSource.upsert(label.toLocal())

        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId > 0)
            remoteDataSource.create(label.toRemote(userId))
        return labelId
    }

    override suspend fun updateLabel(labelId: UUID, title: String, color: LabelColor) {
        val label = getLabel(labelId)?.copy(
            title = title,
            color = color,
            deleted = false,
            modificationDate = LocalDateTime.now()
        ) ?: throw Exception("Label (id $labelId) not found in local database")
        localDataSource.upsert(label.toLocal())

        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId > 0)
            remoteDataSource.update(label.toRemote(userId))
    }

    override suspend fun deleteLabel(labelId: UUID) {
        val userId = settings.getInt(SettingsKeys.USER_ID)
        var remoteDeleteFailed = false
        if (userId > 0) {
            remoteDataSource.delete(labelId)
                .onSuccess {
                    localDataSource.deleteById(labelId)
                }
                .onFailure {
                    remoteDeleteFailed = true
                }
        } else if (userId <= 0 || remoteDeleteFailed) {
            var label = localDataSource.getById(labelId) ?: return
            label = label.copy(deleted = true)
            localDataSource.upsert(label)
        }
    }

    override suspend fun deleteAllLocal() {
        localDataSource.deleteAll()
    }
}