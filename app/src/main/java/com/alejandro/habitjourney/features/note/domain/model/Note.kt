package com.alejandro.habitjourney.features.note.domain.model

import com.alejandro.habitjourney.core.data.local.enums.NoteType
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Representa un único ítem dentro de una nota de tipo lista (CHECKLIST).
 *
 * Esta clase es serializable para poder ser almacenada como JSON en la base de datos.
 *
 * @property id Un identificador único para el ítem, generado automáticamente.
 * @property text El contenido de texto del ítem de la lista.
 * @property isCompleted `true` si el ítem ha sido marcado como completado.
 * @property indentLevel Nivel de sangría para soportar sub-listas (futura implementación).
 * @property order La posición del ítem en la lista para mantener el orden.
 */
@Serializable
data class NoteListItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val indentLevel: Int = 0,
    val order: Int
) {
    /**
     * Determina si el ítem de la lista se considera vacío.
     */
    val isEmpty: Boolean get() = text.isBlank()
}

/**
 * Representa el modelo de dominio de una nota.
 *
 * Esta es la clase central para la lógica de negocio de las notas, conteniendo
 * no solo sus datos sino también propiedades computadas para facilitar su uso en la UI.
 *
 * @property id El identificador único de la nota. Es 0 para notas nuevas.
 * @property userId El ID del usuario al que pertenece la nota.
 * @property title El título de la nota.
 * @property content El contenido principal para notas de tipo texto.
 * @property noteType El tipo de nota, según [NoteType] (TEXT o LIST).
 * @property listItems La lista de [NoteListItem] para notas de tipo CHECKLIST.
 * @property isArchived `true` si la nota está archivada.
 * @property createdAt Timestamp de la fecha de creación.
 * @property updatedAt Timestamp de la última modificación.
 * @property wordCount Conteo de palabras para estadísticas.
 * @property isFavorite `true` si la nota está marcada como favorita.
 */
data class Note(
    val id: Long = 0L,
    val userId: Long,
    val title: String,
    val content: String,
    val noteType: NoteType = NoteType.TEXT,
    val listItems: List<NoteListItem> = emptyList(),
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val wordCount: Int = 0,
    val isFavorite: Boolean = false
) {
    /**
     * Determina si la nota se considera vacía.
     * Para notas de texto, comprueba si título y contenido están en blanco.
     * Para notas de lista, comprueba si título y la lista de ítems están vacíos.
     */
    val isEmpty: Boolean get() = when (noteType) {
        NoteType.TEXT -> title.isBlank() && content.isBlank()
        NoteType.LIST -> title.isBlank() && listItems.isEmpty()
    }

    /**
     * Genera una vista previa del contenido de la nota.
     * Para notas de texto, muestra los primeros 100 caracteres.
     * Para notas de lista, muestra un resumen del conteo de elementos completados.
     */
    val preview: String get() = when (noteType) {
        NoteType.TEXT -> content.take(100).replace("\n", " ")
        NoteType.LIST -> {
            val completedCount = listItems.count { it.isCompleted }
            val totalCount = listItems.size
            "$completedCount/$totalCount elementos completados" // Esto debería ir en un string resource
        }
    }

    /**
     * Calcula el porcentaje de completitud, relevante solo para notas de tipo lista.
     * @return Un valor flotante entre 0.0 y 1.0. Devuelve 0f para notas de texto.
     */
    val completionPercentage: Float get() = when (noteType) {
        NoteType.TEXT -> 0f
        NoteType.LIST -> {
            if (listItems.isEmpty()) 0f
            else listItems.count { it.isCompleted }.toFloat() / listItems.size
        }
    }
}

/**
 * Representa las estadísticas agregadas sobre las notas del usuario.
 *
 * @property activeNotesCount El número total de notas activas (no archivadas).
 * @property totalWordCount La suma total de palabras de todas las notas activas.
 */
data class NoteStats(
    val activeNotesCount: Int,
    val totalWordCount: Int
)
