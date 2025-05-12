package Interface

import Beans.auth.forgotPassword.ForgotPasswordRequest
import Beans.auth.login.LoginRequest
import Beans.auth.login.LoginResponse
import Beans.auth.register.RegisterRequest
import Beans.chat.ChatMessage
import Beans.chat.MessageRecieve
import Beans.rooms.GameRoom
import Beans.sportspaces.SportSpace
import Beans.subscription.Subscriptions
import Beans.update.UpdateEmailRequest
import Beans.update.UpdateNameRequest
import Beans.update.UpdatePasswordRequest
import Beans.userProfile.UserProfile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaceHolder {

    // Autenticación
    @POST("api/v1/users/sign-up")
    fun createUser(@Body registerRequest: RegisterRequest): Call<UserProfile>

    @POST("api/v1/authentication/sign-in")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/v1/authentication/log-out")
    fun logOutUser(): Call<Void>

    // Usuario
    @GET("api/v1/users/me")
    fun getUserId(): Call<UserProfile>

    @PUT("api/v1/users/email")
    fun updateEmail(@Body emailRequest: UpdateEmailRequest): Call<ResponseBody>

    @PUT("api/v1/users/password")
    fun updatePassword(@Body passwordRequest: UpdatePasswordRequest): Call<ResponseBody>

    @PUT("api/v1/users/name")
    fun updateName(@Body nameRequest: UpdateNameRequest): Call<ResponseBody>

    @PUT("api/v1/users/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: UserProfile): Call<UserProfile>

    // Espacios deportivos
    @GET("api/v1/sport-spaces/all")
    fun getAllSportSpaces(): Call<List<SportSpace>>

    @GET("api/v1/sport-spaces/{id}")
    fun getSportSpaceById(@Path("id") id: Int): Call<SportSpace>

    @GET("api/v1/sport-spaces/my-space")
    fun getSportSpacesByUserId(): Call<List<SportSpace>>

    @Multipart
    @POST("api/v1/sport-spaces/create")
    fun createSportSpace(
        @Part("name") name: RequestBody,
        @Part("sportId") sportId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("price") price: RequestBody,
        @Part("address") address: RequestBody,
        @Part("description") description: RequestBody,
        @Part("openTime") openTime: RequestBody,
        @Part("closeTime") closeTime: RequestBody,
        @Part("gamemodeId") gamemodeId: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Call<ResponseBody>

    // Habitaciones
    @GET("api/v1/rooms/all")
    fun getAllRooms(): Call<List<GameRoom>>

    @GET("api/v1/rooms/{id}")
    fun getRoomById(@Path("id") id: Int): Call<GameRoom>

    @FormUrlEncoded
    @POST("api/v1/rooms/create")
    fun createRoom(
        @Field("creatorId") creatorId: Long,
        @Field("sportSpaceId") sportSpaceId: Long,
        @Field("day") day: String,
        @Field("openingDate") openingDate: String,
        @Field("roomName") roomName: String
    ): Call<GameRoom>

    // Suscripciones
    @GET("api/v1/subscriptions")
    fun getCurrentSubscription(): Call<Subscriptions>

    @PUT("api/v1/subscriptions/upgrade")
    fun upgradeSubscription(@Query("newPlanType") newPlanType: String): Call<ResponseBody>

    // Chat
    @GET("api/v1/chat/rooms/{roomId}/messages")
    fun getMessages(@Path("roomId") roomId: Int): Call<List<ChatMessage>>

    @POST("api/v1/chat/rooms/{roomId}/messages")
    fun sendMessage(
        @Path("roomId") roomId: Int,
        @Query("userId") userId: Int,
        @Body chatMessage: MessageRecieve
    ): Call<Void>

    // Contraseñas
    @POST("/api/v1/recover-password/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ResponseBody>
}

