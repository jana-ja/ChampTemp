package de.janaja.champtemp.ui


import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.janaja.champtemp.data.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class TempHumiViewModel (application: Application) : AndroidViewModel(application) {
    private val TAG = "ViewModel"
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repo = Repository()

    private var _currentUser: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    val tempHumis = repo.tempHumis

    init {
        _currentUser.value = firebaseAuth.currentUser
    }


    fun signIn(email: String, password: String){
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    _currentUser.value = firebaseAuth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        getApplication(), "Authentication failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun signUp(email: String, password: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    _currentUser.value = firebaseAuth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        getApplication(), "Authentication failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun logout() {
        firebaseAuth.signOut()
        //_currentUser.value = firebaseAuth.currentUser
        _currentUser.value = null
    }

    fun getAll(){
        repo.loadTempHumiData()
    }
}