package ipca.example.instaclone.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import ipca.example.instaclone.databinding.FragmentPostBinding
import ipca.example.instaclone.models.Post
import java.util.*

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabSend.setOnClickListener {
            Post(binding.editTextComment.text.toString(),
                Date(),
                Firebase.auth.currentUser?.uid?:"",
                ""
            ).sendPost { error ->
                error?.let {
                    Snackbar.make(binding.root,"Alguma coisa correu mal",Snackbar.LENGTH_LONG)
                }?: kotlin.run {
                    findNavController().popBackStack()
                }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}