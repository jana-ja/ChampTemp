package de.janaja.champtemp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.janaja.champtemp.data.model.TempHumi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class Repository {

    private val db = Firebase.firestore
    private val TAG = "Repo"

    private val _tempHumis: MutableLiveData<List<TempHumi>> = MutableLiveData<List<TempHumi>>()
    val tempHumis: LiveData<List<TempHumi>>
        get() = _tempHumis

    fun Timestamp.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(seconds * 1000 + nanoseconds / 1000000), zone)

    fun loadTempHumiData() {

        db.collection("temphumi_data")
            .orderBy("myTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                val list = mutableListOf<TempHumi>()
                for (document in querySnapshot!!) {
                    val tempHumi = TempHumi(
                        document.id,
                        document.data["temp"].toString().toInt(),
                        document.data["humi"].toString().toInt(),
                        (document.data["myTimestamp"] as Timestamp).toLocalDateTime()
                    )
                    list.add(tempHumi)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }

                val source = if (querySnapshot.metadata.isFromCache)
                    "local cache"
                else
                    "server"
                Log.d(TAG, "Data fetched from $source")

                _tempHumis.value = list
            }


//        db.collection("tempHumie").orderBy("prio").orderBy("text")
//            .get()
//            .addOnSuccessListener { collection ->
//                val list = mutableListOf<TempHumi>()
//                for (document in collection!!) {
//                    val tempHumi = TempHumi(
//                        document.id,
//                        document.data["text"].toString(),
//                        document.data["prio"].toString().toInt()
//                    )
//                    list.add(tempHumi)
//                    Log.d(TAG, "${document.id} => ${document.data}")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Listen failed.", e)
//            }

    }
}