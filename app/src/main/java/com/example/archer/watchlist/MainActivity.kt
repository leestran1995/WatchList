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
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.example.archer.watchlist.constants.API_INVALID_TITLE
import com.example.archer.watchlist.constants.API_NO_RESPONSE
import com.example.archer.watchlist.constants.DELETE_RECYCLER_ENTRY
import com.example.archer.watchlist.constants.OMDB_RESPONSE
import com.example.archer.watchlist.dialogs.*
import com.example.archer.watchlist.services.OmdbIntentService
import com.example.archer.watchlist.services.OmdbSearchResponse
import java.io.*


class MainActivity : DialogListener, AppCompatActivity(){

    private var mMedia = ArrayList<Media>()
    lateinit var br: MyBroadcastReceiver
    lateinit var drawer: DrawerLayout
    private var mChannels = HashMap<String, Channel>()
    private lateinit var mCurrentChannel: Channel
    private val channelSubmenuIndex: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        // Standard stuff
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
         * The order here is important. We make the RecyclerView first since the
         * Broadcast Receiver needs the RecyclerView. Then we get the saved channels
         * (if they exist) and re-initialize the Recycler View to reflect the newly
         * loaded channels.
         */
        initRecyclerView()
        broadcastReceiverSetup()

        getSavedChannels()

        // Recycler View setup
        initRecyclerView()
        toolbarSetup()
        navDrawerSetup()
    }


    override fun onResume() {
        val filter = IntentFilter(OMDB_RESPONSE)
        filter.addAction(DELETE_RECYCLER_ENTRY)
        this.registerReceiver(br, filter)
        super.onResume()
    }
    override fun onPause() {
        this.unregisterReceiver(br)
        super.onPause()
    }
    override fun onStop() {
        Log.d("LEETAG", "Destroying process, saving files")
        saveChannelsToFile()

        try {
            this.unregisterReceiver(br)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }

    /**
     * Get the users saved channels if they exist. If they don't the user will
     * only see the Default Channel when the app is opened.
     */
    fun getSavedChannels() {
        try {
            val file = File(filesDir, "savedChannels.dat")
            val fis = FileInputStream(file)
            val ois = ObjectInputStream(fis)


            mChannels = ois.readObject() as HashMap<String, Channel>
            ois.close()
            fis.close()

            for((key, value) in mChannels) {
                applyNewChannelText(key, 1)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.d("LEETAG", "Error: File not found")
        } catch (i: IOException) {
            Log.d("LEETAG", "Error when reading file")
        }
    }
    /**
     * When the app is closed/killed, save all of the user's channels to their device
     * to be retrieved the next time they open the app.
     */
    fun saveChannelsToFile() {
        try {
            val file = File(filesDir, "savedChannels.dat")
            val fos = FileOutputStream(file, false)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(mChannels)
            oos.close()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("LEETAG", "Error: Error when writing file")
        }
    }

    /**
     * Pass an intent to the Play Activity, which shows the user whatever is "on"
     * when they start the play activity.
     */
    private fun startPlayActivity() {
        if(mCurrentChannel.media.size == 0) {
            Toast.makeText(this, "Cannot play an empty channel", Toast.LENGTH_SHORT)
            return
        }
        val outIntent = Intent(this, PlayActivity::class.java)
        outIntent.putExtra("channel", mCurrentChannel)
        startActivity(outIntent)
    }

    /**
     * Set up the toolbar programmatically, adding the "play" and "delete" buttons,
     * in addition the the hamburger button that will open the navigation drawer.
     */
    private fun toolbarSetup() {
        // Basic Navigation Drawer setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        // We made the toolbar programmatically so I guess we're gonna make the buttons
        // programmatically because I didn't learn anything in school.
        val playButton = Button(this)
        playButton.setBackgroundResource(R.drawable.play_arrow_white)
        val playButtonParams = Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT)
        playButtonParams.gravity = Gravity.RIGHT
        playButtonParams.rightMargin = 50
        playButtonParams.width = 75
        playButtonParams.height = 75
        playButton.layoutParams = playButtonParams
        playButton.setOnClickListener {
            startPlayActivity()
        }
        toolbar.addView(playButton)

        val deleteButton = Button(this)
        deleteButton.setBackgroundResource(R.drawable.delete_white)
        val deleteButtonParams = Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT)
        deleteButtonParams.gravity = Gravity.RIGHT
        deleteButtonParams.rightMargin = 25
        deleteButtonParams.width = 75
        deleteButtonParams.height = 75
        deleteButton.layoutParams = deleteButtonParams
        deleteButton.setOnClickListener {
            openDeleteChannelDialog()
        }
        toolbar.addView(deleteButton)

        val toggle = ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }


    /**
     * Initialize the navigation drawer, adding a Default channel if there are no channels
     * in the app yet.
     */
    private fun navDrawerSetup() {

        // Create channel submenu programmatically
        // Create playlist button
        val navView: NavigationView = findViewById(R.id.nav_view)
        val menu: Menu = navView.menu

        val helpMenu = menu.getItem(0)
        //val helpButton = helpMenu.getItem(0)
        helpMenu.setOnMenuItemClickListener {
            openHelpDialog()
            return@setOnMenuItemClickListener true
        }

        val subMenu =  menu.getItem(1).subMenu
        val createPlaylist = subMenu.getItem(0)
        createPlaylist.setOnMenuItemClickListener {
            openNewChannelDialog()
            return@setOnMenuItemClickListener  true
        }

        // Default Channel if no channels exist yet
        if(mChannels.size == 0) {
            val defaultMenu: MenuItem = subMenu.add(0, subMenu.size(), 0, "Default")
            defaultMenu.setIcon(R.drawable.ic_playlist_play_black_24dp)
            defaultMenu.isChecked = true

            // Mark a channel as active and change to that channel when it is clicked
            defaultMenu.setOnMenuItemClickListener {
                for (i in 0 until subMenu.size()) {
                    subMenu.getItem(i).isChecked = false
                }
                defaultMenu.isChecked = true
                changeChannel(defaultMenu.title as String)
                return@setOnMenuItemClickListener true
            }

            mChannels["Default"] = Channel("Default", mMedia)
            mCurrentChannel = mChannels["Default"]!!
        }
    }


    /**
     * Set up the Broadcast Receiver which handles responses from the Omdb service
     */
    private fun broadcastReceiverSetup() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        br = MyBroadcastReceiver(this, Handler(), mMedia, recyclerView)
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
     * Give the Recycler View its adapter and layout manager
     */
    fun initRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = RecyclerViewAdapter(this, mMedia)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter.notifyDataSetChanged()
    }

    /**
     * Start up the service that retrieves media's information from Omdb. The handling
     * of that information is done in the Broadcast Receiver.
     *
     * @param title The title of the media the service should fetch
     */
    fun fetchFromOmdb(title: String) {
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

    private fun openHelpDialog() {
        val helpDialog = HelpDialog()
        helpDialog.show(supportFragmentManager, "help dialog")
    }
    /**
     * Ask the user for confirmation before deleting an entry from the Recycler View.
     * Called from the ViewHolder
     *
     * @param position the position of the ViewHolder in the mMedia ArrayList
     */
    fun openDeleteMediaDialog(position: Int) {
        val args = Bundle()
        args.putInt("position", position)
        val confirmDialog = DeleteDialog()
        confirmDialog.arguments = args
        confirmDialog.show(supportFragmentManager, "delete dialog")
    }
    private fun openDeleteChannelDialog() {
        val args = Bundle()
        args.putString("title", mCurrentChannel.title)
        val confirmDialog = DeleteChannelDialog()
        confirmDialog.arguments = args
        confirmDialog.show(supportFragmentManager, "delete channel dialog")
    }
    fun openNewChannelDialog() {
        val newChannelDialog = NewChannelDialog()
        newChannelDialog.show(supportFragmentManager, "new channel dialog")
    }

    override fun removeChannel(title: String?) {
        mChannels.remove(title)
        var newTitle = ""
        for((key, value) in mChannels) {
            newTitle = key
            break
        }

        val navView: NavigationView = findViewById(R.id.nav_view)
        val menu: Menu = navView.menu
        val subMenu =  menu.getItem(channelSubmenuIndex).subMenu

        // Hopefully the fact that we don't actually delete the channel from
        // the navigation drawer doesn't cause any issues
        for(i in 0 until subMenu.size()) {
            val tempItem = subMenu.getItem(i)
            if (tempItem.title == title) {
                tempItem.isVisible = false
            }
        }

        changeChannel(newTitle)

    }

    /**
     * Called from the TitleDialog window once the user has submitted their title.
     *
     * @param title The title of the media the user is requesting
     */
    override fun applyNewMediaText(title: String) {
        fetchFromOmdb(title)
        initRecyclerView()
    }

    /**
     * Called from the NewChannelDialog, adds a new channel to the navigation drawer
     *
     * @param name the name of the new channel
     * @param status used to indicate whether we need to add the new channels to
     *              mChannels. defaults to 0
     */
    override fun applyNewChannelText(name: String, status: Int) {
        val navView: NavigationView = findViewById(R.id.nav_view)
        val subMenu: SubMenu =  navView.menu.getItem(channelSubmenuIndex).subMenu

        // We use titles to index channels so their names cannot be identical
        if(isChannelNameRepeat(name, subMenu)) {
            Toast.makeText(this, "Channels cannot share names", Toast.LENGTH_LONG).show()
            return
        }

        // Navigation Bar
        val newItem: MenuItem = subMenu.add(0, subMenu.size(), 0, name)
        newItem.setIcon(R.drawable.ic_playlist_play_black_24dp)
        newItem.setOnMenuItemClickListener {
            for (i in  0 until subMenu.size()) {
                subMenu.getItem(i).isChecked = false
            }
            newItem.isChecked = true
            changeChannel(newItem.title as String)
            return@setOnMenuItemClickListener true
        }


        // Switch to the new channel right away
        for (i in  0 until subMenu.size()) {
            subMenu.getItem(i).isChecked = false
        }
        newItem.isChecked = true

        // Interior Logic
        if(status == 0) {
            mChannels[name] = Channel(name)
        }
        changeChannel(name)
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
     * Change from the current channel and reflect the changes in the app.
     *
     * @param name The title of the new channel to switch to
     */
    private fun changeChannel(name: String) {
        val newChannel = mChannels[name]
        if(newChannel == null) {
            Toast.makeText(this, "Error retrieving channel", Toast.LENGTH_SHORT).show()
            return
        }
        mCurrentChannel = newChannel
        mMedia = newChannel.media
        br.mMedia = mMedia

        supportActionBar?.title = newChannel.title

        // Just reset the RecyclerView instead of mucking about with its internals
        initRecyclerView()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Have to reset the broadcaster receiver adapter otherwise notifying
        // the mAdapter of data changes won't work
        br.mRecyclerView = recyclerView
    }

    /**
     * Called from the DeleteDialog, prompting the main activity to delete an item from the
     * mMedia ArrayList and update the recycler view to reflect the changes.
     *
     * @param position The position of the item in the mMedia ArrayList
     */
    override fun removeItem(position: Int) {
        mMedia.removeAt(position)
        notifyAdapter()
    }

    fun addMedia(newMedia: Media) {
        mMedia.add(newMedia)
        notifyAdapter()
    }
    fun notifyAdapter() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter?.notifyDataSetChanged()
    }
}


/**
 * The class that receives and handles the intent sent from the OmdbIntentService to the MainActivity.
 */
class MyBroadcastReceiver(
        val mContext: Context,
        val mHandler: Handler,
        var mMedia: ArrayList<Media>,
        var mRecyclerView: RecyclerView
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
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

        /*
         * Deprecated
         *
         *
        // else
        val newMedia = Media(
                title = intent.getStringExtra("title"),
                imageLink = intent.getStringExtra("imageUrl"),
                summary = intent.getStringExtra("plot"),
                year = intent.getStringExtra("year")
        )
        mMedia.add(newMedia)
        mRecyclerView.adapter?.notifyDataSetChanged()
        */

        val searchResponse = intent.getSerializableExtra("searchResponse") as OmdbSearchResponse
        val searchMediaList: ArrayList<Media> = parseSearchResponse(searchResponse)
        openSearchDialogWindow(context, searchMediaList)
    }

    private fun parseSearchResponse(searchResponse: OmdbSearchResponse): ArrayList<Media> {
        val newList: ArrayList<Media> = ArrayList<Media>()
        for(item in searchResponse.Search) {
            Log.d("LEETAG", item.Title)
            val newMedia = Media(item.Title, item.Poster, summary = item.Year, year = item.Year)
            newList.add(newMedia)
        }
        return newList
    }

    private fun openSearchDialogWindow(context: Context?, searchMediaList: ArrayList<Media>) {

        val searchDialog = SearchDialog()
        val arg: Bundle = Bundle()
        arg.putSerializable("searchResponse", searchMediaList)
        searchDialog.arguments = arg

        searchDialog.show((context as MainActivity).supportFragmentManager, "search Dialog")
    }
}


