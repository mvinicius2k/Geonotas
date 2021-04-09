package br.ufc.geonotas.db

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

abstract class Connection (private val path: String) {
    private val database = FirebaseDatabase.getInstance();
    val reference = database.getReference(path);
    protected val ONE_MEGABYTE: Long = 1024 * 1024
}