package com.alejandro.habitjourney.features.note.di

import com.alejandro.habitjourney.features.note.data.repository.NoteRepositoryImpl
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import com.alejandro.habitjourney.features.note.domain.usecase.ArchiveNoteUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.CreateNoteUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.DeleteNoteUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.GetActiveNotesUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.GetAllNotesUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.GetArchivedNotesUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.GetFavoriteNotesUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.GetNoteByIdUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.GetNoteStatsUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.SearchNotesUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.ToggleFavoriteNoteUseCase
import com.alejandro.habitjourney.features.note.domain.usecase.UpdateNoteUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para la inyección de dependencias del feature de Notas.
 *
 * Se encarga de vincular la implementación del repositorio [NoteRepositoryImpl] a su interfaz
 * [NoteRepository] y de proveer todas las instancias de los casos de uso relacionados con las notas.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NoteModule {

    /**
     * Vincula la implementación [NoteRepositoryImpl] a la interfaz [NoteRepository].
     * Se usa @Binds para una mayor eficiencia en la generación de código por parte de Hilt.
     *
     * @param noteRepositoryImpl La implementación concreta del repositorio.
     * @return Una instancia que satisface la dependencia de [NoteRepository].
     */
    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryImpl
    ): NoteRepository

    /**
     * Contiene las funciones @Provides para los casos de uso.
     */
    companion object {
        @Provides
        fun provideCreateNoteUseCase(noteRepository: NoteRepository): CreateNoteUseCase =
            CreateNoteUseCase(noteRepository)

        @Provides
        fun provideUpdateNoteUseCase(noteRepository: NoteRepository): UpdateNoteUseCase =
            UpdateNoteUseCase(noteRepository)

        @Provides
        fun provideDeleteNoteUseCase(noteRepository: NoteRepository): DeleteNoteUseCase =
            DeleteNoteUseCase(noteRepository)

        @Provides
        fun provideGetNoteByIdUseCase(noteRepository: NoteRepository): GetNoteByIdUseCase =
            GetNoteByIdUseCase(noteRepository)

        @Provides
        fun provideGetActiveNotesUseCase(noteRepository: NoteRepository): GetActiveNotesUseCase =
            GetActiveNotesUseCase(noteRepository)

        @Provides
        fun provideGetAllNotesUseCase(noteRepository: NoteRepository): GetAllNotesUseCase =
            GetAllNotesUseCase(noteRepository)

        @Provides
        fun provideGetArchivedNotesUseCase(noteRepository: NoteRepository): GetArchivedNotesUseCase =
            GetArchivedNotesUseCase(noteRepository)

        @Provides
        fun provideGetFavoriteNotesUseCase(noteRepository: NoteRepository): GetFavoriteNotesUseCase =
            GetFavoriteNotesUseCase(noteRepository)

        @Provides
        fun provideSearchNotesUseCase(noteRepository: NoteRepository): SearchNotesUseCase =
            SearchNotesUseCase(noteRepository)

        @Provides
        fun provideArchiveNoteUseCase(noteRepository: NoteRepository): ArchiveNoteUseCase =
            ArchiveNoteUseCase(noteRepository)

        @Provides
        fun provideToggleFavoriteNoteUseCase(noteRepository: NoteRepository): ToggleFavoriteNoteUseCase =
            ToggleFavoriteNoteUseCase(noteRepository)

        @Provides
        fun provideGetNoteStatsUseCase(noteRepository: NoteRepository): GetNoteStatsUseCase =
            GetNoteStatsUseCase(noteRepository)
    }
}
