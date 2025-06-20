package com.example.dtaquito.chat

import Beans.chat.ChatMessage
import Beans.chat.MessageRecieve
import Beans.playerList.Player
import Interface.PlaceHolder
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import com.google.gson.GsonBuilder
import network.RetrofitClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatRoomFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var chatView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var userId: Int = -1
    private var webSocket: WebSocket? = null
    private var gameRoomId: Int = -1
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameRoomId = it.getInt("GAME_ROOM_ID", -1)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        RetrofitClient.initialize(context.applicationContext)

        prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        service = RetrofitClient.instance.create(PlaceHolder::class.java)

        userId = prefs.getInt("user_id", -1)
        Log.d("ChatRoomFragment", "User ID: $userId")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_chat_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatView = view.findViewById(R.id.chatView)
        chatAdapter = ChatAdapter()
        chatView.layoutManager = LinearLayoutManager(requireContext())
        chatView.adapter = chatAdapter

        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val messageInput = view.findViewById<EditText>(R.id.messageInput)
        //bottomNavigationView = view.findViewById(R.id.bottom_navigation)

        if (gameRoomId != -1) {
            fetchMessages(gameRoomId)
            fetchPlayerListByRoomId(gameRoomId)
        } else {
            requireContext().showToast("Invalid game room ID")
        }

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    .apply { timeZone = TimeZone.getTimeZone("America/Lima") }
                    .format(Date())
                val localMsg = ChatMessage(
                    content   = message,
                    userId    = userId,
                    userName  = prefs.getString("user_name", "USUARIO") ?: "USUARIO",
                    createdAt = now,
                    roomId = gameRoomId
                )

                //chatAdapter.addMessage(localMsg)
                chatView.scrollToPosition(chatAdapter.itemCount - 1)

                sendMessage(gameRoomId, userId, message)

                messageInput.text.clear()
            }
        }

        messageInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val message = messageInput.text.toString()
                if (message.isNotEmpty()) {
                    sendMessage(gameRoomId, userId, message)
                    messageInput.text.clear()
                    chatView.scrollToPosition(chatAdapter.itemCount - 1)
                }
                return@OnEditorActionListener true
            }
            false
        })

        chatView.viewTreeObserver.addOnGlobalLayoutListener {
            if (chatAdapter.itemCount > 0) {
                chatView.scrollToPosition(chatAdapter.itemCount - 1)
            }
            val rect = Rect()
            chatView.getWindowVisibleDisplayFrame(rect)
        }
    }

    override fun onStart() {
        super.onStart()
        if (gameRoomId != -1 && webSocket == null) {
            setupWebSocket(gameRoomId)
        }
    }

    override fun onStop() {
        super.onStop()
        webSocket?.close(1000, null)
        webSocket = null
    }


    private fun fetchPlayerListByRoomId(roomId: Int) {
        service.getPlayerListByRoomId(roomId).enqueue(object : Callback<List<Player>> {
            override fun onResponse(call: Call<List<Player>>, response: Response<List<Player>>) {
                if (response.isSuccessful) {
                    response.body()?.let { playerList ->
                        val isUserInList = playerList.any { it.id == userId }
                        Log.d("ChatRoomFragment", "Player list for room $isUserInList")
                        if (isUserInList) {
                            setupWebSocket(roomId)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Player>>, t: Throwable) {
                logAndShowError("Error: ${t.message}")
            }
        })
    }

    private fun fetchMessages(roomId: Int) {
        Log.d("ChatRoomFragment", "Fetching messages for room $roomId")
        service.getMessages(roomId).enqueue(object : Callback<List<ChatMessage>> {
            override fun onResponse(call: Call<List<ChatMessage>>, response: Response<List<ChatMessage>>) {
                Log.d("ChatRoomFragment", "Response received: ${response.code()}")
                if (response.isSuccessful) {
                    response.body()?.let { messages ->
                        Log.d("ChatRoomFragment", "Messages for room $roomId: $messages")
                        chatAdapter.setMessages(messages)
                    } ?: run {
                        Log.e("ChatRoomFragment", "Response body is null")
                        context?.showToast("Failed to fetch messages")
                    }
                } else {
                    Log.e("ChatRoomFragment", "Failed to fetch messages: ${response.errorBody()?.string()}")
                    context?.showToast("Failed to fetch messages")
                }
            }

            override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {
                Log.e("ChatRoomFragment", "Error fetching messages: ${t.message}")
                logAndShowError("Error: ${t.message}")
            }
        })
    }


    private fun sendMessage(roomId: Int, userId: Int, message: String) {
        service.sendMessage(roomId, userId, MessageRecieve(content = message)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                logAndShowError("Error: ${t.message}")
            }
        })
        val chatMessage = ChatMessage(
            content = message,
            userId = userId,
            userName = "unname",
            createdAt = "",
            roomId = roomId
        )
        val gson = GsonBuilder().create()
        webSocket?.send(gson.toJson(chatMessage))
    }
    private fun setupWebSocket(roomId: Int) {
        if (webSocket != null) return
        //fetchMessages(roomId)
        val request = Request.Builder()
            .url(RetrofitClient.CHAT_URL)
            .build()

        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                activity?.runOnUiThread {
                    Log.d("WebSocket", "Connected — suscribiéndome a la sala $roomId")
                }
                val subscribeJson = """
                {
                  "action": "subscribe",
                  "roomId": $roomId
                }
            """.trimIndent()
                webSocket.send(subscribeJson)
            }

            val gson = GsonBuilder()
                .registerTypeAdapter(ChatMessage::class.java, ChatMessageDeserializer())
                .create()

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message received: $text")
                try {
                    val chatMessage = gson.fromJson(text, ChatMessage::class.java)
                    //if (chatMessage.userId == userId) return

                    activity?.runOnUiThread {
                        if (isAdded) {
                            chatAdapter.addMessage(chatMessage)
                            chatView.scrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Parsing Error", "Error parsing message: ${e.message}")
                }
            }


            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                activity?.runOnUiThread {
                    Log.e("WebSocket", "Error: ${t.message}")
                }
            }
        })
    }

    private fun logAndShowError(message: String) {
        Log.e("ChatRoomFragment", message)
        context?.showToast(message)
    }
}