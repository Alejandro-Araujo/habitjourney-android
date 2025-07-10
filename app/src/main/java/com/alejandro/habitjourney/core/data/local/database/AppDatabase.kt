package com.alejandro.habitjourney.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.note.data.dao.NoteDao
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import com.alejandro.habitjourney.features.habit.data.entity.HabitEntity
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.user.data.entity.UserEntity

/**
 * La base de datos principal de la aplicación que utiliza Room.
 *
 * Esta clase abstracta extiende [RoomDatabase] y actúa como el punto de acceso central
 * para la persistencia de datos locales. Define las entidades que componen la base de datos,
 * la versión del esquema y los [TypeConverters] necesarios.
 *
 * @property version La versión actual del esquema de la base de datos. Debe incrementarse
 * al realizar cambios en el esquema para activar la migración.
 * @property entities La lista de clases de entidad que forman parte de la base de datos.
 * @property exportSchema Indica si se debe exportar el esquema a un archivo JSON en el
 * directorio del proyecto, lo cual es útil para las migraciones.
 */
@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        TaskEntity::class,
        NoteEntity::class,
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al Data Access Object (DAO) para las operaciones de [UserEntity].
     * @return Una instancia de [UserDao].
     */
    abstract fun userDao(): UserDao

    /**
     * Proporciona acceso al Data Access Object (DAO) para las operaciones de [HabitEntity].
     * @return Una instancia de [HabitDao].
     */
    abstract fun habitDao(): HabitDao

    /**
     * Proporciona acceso al Data Access Object (DAO) para las operaciones de [HabitLogEntity].
     * @return Una instancia de [HabitLogDao].
     */
    abstract fun habitLogDao(): HabitLogDao

    /**
     * Proporciona acceso al Data Access Object (DAO) para las operaciones de [TaskEntity].
     * @return Una instancia de [TaskDao].
     */
    abstract fun taskDao(): TaskDao

    /**
     * Proporciona acceso al Data Access Object (DAO) para las operaciones de [NoteEntity].
     * @return Una instancia de [NoteDao].
     */
    abstract fun noteDao(): NoteDao

    /**
     * Contiene constantes relacionadas con la base de datos.
     */
    companion object {
        /**
         * El nombre del archivo de la base de datos.
         */
        const val DATABASE_NAME = "habitjourney_db"
    }
}
