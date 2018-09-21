package com.example.archer.watchlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.Toast
import com.example.archer.watchlist.constants.API_INVALID_TITLE
import com.example.archer.watchlist.constants.API_NO_RESPONSE
import com.example.archer.watchlist.constants.DELETE_RECYCLER_ENTRY
import com.example.archer.watchlist.constants.OMDB_RESPONSE
import com.example.archer.watchlist.dialogs.DeleteDialog
import com.example.archer.watchlist.dialogs.DialogListener
import com.example.archer.watchlist.dialogs.NewChannelDialog
import com.example.archer.watchlist.dialogs.TitleDialog
import com.example.archer.watchlist.services.OmdbIntentService


class MainActivity : DialogListener, AppCompatActivity(){

    private val mMedia = ArrayList<Media>()
    lateinit var br: MyBroadcastReceiver
    lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        // Standard stuff
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Recycler View setup
        initImageBitmaps()

        broadcastReceiverSetup()
        navDrawerSetup()
    }

    private fun navDrawerSetup() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navView: NavigationView = findViewById(R.id.nav_view)
        val menu: Menu = navView.menu
        val subMenu =  menu.getItem(2).subMenu
        val createPlaylist = subMenu.getItem(0)
        createPlaylist.setOnMenuItemClickListener {
            openNewChannelDialog()
            return@setOnMenuItemClickListener  true
        }
    }

    private fun broadcastReceiverSetup() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        br = MyBroadcastReceiver(this, Handler(), mMedia, recyclerView.adapter as RecyclerViewAdapter)
        val filter = IntentFilter(OMDB_RESPONSE)
        filter.addAction(DELETE_RECYCLER_ENTRY)
        this.registerReceiver(br, filter)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    /**
     * Get the current channel's data and add it to mMedia to display once
     * the Recycler View has been initialized
     */
    fun initImageBitmaps() {

        val testMovie = Media(
                "PLACEHOLDER",
                "https://upload.wikimedia.org/wikipedia/commons/f/ff/Wikipedia_logo_593.jpg",
                "It's wikipedia what more do you want from meIt's wikipedia what more do you want from me"
        )


        mMedia.add(testMovie)

        initRecyclerView()
    }

    /**
     * Give the Recycler View its adapter and layout manager
     */
    fun initRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = RecyclerViewAdapter(this, mMedia)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Start up the service that retrieves media's information from Omdb. The handling
     * of that information is done in the Broadcast Receiver.
     *
     * @param title The title of the media the service should fetch
     */
    fun fetchFromOmdb(title: String) {
        Log.d("LEETAG", "fetching $title from omdb")
        val intent = Intent(this, OmdbIntentService::class.java)
        intent.putExtra("title", title)
        startService(intent)
    }

    /**
     * Open the dialog that prompts the user to enter the name of the media they want.
     *
     * @param view The button that the user pressed
     */
    fun openAddMediaDialog(view: View) {
        val inputDialog = TitleDialog()
        inputDialog.show(supportFragmentManager, "input dialog")
    }

    /**
     * Ask the user for confirmation before deleting an entry from the Recycler View.
     * Called from the ViewHolder
     *
     * @param position the position of the ViewHolder in the mMedia ArrayList
     */
    fun openDeleteMediaDialog(position: Int) {
        Log.d("LEETAG", "Opening delete media dialog")
        val args: Bundle = Bundle()
        args.putInt("position", position)
        val confirmDialog = DeleteDialog()
        confirmDialog.arguments = args
        confirmDialog.show(supportFragmentManager, "delete dialog")
    }

    fun openNewChannelDialog() {
        Log.d("LEETAG", "Opening new channel dialog")
        val newChannelDialog = NewChannelDialog()
        newChannelDialog.show(supportFragmentManager, "new channel dialog")
    }

    /**
     * Called from the TitleDialog window once the user has submitted their title.
     *
     * @param title The title of the media the user is requesting
     */
    override fun applyNewMediaText(title: String) {
        fetchFromOmdb(title)
    }

    /**
     * Called from the NewChannelDialog, adds a new channel to the navigation drawer
     *
     * @param name the name of the new channel
     */
    override fun applyNewChannelText(name: String) {
        val navView: NavigationView = findViewById(R.id.nav_view)
        val subMenu: SubMenu =  navView.menu.getItem(2).subMenu

        // We use titles to index channels so their names cannot be identical
        if(isChannelNameRepeat(name, subMenu)) {
            Toast.makeText(this, "Channels cannot share names", Toast.LENGTH_LONG).show()
            return
        }

        val newItem: MenuItem = subMenu.add(name)
        newItem.setIcon(R.drawable.ic_playlist_play_black_24dp)

        newItem.setOnMenuItemClickListener {
            Toast.makeText(this, "wow I'm ${newItem.itemId}", Toast.LENGTH_SHORT).show()
            Log.d("LEETAG", "item clicked")
            return@setOnMenuItemClickListener true
        }
    }

    /**
     * Checks to see if any channels in the submenu share a name with the given channel.
     * Used by applyNewChannelName to reject repeat channel names.
     *
     * @param name the name we fcheck for
     * @param subMenu the subMenu to search through for repeat names
     */
    private fun isChannelNameRepeat(name: String, subMenu: SubMenu) : Boolean {
        for(i in 0 until subMenu.size()) {
            if (subMenu.getItem(i) != null && subMenu.getItem(i).title == name) {
                return true
            }
        }
        return false
    }

    /**
     * Called from the DeleteDialog, prompting the main activity to delete an item from the
     * mMedia ArrayList and update the recycler view to reflect the changes.
     *
     * @param position The position of the item in the mMedia ArrayList
     */
    override fun removeItem(position: Int) {
        mMedia.removeAt(position)
        val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter?.notifyDataSetChanged()
    }
}


/**
 * The class that receives and handles the intent sent from the OmdbIntentService to the MainActivity.
 */
class MyBroadcastReceiver(
        val mContext: Context,
        val mHandler: Handler,
        val mMedia: ArrayList<Media>,
        val mAdapter: RecyclerViewAdapter
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("LEETAG", "received something")
        if (intent == null) {
            Toast.makeText(mContext, "Error retrieving data from omdb", Toast.LENGTH_SHORT).show()
            return
        }

        when (intent.action) {
            OMDB_RESPONSE -> handleOmdbResponse(context, intent)
        }

    }

    private fun handleOmdbResponse(context: Context?, intent: Intent) {
        when (intent.getIntExtra("status", -1)) {
            //  intent had no status extra
            -1 -> { Toast.makeText(mContext, "Error retrieving data from omdb", Toast.LENGTH_SHORT).show()
                return}
            API_INVALID_TITLE -> { Toast.makeText(mContext, "Invalid title", Toast.LENGTH_SHORT).show()
                return}
            API_NO_RESPONSE -> { Toast.makeText(mContext, "Error retrieving data from omdb", Toast.LENGTH_SHORT).show()
                return}
        }

        // else
        val newMedia = Media(
                title = intent.getStringExtra("title"),
                imageLink = intent.getStringExtra("imageUrl"),
                summary = intent.getStringExtra("plot"),
                year = intent.getStringExtra("year")
        )
        mMedia.add(newMedia)
        mAdapter.notifyDataSetChanged()
    }
}


