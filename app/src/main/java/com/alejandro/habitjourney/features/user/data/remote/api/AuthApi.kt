package com.alejandro.habitjourney.features.user.data.remote.api

import com.alejandro.habitjourney.features.user.data.remote.dto.UserDTO
import com.alejandro.habitjourney.features.user.data.remote.dto.request.LoginRequest
import com.alejandro.habitjourney.features.user.data.remote.dto.request.PasswordChangeDTO
import com.alejandro.habitjourney.features.user.data.remote.dto.request.RegisterRequest
import com.alejandro.habitjourney.features.user.data.remote.dto.response.LoginResponse
import com.alejandro.habitjourney.features.user.data.remote.dto.response.MessageResponse
import com.alejandro.habitjourney.features.user.data.remote.dto.response.RegisterResponse
import com.alejandro.habitjourney.features.user.data.remote.dto.response.UserResponseDTO
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para los endpoints de autenticación y usuario.
 * Define las llamadas a la API del backend.
 */
interface AuthApi {

    /**
     * Endpoint para registrar un nuevo usuario.
     * Corresponde a POST /api/auth/register.
     */
    @POST("/api/auth/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

    /**
     * Endpoint para iniciar sesión.
     * Corresponde a POST /api/auth/login.
     */
    @POST("/api/auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>

    /**
     * Endpoint para obtener la información del usuario actualmente autenticado.
     * Corresponde a GET /api/users/me.
     * Requiere token JWT en el header Authorization.
     */
    @GET("/api/users/me")
    suspend fun getCurrentUser(): Response<UserResponseDTO>

    /**
     * Endpoint para actualizar los datos del usuario actualmente autenticado.
     * Corresponde a PUT /api/users/me.
     * Requiere token JWT en el header Authorization.
     */
    @PUT("/api/users/me")
    suspend fun updateUser(@Body userDTO: UserDTO): Response<UserResponseDTO>

    /**
     * Endpoint para eliminar la cuenta del usuario actualmente autenticado.
     * Corresponde a DELETE /api/users/me.
     * Requiere token JWT en el header Authorization.
     */
    @DELETE("/api/users/me")
    suspend fun deleteUser():  Response<MessageResponse>  // Usamos Response<Void> si no esperamos cuerpo de respuesta

    /**
     * Endpoint para cambiar la contraseña del usuario actualmente autenticado.
     * Corresponde a POST /api/users/me/change-password.
     * Requiere token JWT en el header Authorization.
     */
    @POST("/api/users/me/change-password")
    suspend fun changePassword(@Body passwordChangeDTO: PasswordChangeDTO): Response<MessageResponse>
}