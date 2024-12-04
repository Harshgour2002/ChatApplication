package com.example.visionconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.visionconnect.databinding.ActivityMainBinding
import com.example.visionconnect.fragments.userAdapter
import com.example.visionconnect.room.DAO
import com.example.visionconnect.room.appDatabase
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var PagerAdapter : pagerAdapter
    private lateinit var addedUserList : ArrayList<String>
    private lateinit var dao : DAO
    var chatId:String?=""
    var senderName:String?=""
    var senderPic:String?=""
    var senderEmail:String?=""
    var lastMessge:String?=""
    var Time:String?=" "


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        var storedEmail = sharedPref.getString("email", null)
        val email = intent.getStringExtra("Email")
        dao = appDatabase.getDatabase(this).chatDao()

        if (storedEmail == null && email == null) {
            startActivity(Intent(this, startScreen::class.java))
            finish()
        }

        chatId = intent.getStringExtra("chatId")
        senderName = intent.getStringExtra("senderName")
        senderPic = intent.getStringExtra("senderPic")
        senderEmail = intent.getStringExtra("senderEmail")
        lastMessge = intent.getStringExtra("lastMessage")
        Time = intent.getStringExtra("time")


        if (storedEmail == null && email != null) {
            sharedPref.edit()
                .putString("email", email)
                .apply()
            storedEmail = email

        }

        // FAB button action
        binding.FAB.setOnClickListener {
            val intent = Intent(this, allUserList::class.java)
            addedUserList = arrayListOf()  // Initialize addedUserList

            lifecycleScope.launch {
                // Collect data asynchronously from the database
                dao.getAllChats(storedEmail!!).collect { mList ->
                    mList.forEach { chat ->
                        chat.senderEmail?.let {
                            addedUserList.add(it)  // Add senderEmail to the list
                        }
                    }

                    // Log the updated list inside the collect block, after it is populated
                    Log.d("this is to test the list", "$addedUserList")

                    // Once the list is populated, start the next activity
                    intent.putExtra("addedUserList", addedUserList)  // Send the list to the next activity
                    intent.putExtra("currentUserEmail", storedEmail)
                    startActivity(intent)
                }
            }
        }



        //request button action
        binding.notification.setOnClickListener {
            val intent = Intent(this, notification::class.java)
            intent.putExtra("currentUser", storedEmail)
            startActivity(intent)
        }

        //addedRequest button action
        binding.addRequest.setOnClickListener {
            val intent = Intent(this, acceptRequest::class.java)
            intent.putExtra("currentUser", storedEmail)
            startActivity(intent)
        }

        // Logout button action
        binding.logout.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            val view =
                layoutInflater.inflate(R.layout.logout_dialog, null, false) // Use false for root

            builder.setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            dialog.show()

            // Access yes button in the dialog view
            val yesButton = view.findViewById<Button>(R.id.yesBtn)
            yesButton.setOnClickListener {
                sharedPref.edit().remove("email").apply()
                startActivity(Intent(this, startScreen::class.java))
                finish()
                dialog.dismiss()
            }

            // Access cancel button in the dialog view
            val cancelButton = view.findViewById<ImageButton>(R.id.cancleButton)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        //loading the viewpager2
        loadViewPager2(storedEmail!!)

    }
    private fun loadViewPager2(storedEmail:String) {
        PagerAdapter = pagerAdapter(supportFragmentManager, lifecycle,chatId, senderName, senderPic,
            storedEmail
        )

        binding.tablayout.addTab(binding.tablayout.newTab().setText("Chats"))
        binding.tablayout.addTab(binding.tablayout.newTab().setText("Calls"))
        binding.viewPager2.adapter = PagerAdapter

        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager2.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tablayout.selectTab(binding.tablayout.getTabAt(position))

                if(position == 1) binding.FAB.hide() //this mean the call fragment is displayed
                else binding.FAB.show()//this means the chats fragment is displayed
            }
        })
    }
}
