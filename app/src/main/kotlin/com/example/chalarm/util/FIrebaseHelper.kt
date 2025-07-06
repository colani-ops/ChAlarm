package com.example.chalarm.util

import com.example.chalarm.data.Alarm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FirebaseHelper {

    private val auth = FirebaseAuth.getInstance()

    private fun getUserCollection(): CollectionReference {
        return FirebaseFirestore.getInstance()
            .collection("users")
            .document(auth.currentUser?.uid ?: "unknown_user")
            .collection("alarms")
    }

    private var listenerRegistration: ListenerRegistration? = null

    fun listenToAlarms(onAlarmsChanged: (List<Alarm>) -> Unit) {
        listenerRegistration?.remove()

        if (auth.currentUser?.uid == null) {
            onAlarmsChanged(emptyList())
            return
        }

        listenerRegistration = getUserCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onAlarmsChanged(emptyList())
                    return@addSnapshotListener
                }

                val alarms = snapshot.documents.mapNotNull { it.toObject(Alarm::class.java)?.copy(id = it.id) }
                onAlarmsChanged(alarms)
            }
    }

    fun addAlarm(alarm: Alarm) {
        if (auth.currentUser?.uid == null) return
        getUserCollection().document(alarm.id).set(alarm)
    }

    fun updateAlarm(alarm: Alarm, onComplete: (() -> Unit)? = null) {
        if (auth.currentUser?.uid == null) return
        getUserCollection().document(alarm.id).set(alarm)
            .addOnSuccessListener {
                onComplete?.invoke()
            }
            .addOnFailureListener {
                onComplete?.invoke()
            }
    }

    fun deleteAlarm(alarmId: String) {
        if (auth.currentUser?.uid == null) return
        getUserCollection().document(alarmId).delete()
    }

    fun getAlarmOnce(alarmId: String, onResult: (Alarm?) -> Unit) {
        if (auth.currentUser?.uid == null) {
            onResult(null)
            return
        }

        getUserCollection().document(alarmId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val alarm = document.toObject(Alarm::class.java)?.copy(id = document.id)
                    onResult(alarm)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
