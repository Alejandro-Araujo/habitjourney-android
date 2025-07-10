package com.alejandro.habitjourney.features.user.domain.repository

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.Flow


/**
 * Interfaz de repositorio para la gestión de usuarios.
 *
 * Define las operaciones que la capa de dominio necesita para interactuar con los datos del usuario,
 * incluyendo autenticación, gestión del perfil y estado de la sesión, sin exponer los detalles
 * de la implementación de la capa de datos (local o remota).
 */
interface UserRepository {
    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un [NetworkResponse] que contiene un [User] si el inicio de sesión es exitoso.
     * Nota: El token de autenticación generalmente se maneja internamente por Firebase Auth
     * y no es directamente expuesto aquí como parte del Pair.
     */
    suspend fun login(email: String, password: String): NetworkResponse<User>

    /**
     * Registra un nuevo usuario con los datos proporcionados.
     *
     * @param name El nombre del nuevo usuario.
     * @param email El correo electrónico del nuevo usuario.
     * @param password La contraseña del nuevo usuario.
     * @return Un [NetworkResponse] que contiene el [User] creado si el registro es exitoso.
     */
    suspend fun register(name: String, email: String, password: String): NetworkResponse<User>

    /**
     * Inicia sesión con Google utilizando Firebase Auth.
     *
     * @param idToken El token de ID de Google obtenido del GoogleSignInAccount.
     * @return Un [NetworkResponse] con el [User] si el inicio de sesión es exitoso.
     */
    suspend fun signInWithGoogle(idToken: String): NetworkResponse<User>


    /**
     * Obtiene los datos del usuario actualmente autenticado desde la fuente de datos.
     *
     * @return Un [NetworkResponse] que contiene el [User] actual si la operación es exitosa.
     * Retorna un error si no hay un usuario autenticado o hay un problema de red/Firebase.
     */
    suspend fun getCurrentUser(): NetworkResponse<User>

    /**
     * Actualiza la información de perfil del usuario (nombre y correo electrónico).
     *
     * @param name El nuevo nombre del usuario.
     * @param email El nuevo correo electrónico del usuario.
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la operación es exitosa.
     */
    suspend fun updateUser(name: String, email: String): NetworkResponse<User>

    /**
     * Cambia la contraseña del usuario actual.
     * Requiere reautenticación si la sesión no es reciente.
     *
     * @param currentPassword La contraseña actual del usuario.
     * @param newPassword La nueva contraseña que se desea establecer.
     * @return Un [NetworkResponse] que contiene un mensaje de éxito si la operación es exitosa.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): NetworkResponse<String>

    /**
     * Elimina la cuenta del usuario de forma permanente.
     * Esta operación generalmente requiere reautenticación previa por motivos de seguridad.
     *
     * @return Un [NetworkResponse] que contiene un mensaje de éxito si la operación es exitosa.
     */
    suspend fun deleteUser(): NetworkResponse<String>

    /**
     * Cierra la sesión actual del usuario, limpiando cualquier información de sesión.
     *
     * @return Un [NetworkResponse.Success] sin datos si el cierre de sesión es exitoso.
     */
    suspend fun logout(): NetworkResponse<Unit>

    /**
     * Obtiene un [Flow] que emite el objeto [User] almacenado localmente.
     * Esto permite a los observadores reaccionar a los cambios en los datos del usuario localmente,
     * como actualizaciones del perfil o estado de la sesión.
     *
     * @return Un [Flow] que emite el [User] local o `null` si no hay un usuario local.
     */
    fun getLocalUser(): Flow<User?>

    /**
     * Obtiene un [Flow] que indica si hay una sesión de usuario activa.
     * Refleja el estado de autenticación actual del usuario.
     *
     * @return Un [Flow] que emite `true` si el usuario está autenticado, `false` en caso contrario.
     */
    fun isLoggedIn(): Flow<Boolean>

    /**
     * Vincula una cuenta de Google a la cuenta del usuario actualmente autenticado en Firebase.
     * Esto permite que el usuario inicie sesión con el mismo email usando Google o el método original.
     *
     * Requiere que haya un usuario actualmente autenticado.
     *
     * @param idToken El token de ID de Google obtenido tras una autenticación exitosa con Google.
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la vinculación es exitosa.
     */
    suspend fun linkGoogleAccount(idToken: String): NetworkResponse<User>

    /**
     * Vincula credenciales de correo electrónico y contraseña a la cuenta del usuario actualmente autenticado en Firebase.
     * Esto permite que el usuario inicie sesión con el mismo email usando email/password o el método original (ej. Google).
     *
     * Requiere que haya un usuario actualmente autenticado.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña que se desea vincular.
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la vinculación es exitosa.
     */
    suspend fun linkEmailPassword(email: String, password: String): NetworkResponse<User>

    /**
     * Desvincula un método de autenticación específico de la cuenta del usuario actual.
     *
     * @param providerId El ID del proveedor de autenticación a desvincular (ej., "password", "google.com").
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la desvinculación es exitosa.
     */
    suspend fun unlinkAuthMethod(providerId: String): NetworkResponse<User>

    /**
     * Envía un correo electrónico de verificación a la dirección de correo del usuario actual.
     *
     * @return Un [NetworkResponse] indicando el resultado de la operación.
     */
    suspend fun sendEmailVerification(): NetworkResponse<Unit>

    /**
     * Reautentica al usuario actual con la credencial proporcionada.
     * Necesario para operaciones sensibles como cambio de contraseña, email o eliminación de cuenta.
     *
     * @param credential La credencial de autenticación obtenida del último inicio de sesión.
     * @return Un [NetworkResponse] indicando el resultado de la operación.
     */
    suspend fun reauthenticateUser(credential: AuthCredential): NetworkResponse<Unit>

    /**
     * Obtiene los métodos de autenticación vinculados al usuario actual.
     * Devuelve una lista de strings con los provider IDs (ej: "password", "google.com").
     *
     * @return Una [List] de [String] con los IDs de los proveedores de autenticación vinculados.
     * Si ocurre un error, esta lista podría estar vacía o se podría lanzar una excepción (dependiendo de la implementación).
     */
    suspend fun getLinkedAuthMethods(): List<String>

    /**
     * Reautentica al usuario actual utilizando credenciales de Google.
     * Esto es útil cuando se requiere una autenticación reciente para realizar operaciones sensibles
     * y el usuario desea reautenticarse con su cuenta de Google.
     *
     * @param idToken El token de ID de Google obtenido de una autenticación reciente con Google.
     * @return Un [NetworkResponse] indicando el resultado de la operación.
     */
    suspend fun reauthenticateWithGoogle(idToken: String): NetworkResponse<Unit>

    /**
     * Obtiene un [Flow] que emite el estado de verificación del correo electrónico del usuario.
     *
     * @return Un [Flow] que emite `true` si el correo electrónico está verificado, `false` en caso contrario.
     */
    fun isEmailVerifiedFlow(): Flow<Boolean>

    /**
     * Observa inconsistencias en la sesión del usuario.
     * Este Flow podría emitir `true` si se detecta que la sesión local no está sincronizada
     * con el estado de autenticación de Firebase (ej. usuario eliminado externamente).
     *
     * @return Un [Flow] que emite `true` si hay una inconsistencia en la sesión, `false` en caso contrario.
     */
    fun observeSessionInconsistency(): Flow<Boolean>
}