package com.aloe.shike

import com.aloe.http.IHttp
import com.aloe.local.ILocal
import com.aloe.socket.ISocket
import javax.inject.Inject

class Repository @Inject constructor(http: IHttp, local: ILocal, socket: ISocket) :
    LocalRepo(local, socket),
    IHttp by http

open class LocalRepo(local: ILocal, socket: ISocket) : SocketRepo(socket), ILocal by local

open class SocketRepo(socket: ISocket) : ISocket by socket
