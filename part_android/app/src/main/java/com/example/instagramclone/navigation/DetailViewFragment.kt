package com.example.instagramclone.navigation

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagramclone.MainActivity.Companion.accountFragmentList
import com.example.instagramclone.MainActivity.Companion.alarmFragmentList
import com.example.instagramclone.MainActivity.Companion.homeFragmentList
import com.example.instagramclone.MainActivity.Companion.searchFragmentList
import com.example.instagramclone.R
import com.example.instagramclone.navigation.model.AlarmDTO
import com.example.instagramclone.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_alarm.*
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_detail.view.detailviewfragment_swipe
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()

        var mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.reverseLayout = true
        mLayoutManager.stackFromEnd = true
        view.detailviewfragment_recyclerview.layoutManager = mLayoutManager

        view.detailviewfragment_swipe.setOnRefreshListener {
            view.detailviewfragment_swipe?.isRefreshing = false
        }

        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//왜 커스텀뷰홀더를 만들어서 복잡하게 해야하나 ?? 리사이클러뷰를 사용할 때 메모리를 적게 사용하기 위해서 커스텀뷰홀더를 만들어달라는 일종의 약속. 문법이랑은 상관x
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //서버에서 넘어온 정보를 매핑시켜주는 부분
            var viewHolder = (holder as CustomViewHolder).itemView
            viewHolder.detailviewitem_profile_textview.text = contentDTOs!![position].userId

            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .into(viewHolder.detailviewitem_imageview_content)

            if (contentDTOs[position].explain!!.isNotEmpty()) {
                viewHolder.detailviewitem_explain_textview.visibility = View.VISIBLE
                val str =
                    "<b>" + contentDTOs[position].userId + "</b> " + contentDTOs[position].explain
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    viewHolder.detailviewitem_explain_textview.text =
                        Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    viewHolder.detailviewitem_explain_textview.text = Html.fromHtml(str).toString()
                }

                viewHolder.detailviewitem_explain_textview.text = Html.fromHtml(str)
            } else {
                viewHolder.detailviewitem_explain_textview.visibility = View.GONE
            }

            if (contentDTOs[position].favoriteCount == 0) {
                viewHolder.detailviewitem_favoritecounter_textview.visibility = View.GONE
            } else {
                viewHolder.detailviewitem_favoritecounter_textview.visibility = View.VISIBLE
                viewHolder.detailviewitem_favoritecounter_textview.text =
                    "좋아요 " + contentDTOs!![position].favoriteCount + "개"

            }

            firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot == null) return@addSnapshotListener

                    if (documentSnapshot.data != null) {
                        var url = documentSnapshot?.data!!["image"]
                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(viewHolder.detailviewitem_profile_image)
                    }
                }

            viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }

            if (contentDTOs!![position].favorites.containsKey(uid)) {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            } else {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            viewHolder.detailviewitem_profile.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle

                changeFragment(fragment)
                addToList(fragment)
            }
            viewHolder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)

            }
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
                        homeFragmentList.size.toString()
                    )?.commitNow()
                }
                R.id.action_search -> {
                    activity?.supportFragmentManager?.beginTransaction()?.add(
                        R.id.main_content, fragment,
                        searchFragmentList.size.toString()
                    )?.commitNow()
                }
                R.id.action_alarm -> {
                    activity?.supportFragmentManager?.beginTransaction()?.add(
                        R.id.main_content, fragment,
                        alarmFragmentList.size.toString()
                    )?.commitNow()
                }
                R.id.action_account -> {
                    activity?.supportFragmentManager?.beginTransaction()?.add(
                        R.id.main_content, fragment,
                        accountFragmentList.size.toString()
                    )?.commitNow()
                }
            }
        }


        fun addToList(fragment: Fragment) {
            when (activity!!.bottom_navigation.selectedItemId) {
                R.id.action_home -> {
                    homeFragmentList.add(fragment.tag!!)
                }
                R.id.action_search -> {
                    searchFragmentList.add(fragment.tag!!)
                }
                R.id.action_alarm -> {
                    alarmFragmentList.add(fragment.tag!!)
                }
                R.id.action_account -> {
                    accountFragmentList.add(fragment.tag!!)
                }
            }
        }

        fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                } else {
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid: String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}
