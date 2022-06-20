package com.aloe.socket

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress

internal class ClientTcp {
    fun connect(host: String, port: Int) {
        val group = NioEventLoopGroup()
        runCatching {
            val strap = Bootstrap()
            strap.group(group).channel(NioSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                            override fun channelActive(ctx: ChannelHandlerContext) {
                                super.channelActive(ctx)
                                val address = ctx.channel().remoteAddress() as InetSocketAddress
                                println("连接服务器${address.hostName}:${address.port}成功")
                            }

                            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                super.channelRead(ctx, msg)
                                val address = ctx.channel().remoteAddress() as InetSocketAddress
                                println("收到服务器(${address.hostName}:${address.port})数据:$msg")
                            }
                        })
                    }
                })
            strap.connect(host, port).sync().channel().closeFuture().sync()
        }
        group.shutdownGracefully()
    }
}
