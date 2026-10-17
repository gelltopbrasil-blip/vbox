package com.exp.vbox

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var input: EditText
    private lateinit var status: TextView
    private var pending: ProxyConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        input = EditText(this).apply {
            hint = "um proxy por linha: host:porta[:user:senha]"
            minLines = 5
        }
        status = TextView(this).apply { text = "parado" }
        val rotate = Button(this).apply { text = "Conectar / próximo proxy"; setOnClickListener { rotateProxy() } }
        val stop = Button(this).apply { text = "Parar"; setOnClickListener { stopVpn() } }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 96, 32, 32)
            addView(input); addView(rotate); addView(stop); addView(status)
        })
    }

    private fun rotateProxy() {
        if (ProxyPool.size() == 0) ProxyPool.load(input.text.toString())
        val p = ProxyPool.next() ?: run { status.text = "lista de proxies inválida"; return }
        pending = p
        val prep = VpnService.prepare(this)
        if (prep != null) startActivityForResult(prep, 1) else startVpn(p)
    }

    private fun startVpn(p: ProxyConfig) {
        startService(Intent(this, ProxyVpnService::class.java)
            .putExtra(ProxyVpnService.EXTRA_PROXY, listOfNotNull(p.host, p.port.toString(), p.user, p.pass).joinToString(":")))
        status.text = "proxy: ${p.host}:${p.port}"
    }

    private fun stopVpn() {
        startService(Intent(this, ProxyVpnService::class.java).setAction(ProxyVpnService.ACTION_STOP))
        status.text = "parado"
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) pending?.let { startVpn(it) }
    }
}
