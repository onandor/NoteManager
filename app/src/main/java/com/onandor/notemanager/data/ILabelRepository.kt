package com.onandor.notemanager.data

import com.onandor.notemanager.utils.LabelColor
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ILabelRepository {

    fun getLabelStream(labelId: UUID): Flow<Label?>

    fun getLabelsStream(): Flow<List<Label>>

    suspend fun getLabel(labelId: UUID): Label?

    suspend fun getLabels(): List<Label>

    suspend fun createLabel(title: String, color: LabelColor): UUID

    suspend fun updateLabel(labelId: UUID, title: String, color: LabelColor)

    suspend fun deleteLabel(labelId: UUID)

    suspend fun deleteAllLocal()
}