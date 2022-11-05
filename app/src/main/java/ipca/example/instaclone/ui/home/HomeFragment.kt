package ipca.example.instaclone.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import com.google.firebase.storage.ktx.storage
import ipca.example.instaclone.R
import ipca.example.instaclone.databinding.FragmentHomeBinding
import ipca.example.instaclone.databinding.RowPostBinding
import ipca.example.instaclone.models.Post
import java.io.InputStream
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var posts = arrayListOf<Post>()

    var adapter = PostsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabPostPhoto.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_postFragment)
        }
        val db = Firebase.firestore
        db.collection("posts")
            .addSnapshotListener { value, error ->
                posts.clear()
                for (doc in value?.documents!!){
                    posts.add(Post.fromDoc(doc))
                }
                adapter.notifyDataSetChanged()
            }
        binding.recycleViewPosts.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recycleViewPosts.adapter = adapter
        binding.recycleViewPosts.itemAnimator = DefaultItemAnimator()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class PostsAdapter : RecyclerView.Adapter<PostsAdapter.ViewHolder>(){

        inner class ViewHolder(binding: RowPostBinding) : RecyclerView.ViewHolder(binding.root){
            val textViewTitle : TextView = binding.textViewTitle
            val textViewDate : TextView = binding.textViewDate
            val imageViewPhoto : ImageView = binding.imageViewPhoto
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                RowPostBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var item = posts[position]
            holder.apply {
                val storage = Firebase.storage
                var storageRef = storage.reference
                var islandRef = storageRef.child("images/${item.urlToImage}")

                val ONE_MEGABYTE: Long = 10024 * 1024
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {

                    val inputStream = it.inputStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageViewPhoto.setImageBitmap(bitmap)
                }.addOnFailureListener {
                    // Handle any errors
                    Log.d(TAG, it.toString() )
                }

                textViewTitle.text = item.comment
                textViewDate.text = item.postDate.toString()

                imageViewPhoto.setOnClickListener {
                    val SENDER_ID = 941462621433

                    val fm = Firebase.messaging
                    fm.send(remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                        setMessageId("cPHLpGVxQeCXD9kF29qafh:APA91bEQ98QQ18_28g8QE0BxSdI1dA8ouZbAaONdS0KlpNRGZVQU5pmfXMmYsS5XTeVXNhWTZI3XzGrdyfIQ0VKRpiQYmkGzr0CvTVyqzOLu16GZ_2MVj6_CJXlB8pqunI851oWjJxsh")
                        addData("my_message", "Hello World")
                        addData("my_action", "SAY_HELLO")
                    })
                }
            }

        }

        override fun getItemCount(): Int {
            return posts.size
        }

    }

    companion object {
        const val TAG = "HomeFragment"
    }
}