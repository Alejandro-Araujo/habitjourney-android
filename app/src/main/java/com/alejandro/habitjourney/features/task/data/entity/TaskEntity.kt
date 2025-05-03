package com.alejandro.habitjourney.features.task.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.user.data.entity.UserEntity
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE)],
    indices = [
        Index("user_id"),
        Index("is_completed"),
        Index("due_date")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: LocalDate? = null,

    @ColumnInfo(name = "priority")
    val priority: Priority? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)