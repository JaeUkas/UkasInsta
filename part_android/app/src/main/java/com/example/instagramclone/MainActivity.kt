package com.example.instagramclone

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.instagramclone.navigation.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

var selectedItem: Int = 0

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    var detailViewFragment: DetailViewFragment? = null
    var gridFragment: GridFragment? = null
    var alarmFragment: AlarmFragment? = null
    var userFragment: UserFragment? = null

    var fragmentManager: FragmentManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentManager = supportFragmentManager

        bottom_navigation.setOnNavigationItemSelectedListener(this)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        bottom_navigation.selectedItemId = R.id.action_home


        selectedItem = bottom_navigation.selectedItemId
        // registerPushToken()

    }

    override fun onBackPressed() {
        if (selectedItem == R.id.action_home) {
            moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
            finishAndRemoveTask();                        // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());
        } else bottom_navigation.selectedItemId = R.id.action_home
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        setToolbarDefault()
        selectedItem = p0.itemId
        when (p0.itemId) {
            R.id.action_home -> {
                if (detailViewFragment == null) {
                    detailViewFragment = DetailViewFragment()
                    fragmentManager!!.beginTransaction()
                        .add(R.id.main_content, detailViewFragment!!).commit()
                }
                if (gridFragment != null) fragmentManager!!.beginTransaction()
                    .hide(gridFragment!!).commit()
                if (alarmFragment != null) fragmentManager!!.beginTransaction()
                    .hide(alarmFragment!!).commit()
                if (userFragment != null) fragmentManager!!.beginTransaction()
                    .hide(userFragment!!).commit()
                if (detailViewFragment != null) fragmentManager!!.beginTransaction()
                    .show(detailViewFragment!!).commit()

                return true
            }
            R.id.action_search -> {
                if (gridFragment == null) {
                    gridFragment = GridFragment()
                    fragmentManager!!.beginTransaction().add(R.id.main_content, gridFragment!!)
                        .commit()
                }
                if (detailViewFragment != null) fragmentManager!!.beginTransaction()
                    .hide(detailViewFragment!!).commit()
                if (alarmFragment != null) fragmentManager!!.beginTransaction()
                    .hide(alarmFragment!!).commit()
                if (userFragment != null) fragmentManager!!.beginTransaction().hide(userFragment!!)
                    .commit()
                if (gridFragment != null) fragmentManager!!.beginTransaction().show(gridFragment!!)
                    .commit()

                return true
            }
            R.id.action_add_photo -> {

                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1
                    )
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startActivity(Intent(this, AddPhotoActivity::class.java))
                    }
                }
                return false
            }
            R.id.action_favorite_alarm -> {
                if (alarmFragment == null) {
                    alarmFragment = AlarmFragment()
                    fragmentManager!!.beginTransaction().add(R.id.main_content, alarmFragment!!)
                        .commit()
                }
                if (gridFragment != null) fragmentManager!!.beginTransaction()
                    .hide(gridFragment!!).commit()
                if (detailViewFragment != null) fragmentManager!!.beginTransaction()
                    .hide(detailViewFragment!!).commit()
                if (userFragment != null) fragmentManager!!.beginTransaction()
                    .hide(userFragment!!).commit()
                if (alarmFragment != null) fragmentManager!!.beginTransaction()
                    .show(alarmFragment!!).commit()

                return true
            }
            R.id.action_account -> {
                if (userFragment == null) {
                    userFragment = UserFragment()
                    var bundle = Bundle()
                    var uid = FirebaseAuth.getInstance().currentUser?.uid
                    bundle.putString("destinationUid", uid)
                    userFragment!!.arguments = bundle
                    fragmentManager!!.beginTransaction().add(R.id.main_content, userFragment!!)
                        .commit()
                }
                if (gridFragment != null) fragmentManager!!.beginTransaction()
                    .hide(gridFragment!!).commit()
                if (alarmFragment != null) fragmentManager!!.beginTransaction()
                    .hide(alarmFragment!!).commit()
                if (detailViewFragment != null) fragmentManager!!.beginTransaction()
                    .hide(detailViewFragment!!).commit()
                if (userFragment != null) fragmentManager!!.beginTransaction()
                    .show(userFragment!!).commit()

                // var userFragment = UserFragment()
                // var bundle = Bundle()
                // var uid = FirebaseAuth.getInstance().currentUser?.uid
                // bundle.putString("destinationUid", uid)
                // userFragment.arguments = bundle
                // supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment)
                //     .commit()
                return true
            }
        }
        return false
    }

    fun setToolbarDefault() {
        toolbar_username.visibility = View.GONE
        toolbar_btn_back.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
    }

    /* fun registerPushToken() {
         FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
             val token = task.result?.token
             val uid = FirebaseAuth.getInstance().currentUser?.uid
             val map = mutableMapOf<String, Any>()
             map["pushToken"] = token!!

             FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)

         }
     }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance()?.uid
            var storageRef =
                FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String, Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }
}
