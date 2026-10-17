package com.exp.vbox

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var input: EditText
    private lateinit var status: TextView
    private lateinit var proxyBox: LinearLayout
    private lateinit var modeLocal: RadioButton
    private lateinit var modeProxy: RadioButton
    private var pending: ProxyConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply { text = "modo: rede local" }
        input = EditText(this).apply {
            hint = "um proxy por linha: host:porta[:user:senha]"
            minLines = 5
        }
        val connect = Button(this).apply { text = "Conectar proxy"; setOnClickListener { connectProxy(true) } }
        val next = Button(this).apply { text = "Próximo proxy"; setOnClickListener { connectProxy(false) } }
        proxyBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            addView(input); addView(connect); addView(next)
        }
        modeLocal = RadioButton(this).apply { text = "Rede local (sem proxy)"; id = View.generateViewId() }
        modeProxy = RadioButton(this).apply { text = "Usar proxys"; id = View.generateViewId() }
        val group = RadioGroup(this).apply {
            addView(modeLocal); addView(modeProxy)
            check(modeLocal.id)
            setOnCheckedChangeListener { _, checked ->
                if (checked == modeProxy.id) {
                    proxyBox.visibility = View.VISIBLE
                    status.text = "modo: proxy (desconectado)"
                } else {
                    proxyBox.visibility = View.GONE
                    stopVpn()
                    status.text = "modo: rede local"
                }
            }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 96, 32, 32)
            addView(group); addView(proxyBox); addView(status)
        })
    }

    private fun connectProxy(reload: Boolean) {
        if (reload || ProxyPool.size() == 0) ProxyPool.load(input.text.toString())
        val p = ProxyPool.next() ?: run { status.text = "lista de proxies vazia ou inválida"; return }
        pending = p
        val prep = VpnService.prepare(this)
        if (prep != null) startActivityForResult(prep, 1) else startVpn(p)
    }

    private fun startVpn(p: ProxyConfig) {
        startService(Intent(this, ProxyVpnService::class.java)
            .putExtra(ProxyVpnService.EXTRA_PROXY, listOfNotNull(p.host, p.port.toString(), p.user, p.pass).joinToString(":")))
        status.text = "modo: proxy ${p.host}:${p.port}"
    }

    private fun stopVpn() {
        startService(Intent(this, ProxyVpnService::class.java).setAction(ProxyVpnService.ACTION_STOP))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) pending?.let { startVpn(it) }
    }
}
