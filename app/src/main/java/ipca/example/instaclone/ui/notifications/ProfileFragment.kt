package ipca.example.instaclone.ui.notifications

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import ipca.example.instaclone.databinding.FragmentProfileBinding
import ipca.example.instaclone.models.User
import ipca.example.instaclone.ui.home.HomeFragment
import ipca.example.instaclone.ui.home.PostFragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonChangePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.firestore
        db.collection("users").document(uid)
            .addSnapshotListener { value, error ->

            val user = value?.let {
                User.fromDoc(it)
            }
                binding.editTextName.setText(user?.username)
                binding.editTextEmail.setText(user?.email)
                user?.photoFilename?.let{
                    val storage = Firebase.storage
                    var storageRef = storage.reference
                    var islandRef = storageRef.child("userPhotos/${it}")

                    val ONE_MEGABYTE: Long = 10024 * 1024
                    islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {

                        val inputStream = it.inputStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageViewUserPhoto.setImageBitmap(bitmap)
                    }.addOnFailureListener {
                        // Handle any errors
                        Log.d(HomeFragment.TAG, it.toString() )
                    }
                }
            }


        val focusChange = object : OnFocusChangeListener {
            override fun onFocusChange(view: View?, hasFocus: Boolean) {
                if (!hasFocus){
                    when(view){
                        binding.editTextName -> {
                            User.postField(binding.editTextName.text.toString(), "username")
                        }
                        binding.editTextEmail -> {
                            User.postField(binding.editTextEmail.text.toString(), "email")
                        }
                    }
                }
            }
        }

        binding.editTextName.onFocusChangeListener = focusChange
        binding.editTextEmail.onFocusChangeListener = focusChange
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PostFragment.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            ///val imageBitmap = data?.extras?.get("data") as Bitmap
            BitmapFactory.decodeFile(currentPhotoPath).apply {
                binding.imageViewUserPhoto.setImageBitmap(this)
                uploadFile {
                    if (it != null) {
                        User.postField(it, "photoFilename")
                    }
                }
            }
        }else{
            findNavController().popBackStack()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(
                requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "ipca.example.instaclone.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, PostFragment.REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val userId: String = FirebaseAuth.getInstance().currentUser!!.uid
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${userId}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun uploadFile(callback: (String?)->Unit) {
        val storage = Firebase.storage
        var storageRef = storage.reference
        val file = Uri.fromFile(File(currentPhotoPath))
        var metadata = storageMetadata {
            contentType = "image/jpg"
        }

        val uploadTask = storageRef.child("userPhotos/${file.lastPathSegment}")
            .putFile(file, metadata)

        uploadTask.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            Log.d(PostFragment.TAG, "Upload is $progress% done")
        }.addOnPausedListener {
            Log.d(PostFragment.TAG, "Upload is paused")
        }.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d(PostFragment.TAG, it.toString())
            callback(null)
        }.addOnSuccessListener {
            // Handle successful uploads on complete
            Log.d(PostFragment.TAG, it.uploadSessionUri.toString())
            callback(file.lastPathSegment)
        }
    }
}