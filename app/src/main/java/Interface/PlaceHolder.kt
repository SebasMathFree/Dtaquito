package Interface

import Beans.chat.ChatMessage
import Beans.rooms.GameRoom
import Beans.auth.login.LoginRequest
import Beans.auth.login.LoginResponse
import Beans.auth.register.RegisterRequest
import Beans.chat.MessageRecieve
import Beans.playerList.PlayerList
import Beans.sportspaces.SportSpace
import Beans.subscription.Subscriptions
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

    @POST("api/v1/users/sign-up")
    fun createUser(@Body registerRequest: RegisterRequest): Call<UserProfile>

    @POST("api/v1/authentication/sign-in")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("api/v1/users/me")
    fun getUserId(): Call<UserProfile>

    @POST("api/v1/authentication/log-out")
    fun logOutUser(): Call<Void>

    @PUT("/api/v1/users/email")
    fun updateEmail(@Body emailRequest: Map<String, String>): Call<ResponseBody>

    @PUT("api/v1/users/password")
    fun updatePassword(@Body passwordRequest: Map<String, String>): Call<ResponseBody>

    @PUT("api/v1/users/name")
    fun updateName(@Body nameRequest: Map<String, String>): Call<ResponseBody>

    @GET("api/v1/sport-spaces/all")
    fun getAllSportSpaces(): Call<List<SportSpace>>

    @GET("api/v1/sport-spaces/{id}")
    fun getSportSpaceById(@Path("id") id: Int): Call<SportSpace>

    @GET("/api/v1/sport-spaces/my-space")
    fun getSportSpacesByUserId(): Call<List<SportSpace>>

    @GET("api/v1/rooms/all")
    fun getAllRooms(): Call<List<GameRoom>>

    @FormUrlEncoded
    @POST("api/v1/rooms/create")
    fun createRoom(
        @Field("creatorId") creatorId: Long,
        @Field("sportSpaceId") sportSpaceId: Long,
        @Field("day") day: String,
        @Field("openingDate") openingDate: String,
        @Field("roomName") roomName: String
    ): Call<GameRoom>

    @GET("api/v1/rooms/{id}")
    fun getRoomById(@Path("id") id: Int): Call<GameRoom>

    @GET("api/v1/subscriptions")
    fun getCurrentSubscription(): Call<Subscriptions>

    @PUT("api/v1/subscriptions/upgrade")
    fun upgradeSubscription(
        @Query("newPlanType") newPlanType: String
    ): Call<ResponseBody>

    @GET("api/v1/player-lists/room/{id}")
    fun getPlayerListByRoomId(@Path("id") id: Int): Call<List<PlayerList>>

    @FormUrlEncoded
    @POST("api/v1/player-lists/join")
    fun joinRoom(
        @Field("userId") userId: Int,
        @Field("roomId") roomId: Int
    ): Call<Void>

    @POST("api/v1/deposit/create-deposit")
    fun createDeposit(@Query("amount") amount: Int): Call<ResponseBody>

    @PUT("api/v1/users/{id}")
    fun updateUser(
        @Path("id") id: Int,
        @Body user: UserProfile
    ): Call<UserProfile>

    @GET("api/v1/chat/rooms/{roomId}/messages")
    fun getMessages(@Path("roomId") roomId: Int): Call<List<ChatMessage>>

    @POST("api/v1/chat/rooms/{roomId}/messages")
    fun sendMessage(
        @Path("roomId") roomId: Int,
        @Query("userId") userId: Int,
        @Body chatMessage: MessageRecieve
    ): Call<Void>

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



}

