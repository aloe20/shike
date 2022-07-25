package com.aloe.socket

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

internal class SocketImpl constructor(private val ctx: Context, private val udp: ClientUdp) :
    ISocket {
    override fun initSocket() {
        SocketWorker.initSocket(ctx)
    }

    override fun getUdpReceive(): StateFlow<ByteArray> = udp.receiveFlow

    override fun startUdp(port: Int) {
        sendUdp("0.0.0.1", port, ByteArray(0))
    }

    override fun stopUdp() {
        sendUdp("0.0.0.2", 0, ByteArray(0))
    }

    override fun sendUdp(host: String, port: Int, data: ByteArray) {
        udp.sendData(host, port, data)
    }
}
