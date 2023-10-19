package com.onandor.notemanager.data

import com.onandor.notemanager.data.local.db.LabelDao
import com.onandor.notemanager.data.mapping.toExternal
import com.onandor.notemanager.data.mapping.toLocal
import com.onandor.notemanager.di.ApplicationScope
import com.onandor.notemanager.di.DefaultDispatcher
import com.onandor.notemanager.utils.LabelColor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class LabelRepository @Inject constructor(
    private val localDataSource: LabelDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
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

    override suspend fun createLabel(title: String, color: LabelColor): UUID {
        val labelId = withContext(dispatcher) {
            UUID.randomUUID()
        }
        val label = Label(
            id = labelId,
            title = title,
            color = color
        )
        localDataSource.upsert(label.toLocal())
        return labelId
    }

    override suspend fun updateLabel(labelId: UUID, title: String, color: LabelColor) {
        val label = getLabel(labelId)?.copy(
            title = title,
            color = color
        ) ?: throw Exception("Label (id $labelId) not found in local database")
        localDataSource.upsert(label.toLocal())
    }

    override suspend fun deleteLabel(labelId: UUID) {
        localDataSource.deleteById(labelId)
    }

    override suspend fun deleteAllLocal() {
        localDataSource.deleteAll()
    }


}