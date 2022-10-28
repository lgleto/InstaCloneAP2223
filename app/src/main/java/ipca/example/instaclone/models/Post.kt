package ipca.example.instaclone.models

import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.ContentViewCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

data class Post (
    var comment     : String,
    var postDate    : Date,
    var userId      : String,
    var urlToImage  : String,
    ){

    fun toHashMap() : HashMap<String,Any> {
       return hashMapOf(
            "comment"    to comment,
            "postDate"   to Timestamp(postDate),
            "userId"     to userId,
            "urlToImage" to urlToImage
        )
    }

    fun sendPost(callback: (error:String?)->Unit) {
        val db = Firebase.firestore
        db.collection("posts")
            .add(toHashMap())
            .addOnSuccessListener { documentReference ->
                callback(null)
            }
            .addOnFailureListener { e ->
                callback(e.toString())
            }
    }

    companion object {
        fun fromDoc(doc:DocumentSnapshot) : Post {
            return Post(
                doc.getString("comment" )!!,
                doc.getDate("postDate")!!,
                doc.getString("userId")!!,
                doc.getString("urlToImage")!!
            )
        }
    }
}
