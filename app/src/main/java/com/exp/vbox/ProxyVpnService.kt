package com.exp.vbox

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class ProxyVpnService : VpnService() {
    private var tun: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTunnel(); stopSelf()
        } else {
            val proxy = ProxyConfig.parse(intent?.getStringExtra(EXTRA_PROXY))
            if (proxy != null) startTunnel(proxy)
        }
        return START_STICKY
    }

    private fun startTunnel(proxy: ProxyConfig) {
        stopTunnel()
        val b = Builder()
            .setSession("vbox")
            .setMtu(1500)
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")
            // Apps dentro do container virtual rodam no UID deste APK,
            // então limitar a este pacote cobre só o que roda dentro dele.
            .addAllowedApplication(packageName)
        tun = b.establish() ?: return
        TunnelEngine.start(tun!!.fd, proxy, this)
    }

    private fun stopTunnel() {
        TunnelEngine.stop()
        tun?.close()
        tun = null
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.exp.vbox.STOP"
        const val EXTRA_PROXY = "proxy"
    }
}
