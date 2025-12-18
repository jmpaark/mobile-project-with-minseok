package com.example.jiminandminseok

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirestoreRecordRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RecordRepository {
    private val collection = firestore.collection("smoking_records")

    override suspend fun add(count: Int, memo: String?) {
        val data = mapOf(
            "id" to UUID.randomUUID().toString(),
            "count" to count,
            "memo" to memo,
            "createdAt" to System.currentTimeMillis()
        )
        suspendCoroutine { cont ->
            collection.add(data)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    override fun observeAll(): Flow<List<SmokingRecord>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val records = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val count = doc.getLong("count")?.toInt() ?: return@mapNotNull null
                    val memo = doc.getString("memo")
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    SmokingRecord(
                        id = id,
                        count = count,
                        memo = memo,
                        createdAt = createdAt
                    )
                } ?: emptyList()
                trySend(records).isSuccess
            }
        awaitClose { listener.remove() }
    }
}
