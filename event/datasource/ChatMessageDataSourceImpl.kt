package com.dev.podo.event.datasource

import android.util.Log
import com.dev.podo.BuildConfig
import com.dev.podo.core.datasource.Storage
import com.dev.podo.event.model.dto.chat.ChatDto
import com.dev.podo.event.model.entities.ChatEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.centrifugal.centrifuge.*
import javax.inject.Inject
import kotlin.text.Charsets.UTF_8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatMessageDataSourceImpl @Inject constructor() : ChatMessageDataSource {

    private val className = this.javaClass.name
    private var _messageFlow: MutableSharedFlow<ChatEvent.Message?> = MutableSharedFlow()
    override val messageFlow: SharedFlow<ChatEvent.Message?>
        get() = _messageFlow.asSharedFlow()

    override fun init(coroutineScope: CoroutineScope) {

//        coroutineScope.launch {
//            Log.i(className, "coroutine start")
//            repeat(10){
//                Log.i(className, "repeat start")
//                val message = ChatEvent.Message.emptyMessage(it, "test message $it", 156)
//                _messageFlow.emit(message)
//                Log.i(className, "emit: $message")
//                delay(5000 - 50 * it.toLong())
//            }
//            Log.i(className, "repeat end")
//        }

        val listener: EventListener = object : EventListener() {
            override fun onConnect(client: Client?, event: ConnectEvent?) {
                Log.i(this.javaClass.name, "connected")
            }

            override fun onDisconnect(client: Client?, event: DisconnectEvent) {
                Log.i(this.javaClass.name, "disconnected ${event.reason}, reconnect ${event.reconnect}")
            }
        }

        val subListener = object : SubscriptionEventListener() {

            override fun onSubscribeSuccess(sub: Subscription, event: SubscribeSuccessEvent) {
                Log.i(this.javaClass.name, "subscribed to " + sub.channel)
            }

            override fun onSubscribeError(sub: Subscription, event: SubscribeErrorEvent) {
                Log.e(this.javaClass.name, "subscribe error " + sub.channel + " " + event.message)
            }

            override fun onPublish(sub: Subscription, event: PublishEvent) {
                val data = String(event.data, UTF_8)
                Log.i(this.javaClass.name, "message from " + sub.channel + " " + data)
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val jsonAdapter = moshi.adapter(ChatDto.WSMessage::class.java)
                val message = jsonAdapter.fromJson(data)

                coroutineScope.launch {
                    message?.message?.asEntity()?.let {
                        _messageFlow.emit(it)
                    }
                }
            }
        }

        val client = Client(
            BuildConfig.CENT_HOST,
            Options(),
            listener
        )

        client.setToken(Storage.broadcastToken)
        client.connect()

        val sub: Subscription? = try {
            client.newSubscription("chats:user#${Storage.user?.id}", subListener)
        } catch (e: DuplicateSubscriptionException) {
            e.printStackTrace()
            null
        }
        sub?.subscribe()
    }
}
