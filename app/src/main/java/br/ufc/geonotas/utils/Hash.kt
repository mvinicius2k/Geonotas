package br.ufc.geonotas.utils

import java.security.MessageDigest

class Hash {
    companion object{
        fun sha256(string: String): String{
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(string.toByteArray())
            return digest.fold("", {
                    str, it -> str + "%02x".format(it)
            })
        }
    }
}