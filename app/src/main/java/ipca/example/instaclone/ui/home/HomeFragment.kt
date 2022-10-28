package ipca.example.instaclone.ui.home

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
import ipca.example.instaclone.R
import ipca.example.instaclone.databinding.FragmentHomeBinding
import ipca.example.instaclone.databinding.RowPostBinding
import ipca.example.instaclone.models.Post
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
                textViewTitle.text = item.comment
                textViewDate.text = item.postDate.toString()
            }
        }

        override fun getItemCount(): Int {
            return posts.size
        }

    }
}