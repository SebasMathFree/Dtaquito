package com.example.dtaquito.chat//package com.example.dtaquito
//
//import Beans.chat.ChatMessage
//import Beans.chat.MessageRecieve
//import Beans.playerList.PlayerList
//import Interface.PlaceHolder
//import android.graphics.Rect
//import android.os.Bundle
//import android.util.Log
//import android.view.KeyEvent
//import android.view.View
//import android.view.inputmethod.EditorInfo
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.gson.GsonBuilder
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.WebSocket
//import okhttp3.WebSocketListener
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class ChatRoomActivity : PlayerBase() {
//
//    private lateinit var service: PlaceHolder
//    private lateinit var bottomNavigationView: BottomNavigationView
//    private lateinit var chatView: RecyclerView
//    private lateinit var chatAdapter: ChatAdapter
//    private var webSocket: WebSocket? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_chat_room)
//        setupBottomNavigation(R.id.navigation_home)
//
//        service = createRetrofit().create(PlaceHolder::class.java)
//
//        chatView = findViewById(R.id.chatView)
//        chatAdapter = ChatAdapter()
//        chatView.layoutManager = LinearLayoutManager(this)
//        chatView.adapter = chatAdapter
//
//        val gameRoomId = intent.getIntExtra("GAME_ROOM_ID", -1)
//
//        if (gameRoomId != -1) {
//            fetchPlayerListByRoomId(gameRoomId)
//        } else {
//            showToast("Invalid game room ID")
//        }
//
//        val sendButton = findViewById<Button>(R.id.sendButton)
//        val messageInput = findViewById<EditText>(R.id.messageInput)
//        bottomNavigationView = findViewById(R.id.bottom_navigation)
//
//        sendButton.setOnClickListener {
//            val message = messageInput.text.toString()
//            if (message.isNotEmpty()) {
//                sendMessage(gameRoomId, userId, message)
//                messageInput.text.clear()
//                chatView.scrollToPosition(chatAdapter.itemCount - 1)
//            }
//        }
//
//        messageInput.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_SEND || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
//                val message = messageInput.text.toString()
//                if (message.isNotEmpty()) {
//                    sendMessage(gameRoomId, userId, message)
//                    messageInput.text.clear()
//                    chatView.scrollToPosition(chatAdapter.itemCount - 1)
//                }
//                return@OnEditorActionListener true
//            }
//            false
//        })
//
//        chatView.viewTreeObserver.addOnGlobalLayoutListener {
//            if (chatAdapter.itemCount > 0) {
//                chatView.scrollToPosition(chatAdapter.itemCount - 1)
//            }
//            val rect = Rect()
//            chatView.getWindowVisibleDisplayFrame(rect)
//            val screenHeight = chatView.rootView.height
//            val keypadHeight = screenHeight - rect.bottom
//            if (keypadHeight > screenHeight * 0.15) {
//                bottomNavigationView.visibility = View.GONE
//            } else {
//                bottomNavigationView.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    private fun createRetrofit(): Retrofit {
//        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor(AuthInterceptor(tokenManager))
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl("https://dtaquito-backend.azurewebsites.net/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//    }
//
//    private fun fetchPlayerListByRoomId(roomId: Int) {
//        service.getPlayerListByRoomId(roomId).enqueue(object : Callback<List<PlayerList>> {
//            override fun onResponse(call: Call<List<PlayerList>>, response: Response<List<PlayerList>>) {
//                if (response.isSuccessful) {
//                    response.body()?.let { playerList ->
//                        val isUserInList = playerList.any { it.userId == intent.getIntExtra("USER_ID", -1) }
//                        Log.d("ChatRoomActivity", "Player list for room $isUserInList")
//                        if (isUserInList){
//                            setupWebSocket(roomId)
//                        }
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<List<PlayerList>>, t: Throwable) {
//                logAndShowError("Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun fetchMessages(roomId: Int) {
//        Log.d("ChatRoomActivity", "Fetching messages for room $roomId")
//        service.getMessages(roomId).enqueue(object : Callback<List<ChatMessage>> {
//            override fun onResponse(call: Call<List<ChatMessage>>, response: Response<List<ChatMessage>>) {
//                Log.d("ChatRoomActivity", "Response received: ${response.code()}")
//                if (response.isSuccessful) {
//                    response.body()?.let { messages ->
//                        Log.d("ChatRoomActivity", "Messages for room $roomId: ${GsonBuilder().create().toJson(messages)}")
//                        chatAdapter.setMessages(messages)
//                    } ?: run {
//                        Log.e("ChatRoomActivity", "Response body is null")
//                        showToast("Failed to fetch messages")
//                    }
//                } else {
//                    Log.e("ChatRoomActivity", "Failed to fetch messages: ${response.errorBody()?.string()}")
//                    showToast("Failed to fetch messages")
//                }
//            }
//
//            override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {
//                Log.e("ChatRoomActivity", "Error fetching messages: ${t.message}")
//                logAndShowError("Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun sendMessage(roomId: Int, userId: Int, message: String) {
//        val sentMessage = MessageRecieve(content = message)
//        service.sendMessage(roomId, userId, sentMessage).enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    showToast("Message sent")
//                } else {
//                    showToast("Failed to send message")
//                }
//            }
//
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                logAndShowError("Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun setupWebSocket(roomId: Int) {
//        fetchMessages(roomId)
//        val token = tokenManager.getToken()
//        val request = Request.Builder()
//            .url("wss://dtaquito-backend.azurewebsites.net/ws/chat")
//            .addHeader("Authorization", "Bearer $token") // Add the authorization header
//            .build()
//
//        val client = OkHttpClient()
//        webSocket = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
//                runOnUiThread {
//                    Log.d("WebSocket", "Connected")
//                }
//            }
//
//            val gson = GsonBuilder()
//                .registerTypeAdapter(ChatMessage::class.java, ChatMessageDeserializer())
//                .create()
//
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                Log.d("WebSocket", "Message received: $text")
//                try {
//                    val chatMessage = gson.fromJson(text, ChatMessage::class.java)
//                    runOnUiThread {
//                        chatAdapter.addMessage(chatMessage)
//                        chatView.scrollToPosition(chatAdapter.itemCount - 1)
//                    }
//                } catch (e: Exception) {
//                    Log.e("Parsing Error", "Error parsing message: ${e.message}")
//                }
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
//                runOnUiThread {
//                    Log.e("WebSocket", "Error: ${t.message}")
//                }
//            }
//        })
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun logAndShowError(message: String) {
//        Log.e("MainGameRoomActivity", message)
//        showToast(message)
//    }
//}