package com.alejandro.habitjourney.features.user.data.repository

import android.content.Context
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import com.alejandro.habitjourney.features.user.data.mapper.toDomainUser
import com.alejandro.habitjourney.features.user.data.mapper.toEntity
import com.alejandro.habitjourney.features.user.data.preferences.UserPreferences
import com.alejandro.habitjourney.features.user.domain.exception.ForceSignOutException
import com.alejandro.habitjourney.features.user.domain.exception.ReauthenticationRequiredException
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación concreta del [UserRepository] que gestiona las operaciones de usuario
 * combinando fuentes de datos locales (Room, DataStore) y remotas (Firebase Authentication).
 *
 * Se encarga de la lógica de registro, inicio de sesión, actualización, cambio de contraseña,
 * eliminación de usuario y cierre de sesión, así como de la persistencia local de los datos
 * del usuario y su estado de autenticación.
 *
 * @property context El contexto de la aplicación, inyectado por Hilt.
 * @property firebaseAuth La instancia de Firebase Authentication.
 * @property userDao El DAO de usuario para interactuar con la base de datos local (Room).
 * @property userPreferences El gestor de preferencias de usuario (DataStore) para tokens y User ID.
 * @property errorHandler El manejador de errores para procesar excepciones de Firebase y otras.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val errorHandler: ErrorHandler
) : UserRepository {

    /**
     * Registra un nuevo usuario en Firebase Authentication y lo guarda localmente.
     *
     * @param name El nombre del usuario.
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un [NetworkResponse] que indica el éxito o error de la operación, conteniendo el [User] si es exitoso.
     */
    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): NetworkResponse<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_registration_failed)))

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            firebaseUser.sendEmailVerification().await()
            AppLogger.d(
                "UserRepositoryImpl",
                "Email verification sent after registration to: $email"
            )

            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = firebaseUser.email ?: email
            )

            val userEntity = user.toEntity()
            userDao.insertUser(userEntity)
            userPreferences.saveUserId(user.id)

            NetworkResponse.Success(user)
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Inicia sesión con un usuario existente usando Firebase Authentication.
     * Guarda el token de autenticación y el ID de usuario en las preferencias locales.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un [NetworkResponse] con el [User] y el token de Firebase si el inicio de sesión es exitoso.
     */
    override suspend fun login(
        email: String,
        password: String
    ): NetworkResponse<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_login_failed)))

            val idToken = firebaseUser.getIdToken(true).await().token ?: ""
            val user = firebaseUser.toDomainUser()
            val userExists = userDao.userExists(user.id)
            if (!userExists) {
                val userEntity = user.toEntity()
                userDao.insertUser(userEntity)
            } else {
                userDao.updateUserInfo(user.id, user.name, user.email)
            }

            userPreferences.saveAuthToken(idToken)
            userPreferences.saveUserId(user.id)

            NetworkResponse.Success(user)
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Inicia sesión con Google utilizando Firebase Auth.
     *
     * @param idToken El token de ID de Google obtenido del GoogleSignInAccount
     * @return Un [NetworkResponse] con el [User] y el token si el inicio de sesión es exitoso.
     */
    override suspend fun signInWithGoogle(idToken: String): NetworkResponse<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_google_login_failed)))

            val firebaseToken = firebaseUser.getIdToken(true).await().token ?: ""

            val user = firebaseUser.toDomainUser()

            val userExists = userDao.userExists(user.id)
            if (!userExists) {
                val userEntity = user.toEntity()
                userDao.insertUser(userEntity)
            } else {
                userDao.updateUserInfo(user.id, user.name, user.email)
            }

            userPreferences.saveAuthToken(firebaseToken)
            userPreferences.saveUserId(user.id)

            NetworkResponse.Success(user)
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Obtiene los datos del usuario actual desde Firebase Authentication y la base de datos local.
     *
     * @return Un [NetworkResponse] con el [User] actual si la operación es exitosa.
     */
    override suspend fun getCurrentUser(): NetworkResponse<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            val localUser = userDao.getUserById(firebaseUser.uid).first()?.toDomainUser()

            if (localUser != null) {
                NetworkResponse.Success(localUser)
            } else {
                val user = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: ""
                )

                userDao.insertUser(user.toEntity())

                NetworkResponse.Success(user)
            }
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Actualiza la información (nombre y correo electrónico) del usuario en Firebase Authentication y en la base de datos local.
     *
     * @param name El nuevo nombre del usuario.
     * @param email El nuevo correo electrónico del usuario.
     * @return Un [NetworkResponse] con el [User] actualizado si la operación es exitosa.
     */
    override suspend fun updateUser(
        name: String,
        email: String
    ): NetworkResponse<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            if (name != firebaseUser.displayName) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                userDao.updateUserInfo(firebaseUser.uid, name, firebaseUser.email ?: "")
                AppLogger.d("UserRepositoryImpl", "Display name updated to: $name")
            }

            if (email != firebaseUser.email) {
                try {
                    firebaseUser.verifyBeforeUpdateEmail(email).await()
                    AppLogger.d(
                        "UserRepositoryImpl",
                        "Email verification sent to new email: $email"
                    )

                    throw ForceSignOutException(
                        message = context.getString(R.string.email_verification_sent_logout_required),
                    )

                } catch (e: FirebaseAuthRecentLoginRequiredException) {
                    AppLogger.e(
                        "UserRepositoryImpl",
                        "Reauthentication required for email update",
                        e
                    )
                    return NetworkResponse.Error(
                        ReauthenticationRequiredException(
                            message = context.getString(R.string.error_recent_login_required),
                            cause = e,
                        )
                    )
                }
            }

            val updatedUser = firebaseUser.toDomainUser()
            userDao.updateUserInfo(
                userId = updatedUser.id,
                name = updatedUser.name,
                email = updatedUser.email
            )

            NetworkResponse.Success(updatedUser)

        } catch (e: ForceSignOutException) {
            AppLogger.d("UserRepositoryImpl", "ForceSignOut required after email verification")
            throw e
        } catch (e: Exception) {
            AppLogger.e("UserRepositoryImpl", "Error updating user profile or email", e)
            NetworkResponse.Error(e)
        }
    }

    /**
     * Cambia la contraseña del usuario en Firebase Authentication.
     *
     * @param currentPassword La contraseña actual del usuario.
     * @param newPassword La nueva contraseña para el usuario.
     * @return Un [NetworkResponse] con un mensaje de éxito si la operación es exitosa.
     */
    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): NetworkResponse<String> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            val email = firebaseUser.email
            if (email.isNullOrEmpty()) {
                return NetworkResponse.Error(Exception(context.getString(R.string.error_email_empty)))
            }

            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            firebaseUser.reauthenticate(credential).await()

            firebaseUser.updatePassword(newPassword).await()

            NetworkResponse.Success(context.getString(R.string.password_changed_success))
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            NetworkResponse.Error(
                ReauthenticationRequiredException(
                    context.getString(R.string.error_recent_login_required),
                    cause = e,
                )
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            NetworkResponse.Error(Exception(context.getString(R.string.error_invalid_current_password)))
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Elimina la cuenta del usuario de forma permanente de Firebase Authentication.
     * Borra la cuenta en Firebase, elimina los datos del usuario de la base de datos local
     * y limpia las preferencias de sesión.
     *
     * @return Un [NetworkResponse] con un mensaje de éxito si la operación es exitosa.
     */
    override suspend fun deleteUser(): NetworkResponse<String> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            firebaseUser.delete().await()
            userPreferences.clear()
            userDao.deleteUser(firebaseUser.uid)

            NetworkResponse.Success(context.getString(R.string.user_deleted_successfully))
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Cierra la sesión actual del usuario en Firebase Authentication.
     * Limpia las credenciales de sesión y el ID de usuario de las preferencias locales.
     * Los datos de usuario en la base de datos local permanecen intactos.
     *
     * @return Un [NetworkResponse.Success] sin datos si el cierre de sesión es exitoso.
     */
    override suspend fun logout(): NetworkResponse<Unit> {
        firebaseAuth.signOut()
        userPreferences.clear()
        return NetworkResponse.Success(Unit)
    }

    /**
     * Observa el usuario local actual basándose en el ID de usuario almacenado en las preferencias.
     *
     * @return Un [Flow] que emite el [User] local o `null` si no hay usuario activo o no se encuentra.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLocalUser(): Flow<User?> {
        return userPreferences.userIdFlow.flatMapLatest { userId ->
            if (userId != null) {
                userDao.getUserById(userId).map { entity ->
                    entity?.toDomainUser()
                }
            } else {
                flowOf(null)
            }
        }
    }

    /**
     * Verifica si hay una sesión de usuario activa.
     * Una sesión se considera activa si hay un token de autenticación presente en las preferencias.
     *
     * @return Un [Flow] que emite `true` si hay una sesión activa, `false` en caso contrario.
     */
    override fun isLoggedIn(): Flow<Boolean> {
        return userPreferences.authTokenFlow.map { token ->
            !token.isNullOrEmpty()
        }
    }

    /**
     * Vincula una cuenta de Google a la cuenta del usuario actualmente autenticado en Firebase.
     *
     * @param idToken El token de ID de Google obtenido tras una autenticación exitosa con Google.
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la vinculación es exitosa.
     */
    override suspend fun linkGoogleAccount(idToken: String): NetworkResponse<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseUser.linkWithCredential(credential).await()
            val updatedUser = authResult.user?.toDomainUser()
                ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_linking_failed)))

            userDao.updateUserInfo(updatedUser.id, updatedUser.name, updatedUser.email)

            NetworkResponse.Success(updatedUser)
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Vincula credenciales de email/password a una cuenta existente de Google.
     *
     * @param email El correo electrónico a vincular.
     * @param password La contraseña a vincular.
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la vinculación es exitosa.
     */
    override suspend fun linkEmailPassword(email: String, password: String): NetworkResponse<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val authResult = firebaseUser.linkWithCredential(credential).await()
            val updatedUser = authResult.user?.toDomainUser()
                ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_linking_failed)))

            userDao.updateUserInfo(updatedUser.id, updatedUser.name, updatedUser.email)

            NetworkResponse.Success(updatedUser)
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Obtiene los métodos de autenticación vinculados al usuario actual.
     *
     * @return Una [List] de [String] que contiene los IDs de los proveedores de autenticación vinculados.
     */
    override suspend fun getLinkedAuthMethods(): List<String> {
        val firebaseUser = firebaseAuth.currentUser ?: return emptyList()

        return try {
            val providerData = firebaseUser.providerData
            providerData.map { it.providerId }.filter { it != "firebase" }
        } catch (e: Exception) {
            AppLogger.e("UserRepository", "Error obteniendo métodos vinculados: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Desvincula un método de autenticación específico de la cuenta del usuario actual.
     *
     * @param providerId El ID del proveedor de autenticación a desvincular (ej., "password", "google.com").
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la desvinculación es exitosa.
     */
    override suspend fun unlinkAuthMethod(providerId: String): NetworkResponse<User> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            val authResult = firebaseUser.unlink(providerId).await()
            val updatedUser = authResult.user?.toDomainUser()
                ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_unlinking_failed)))

            userDao.updateUserInfo(updatedUser.id, updatedUser.name, updatedUser.email)
            NetworkResponse.Success(updatedUser)
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Envía un correo electrónico de verificación a la dirección de correo del usuario actual.
     *
     * @return Un [NetworkResponse] indicando el resultado de la operación.
     */
    override suspend fun sendEmailVerification(): NetworkResponse<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_user_not_logged_in)))
            user.sendEmailVerification().await()
            NetworkResponse.Success(Unit)
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Reautentica al usuario actual con la credencial proporcionada.
     *
     * @param credential La credencial de autenticación obtenida del último inicio de sesión.
     * @return Un [NetworkResponse] indicando el resultado de la operación.
     */
    override suspend fun reauthenticateUser(credential: AuthCredential): NetworkResponse<Unit> {
        val firebaseUser = firebaseAuth.currentUser
            ?: return NetworkResponse.Error(Exception(context.getString(R.string.error_no_active_session)))

        return try {
            firebaseUser.reauthenticate(credential).await()
            NetworkResponse.Success(Unit)
        } catch (e: Exception) {
            AppLogger.e("UserRepository", "Error en reautenticación: ${e.message}", e)
            NetworkResponse.Error(e)
        }
    }

    /**
     * Reautentica al usuario actual utilizando credenciales de Google.
     *
     * @param idToken El token de ID de Google obtenido tras una autenticación exitosa con Google.
     * @return Un [NetworkResponse] indicando el resultado de la operación.
     */
    override suspend fun reauthenticateWithGoogle(idToken: String): NetworkResponse<Unit> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = firebaseAuth.currentUser

        return try {
            if (currentUser == null) {
                NetworkResponse.Error(Exception("No hay usuario autenticado actualmente."))
            } else {
                currentUser.reauthenticate(credential).await()
                NetworkResponse.Success(Unit)
            }
        } catch (e: Exception) {
            NetworkResponse.Error(e)
        }
    }

    /**
     * Observa el estado de verificación del correo electrónico del usuario.
     *
     * @return Un [Flow] que emite `true` si el correo electrónico está verificado, `false` en caso contrario.
     */
    override fun isEmailVerifiedFlow(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {

                user.reload().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isVerified = user.isEmailVerified
                        AppLogger.d(
                            "UserRepositoryImpl",
                            "isEmailVerifiedFlow: Email verification status reloaded: $isVerified"
                        )
                        trySend(isVerified)
                    } else {
                        AppLogger.e(
                            "UserRepositoryImpl",
                            "isEmailVerifiedFlow: Failed to reload user for email verification: ${task.exception?.message}"
                        )
                        trySend(user.isEmailVerified)
                    }
                }
            } else {
                AppLogger.d(
                    "UserRepositoryImpl",
                    "isEmailVerifiedFlow: No user found, email is not verified."
                )
                trySend(false)
            }
        }
        firebaseAuth.addAuthStateListener(listener)

        firebaseAuth.currentUser?.reload()?.await()
        trySend(firebaseAuth.currentUser?.isEmailVerified ?: false)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }.conflate()

    /**
     * Observa inconsistencias en la sesión del usuario.
     *
     * @return Un [Flow] que emite `true` si se detecta una inconsistencia en la sesión, `false` en caso contrario.
     */
    override fun observeSessionInconsistency(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(false)
                return@AuthStateListener
            }

            val job = launch {
                try {
                    val localUser = userDao.getUserById(firebaseUser.uid).first()
                    if (localUser == null) {
                        trySend(false)
                        return@launch
                    }

                    firebaseUser.reload().await()

                    val isMismatched = firebaseUser.email != localUser.email
                    if (isMismatched) {
                        AppLogger.w(
                            "UserRepositoryImpl",
                            "Inconsistencia de email detectada. Forzando logout."
                        )
                    }
                    trySend(isMismatched)

                } catch (e: Exception) {
                    AppLogger.e(
                        "UserRepositoryImpl",
                        "Fallo al recargar el usuario. La sesión es inválida.",
                        e
                    )
                    trySend(true)
                }
            }
            job.invokeOnCompletion { if (it is CancellationException) job.cancel() }
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }

    }.distinctUntilChanged()
}