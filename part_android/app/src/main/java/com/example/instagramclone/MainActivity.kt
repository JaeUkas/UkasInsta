package com.example.instagramclone

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.isDigitsOnly
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
import kotlinx.android.synthetic.main.fragment_alarm.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_grid.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.fragment_user.view.account_recyclerview
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    var detailViewFragment: DetailViewFragment? = null
    var gridFragment: GridFragment? = null
    var alarmFragment: AlarmFragment? = null
    var userFragment: UserFragment? = null
    val fragmentManager = supportFragmentManager

    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null

    var clickButtonList : ArrayList<Int>? = null
    companion object {
        var homeFragmentList: ArrayList<String> = arrayListOf()
        var searchFragmentList: ArrayList<String> = arrayListOf()
        var alarmFragmentList: ArrayList<String> = arrayListOf()
        var accountFragmentList: ArrayList<String> = arrayListOf()

        var selectedItem: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth?.uid

        clickButtonList = arrayListOf()
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
        when (bottom_navigation.selectedItemId) {
            R.id.action_home -> {
                changeFragment(homeFragmentList)
            }
            R.id.action_search -> {
                changeFragment(searchFragmentList)
            }
            R.id.action_alarm -> {
                changeFragment(alarmFragmentList)
            }
            R.id.action_account -> {
                changeFragment(accountFragmentList)
            }
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        setToolbarDefault()
        if(p0.itemId != R.id.action_add_photo) {
            if (clickButtonList!!.contains(p0.itemId)) {
                clickButtonList!!.remove(p0.itemId)
                clickButtonList!!.add(p0.itemId)
            } else clickButtonList!!.add(p0.itemId)

            for (fragment in fragmentManager.fragments) {
                if (fragment != null && fragment.isVisible)
                    fragmentManager.beginTransaction().hide(fragment).commitNow()
            }
        }

        when (p0.itemId) {
            R.id.action_home -> {
                if (detailViewFragment == null) {
                    detailViewFragment = DetailViewFragment()

                    fragmentManager!!.beginTransaction()
                        .add(R.id.main_content, detailViewFragment!!, "action_home").commitNow()
                    homeFragmentList.add(detailViewFragment!!.tag!!)
                }

                if (detailViewFragment != null) {
                    if(selectedItem == p0.itemId){
                        deleteFragment(homeFragmentList, detailViewFragment!!)
                    }

                    fragmentManager.beginTransaction()
                        .show(fragmentManager.findFragmentByTag(homeFragmentList.last())!!).commitNow()
                    if(selectedItem == p0.itemId) detailViewFragment!!.detailviewfragment_recyclerview.smoothScrollToPosition(detailViewFragment!!.detailviewfragment_recyclerview.height)
                }
                selectedItem = p0.itemId
                return true
            }
            R.id.action_search -> {
                if (gridFragment == null) {
                    gridFragment = GridFragment()
                    fragmentManager.beginTransaction()
                        .add(R.id.main_content, gridFragment!!, "action_search")
                        .commitNow()
                    searchFragmentList.add(gridFragment!!.tag!!)
                }

                if (gridFragment != null) {
                    if(selectedItem == p0.itemId){
                        deleteFragment(searchFragmentList,gridFragment!!)
                    }
                    fragmentManager.beginTransaction()
                        .show(fragmentManager.findFragmentByTag(searchFragmentList.last())!!).commitNow()
                    if(selectedItem == p0.itemId) gridFragment!!.gridfragment_recyclerview.smoothScrollToPosition(0)
                }
                selectedItem = p0.itemId
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
            R.id.action_alarm -> {
                if (alarmFragment == null) {
                    alarmFragment = AlarmFragment()
                    fragmentManager.beginTransaction()
                        .add(R.id.main_content, alarmFragment!!, "favorite_alarm")
                        .commitNow()
                    alarmFragmentList.add(alarmFragment!!.tag!!)
                }

                if (alarmFragment != null) {
                    if(selectedItem == p0.itemId){
                        deleteFragment(alarmFragmentList,alarmFragment!!)
                    }
                    fragmentManager.beginTransaction()
                        .show(fragmentManager.findFragmentByTag(alarmFragmentList.last())!!).commitNow()
                    if(selectedItem == p0.itemId) alarmFragment!!.alarmfragment_recyclerview.smoothScrollToPosition(0)
                }
                selectedItem = p0.itemId
                return true
            }
            R.id.action_account -> {
                if (userFragment == null) {
                    userFragment = UserFragment()
                    var bundle = Bundle()
                    var uid = FirebaseAuth.getInstance().currentUser?.uid
                    bundle.putString("destinationUid", uid)
                    userFragment!!.arguments = bundle
                    fragmentManager.beginTransaction()
                        .add(R.id.main_content, userFragment!!, "action_account")
                        .commitNow()
                    accountFragmentList.add(userFragment!!.tag!!)
                }

                if (userFragment != null) {
                    if(selectedItem == p0.itemId){
                        deleteFragment(accountFragmentList, userFragment!!)
                    }
                    fragmentManager.beginTransaction()
                        .show(fragmentManager.findFragmentByTag(accountFragmentList.last())!!).commitNow()
                    if(selectedItem == p0.itemId) userFragment!!.account_recyclerview.smoothScrollToPosition(0)
                }
                selectedItem = p0.itemId
                return true
            }

        }
        return false
    }

    fun deleteFragment(arrayList: ArrayList<String>, fragment: Fragment){
        for(str in arrayList){
            if(str == arrayList[0]) continue
            fragmentManager.beginTransaction()
                .remove(fragmentManager.findFragmentByTag(str)!!).commitNow()
        }
        arrayList.clear()
        arrayList.add(fragment!!.tag!!)
    }

    fun changeFragment(arrayList: ArrayList<String>){
        if (arrayList.size <= 1) {
            clickButtonList!!.remove(clickButtonList!!.last())
            if(clickButtonList!!.last() != null) bottom_navigation.selectedItemId = clickButtonList!!.last()
            else{
            moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
            finishAndRemoveTask();                        // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());
            }
            return
        }
        fragmentManager.beginTransaction()
            .remove(fragmentManager.findFragmentByTag(arrayList.last())!!).commitNow()
        arrayList.remove(arrayList.last())
        if (arrayList.size > 0) fragmentManager.beginTransaction()
            .show(fragmentManager.findFragmentByTag(arrayList.last())!!).commitNow()
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
            storageRef.putFile(imageUri!!).continueWithTask {
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String, Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }
}
