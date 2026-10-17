package com.exp.vbox

/** Formato por linha: host:porta  ou  host:porta:usuario:senha  (SOCKS5) */
data class ProxyConfig(val host: String, val port: Int, val user: String?, val pass: String?) {
    companion object {
        fun parse(line: String?): ProxyConfig? {
            val p = line?.trim()?.split(":") ?: return null
            if (p.size != 2 && p.size != 4) return null
            val port = p[1].toIntOrNull() ?: return null
            return ProxyConfig(p[0], port, p.getOrNull(2), p.getOrNull(3))
        }
    }
}

object ProxyPool {
    private var list: List<ProxyConfig> = emptyList()
    private var idx = -1
    fun load(text: String) {
        list = text.lines().mapNotNull { ProxyConfig.parse(it) }
        idx = -1
    }
    fun next(): ProxyConfig? {
        if (list.isEmpty()) return null
        idx = (idx + 1) % list.size
        return list[idx]
    }
    fun size() = list.size
}
