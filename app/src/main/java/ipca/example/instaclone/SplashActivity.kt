package ipca.example.instaclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ipca.example.instaclone.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch (Dispatchers.IO){
            delay(2000)
            lifecycleScope.launch (Dispatchers.Main) {
                val auth = Firebase.auth
                val currentUser = auth.currentUser
                if (currentUser != null){
                    startActivity(
                        Intent(this@SplashActivity,
                            MainActivity::class.java)
                    )

                }else{
                    startActivity(
                        Intent(this@SplashActivity,
                            LoginActivity::class.java)
                    )
                }
                finish()
            }

        }

    }
}