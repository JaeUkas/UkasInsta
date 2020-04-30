package com.example.instagramclone.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagramclone.MainActivity
import com.example.instagramclone.R
import com.example.instagramclone.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerviewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    alarmDTOList.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        if (snapshot.toObject(AlarmDTO::class.java)!!.uid != uid)
                            alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustoViewHolder(view)
        }

        inner class CustoViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            FirebaseFirestore.getInstance().collection("profileImages")
                .document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop())
                            .into(view.commentviewitem_imageview_profile)
                    }
                }
            view.commentviewitem_imageview_profile.setOnClickListener{
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", alarmDTOList[position].uid)
                bundle.putString("userId", alarmDTOList[position].userId)
                fragment.arguments = bundle

                changeFragment(fragment)
                addToList(fragment)
            }
            when (alarmDTOList[position].kind) {
                0 -> {
                    val str = alarmDTOList[position].userId + "님이 회원님의 게시물을 좋아합니다"
                    view.commentviewitem_textview_profile.text = str
                }
                1 -> {
                    val str =
                        alarmDTOList[position].userId + "님이 댓글을 남겼습니다: " + alarmDTOList[position].message
                    view.commentviewitem_textview_profile.text = str
                }
                2 -> {
                    val str = alarmDTOList[position].userId + "님이 회원님을 팔로우하기 시작했습니다"
                    view.commentviewitem_textview_profile.text = str
                }
            }
            view.commentviewitem_textview_comment.visibility = View.INVISIBLE


        }
        fun changeFragment(fragment: Fragment){
            for (fragment in activity!!.supportFragmentManager.fragments) {
                if (fragment != null && fragment.isVisible) {
                    activity?.supportFragmentManager
                        ?.beginTransaction()
                        ?.hide(fragment)
                        ?.commitNow()
                }
            }
            when (activity!!.bottom_navigation.selectedItemId) {
                R.id.action_home -> {
                    activity?.supportFragmentManager?.beginTransaction()?.add(
                        R.id.main_content, fragment,
                        MainActivity.homeFragmentList.size.toString()
                    )?.commitNow()
                }
                R.id.action_search -> {
                    activity?.supportFragmentManager?.beginTransaction()?.add(
                        R.id.main_content, fragment,
                        MainActivity.searchFragmentList.size.toString()
                    )?.commitNow()
                }
                R.id.action_alarm -> {
                    activity?.supportFragmentManager?.beginTransaction()?.add(
                        R.id.main_content, fragment,
                        MainActivity.alarmFragmentList.size.toString()
                    )?.commitNow()
                }
                R.id.action_account -> {
                    activity?.supportFragmentManager?.beginTransaction()?.add(
                        R.id.main_content, fragment,
                        MainActivity.accountFragmentList.size.toString()
                    )?.commitNow()
                }
            }
        }

        fun addToList(fragment: Fragment) {
            when (activity!!.bottom_navigation.selectedItemId) {
                R.id.action_home -> {
                    MainActivity.homeFragmentList.add(fragment.tag!!)
                }
                R.id.action_search -> {
                    MainActivity.searchFragmentList.add(fragment.tag!!)
                }
                R.id.action_alarm -> {
                    MainActivity.alarmFragmentList.add(fragment.tag!!)
                }
                R.id.action_account -> {
                    MainActivity.accountFragmentList.add(fragment.tag!!)
                }
            }
        }

    }
}
