package com.aloe.socket

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.net.Inet6Address
import java.net.NetworkInterface
import kotlinx.coroutines.flow.StateFlow

interface ISocket {
    fun initSocket()
    fun getUdpReceive(): StateFlow<ByteArray>
    fun startUdp(port: Int)
    fun stopUdp()
    fun sendUdp(host: String, port: Int, data: ByteArray)
}

fun getIp(): String {
    return runCatching {
        NetworkInterface.getNetworkInterfaces().asSequence()
            .filter { it.isUp && it.inetAddresses.toList().isNotEmpty() && it.isLoopback.not() }
            .flatMap { it.inetAddresses.asSequence() }
            .filter { (it is Inet6Address && it.isLinkLocalAddress).not() }
            .first().hostName
    }.getOrDefault("")
}

@Module
@InstallIn(SingletonComponent::class)
internal interface SocketModule {
    @Binds
    fun getSocket(local: SocketImpl): ISocket
}
