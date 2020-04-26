package com.example.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    val regex = Regex( "[0-9a-zA-Z-_]([.]?[0-9a-zA-Z-_])*@[0-9a-zA-Z]+[.][a-zA-Z]{2,3}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance();


        email_edittext.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(password_edittext.text.toString().length >= 6 && email_edittext.text.toString() != ""){
                    email_signin_button.setBackgroundResource(R.color.email_signin_button_color)
                    email_signin_button.isEnabled = true
                }
                else{
                    email_signin_button.setBackgroundResource(R.color.disable_email_signin_button_color)
                    email_signin_button.isEnabled = false
                }
            }
        })

         password_edittext.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(password_edittext.text.toString().length >= 6 && email_edittext.text.toString() != ""){
                    email_signin_button.setBackgroundResource(R.color.email_signin_button_color)
                    email_signin_button.isEnabled = true
                }
                else{
                    email_signin_button.setBackgroundResource(R.color.disable_email_signin_button_color)
                    email_signin_button.isEnabled = false
                }
            }
        })

        email_signup_button.setOnClickListener {
            if(!regex.matches(email_edittext.text.toString()))
                Toast.makeText(this,"올바른 형식의 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            else signupByEmail()
        }


        email_signin_button.setOnClickListener {
            if(!regex.matches(email_edittext.text.toString()))
                Toast.makeText(this,"올바른 형식의 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            else signinByEmail()
        }
        google_login_button.setOnClickListener {
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStart() {
        super.onStart()
            moveMainPage(firebaseAuth?.currentUser)
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login
                    moveMainPage(task.result?.user)
                } else {
                    //error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun signupByEmail() {
        firebaseAuth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Creating a user account
                    moveMainPage(task.result?.user)
                } else {
                    //error message
                    Toast.makeText(this,"사용할 수 없는 아이디입니다. 다른 아이디를 사용하세요.", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun signinByEmail() {
        firebaseAuth?.signInWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login
                    moveMainPage(task.result?.user)
                } else {
                    //error message
                    Toast.makeText(this, "가입하지 않은 아이디이거나, 잘못된 비밀번호입니다.", Toast.LENGTH_LONG).show()
                }
            }

    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}




