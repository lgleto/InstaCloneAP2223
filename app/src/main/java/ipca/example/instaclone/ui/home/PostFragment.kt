package ipca.example.instaclone.ui.home

import android.app.Activity.RESULT_OK
import android.content.ComponentCallbacks
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import ipca.example.instaclone.databinding.FragmentPostBinding
import ipca.example.instaclone.models.Post
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
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
        dispatchTakePictureIntent()
        binding.fabSend.setOnClickListener {
            uploadFile{filename ->
                filename?.let {
                    Post(binding.editTextComment.text.toString(),
                        Date(),
                        Firebase.auth.currentUser?.uid?:"",
                        it
                    ).sendPost { error ->
                        error?.let {
                            Snackbar.make(binding.root,"Alguma coisa correu mal",Snackbar.LENGTH_LONG)
                        }?: kotlin.run {
                            findNavController().popBackStack()
                        }
                    }
                }?:run{
                    // apresentar erro
                }

            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ///val imageBitmap = data?.extras?.get("data") as Bitmap
            BitmapFactory.decodeFile(currentPhotoPath).apply {
                binding.imageViewPhoto.setImageBitmap(this)
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
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
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

        val uploadTask = storageRef.child("images/${file.lastPathSegment}")
            .putFile(file, metadata)

        uploadTask.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            Log.d(TAG, "Upload is $progress% done")
        }.addOnPausedListener {
            Log.d(TAG, "Upload is paused")
        }.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d(TAG, it.toString())
            callback(null)
        }.addOnSuccessListener {
            // Handle successful uploads on complete
            Log.d(TAG, it.uploadSessionUri.toString())
            callback(file.lastPathSegment)
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1001
        const val TAG = "PostFragment"
    }

}