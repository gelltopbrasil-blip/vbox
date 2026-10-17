package com.exp.vbox

import android.net.VpnService

/**
 * Ponte entre a interface TUN e o proxy SOCKS5.
 * TODO: implementar com uma lib nativa tun2socks (ex.: hev-socks5-tunnel via NDK/JNI).
 * IMPORTANTE: os sockets que o engine abre para falar com o proxy precisam passar por
 * vpnService.protect(socket); senão o tráfego do próprio túnel entra em loop na VPN.
 */
object TunnelEngine {
    fun start(tunFd: Int, proxy: ProxyConfig, vpn: VpnService) {
        // chamar a lib nativa aqui, passando tunFd, proxy e um callback que use vpn.protect(fd)
    }
    fun stop() {
        // parar a lib nativa aqui
    }
}
