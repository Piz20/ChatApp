package com.example.chatapp

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.adapter.SearchUserRecyclerAdapter
import com.example.chatapp.models.User
import com.algolia.search.model.search.Query
import com.algolia.search.client.ClientSearch
import com.algolia.search.exception.AlgoliaClientException
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.example.chatapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class SearchUserActivity : BaseActivity() {
    lateinit var searchEditText: EditText
    lateinit var searchButton: ImageButton
    lateinit var searchuserRecyclerView: RecyclerView
    lateinit var searchuserAdapater: SearchUserRecyclerAdapter
    lateinit var backButton: ImageButton
    lateinit var nohomietextView: TextView
    lateinit var algoliaAppIdKey: String
    lateinit var algoliaApiKey: String
    private lateinit var progressbar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)
        searchEditText = findViewById(R.id.search_user_edittext)
        searchButton = findViewById(R.id.search_user_button)
        searchuserRecyclerView = findViewById(R.id.search_user_recycler_view)
        nohomietextView = findViewById(R.id.no_homies_textView)
        backButton = findViewById(R.id.back_button)
        progressbar = findViewById(R.id.find_homie_progressbar)
        setupUi()
        val applicationInfo: ApplicationInfo = this.packageManager.getApplicationInfo(
            application.packageName,
            PackageManager.GET_META_DATA
        )
        algoliaAppIdKey = applicationInfo.metaData["ALGOLIA_APP_ID_KEY"].toString()
        algoliaApiKey = applicationInfo.metaData["ALGOLIA_ADMIN_API_KEY"].toString()
    }


    //Basic UI implementations
    fun setupUi() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        searchEditText.gravity = Gravity.CENTER
        searchEditText.setSelection(searchEditText.text.length / 2)
        searchEditText.requestFocus()
        lifecycleScope.launch { AlgoliaSearch() }
    }

    suspend fun AlgoliaSearch() {


        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lifecycleScope.launch {
                    setupSearchRecyclerView(s.toString())
                }

            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun afterTextChanged(s: Editable?) {
                lifecycleScope.launch {
                    setupSearchRecyclerView(s.toString())
                }
            }
        })
        searchButton.setOnClickListener {
            val searchTerm = searchEditText.text.toString()
            if (searchTerm.isEmpty()) {
                searchEditText.setError("Invalid Username")
            }
            lifecycleScope.launch {
                setupSearchRecyclerView(searchTerm)
            }
        }
    }

    //To handle the search of users
    suspend fun setupSearchRecyclerView(searchTerm: String) {
        try {
            val client = ClientSearch(
                applicationID = ApplicationID(algoliaAppIdKey), apiKey = APIKey(algoliaApiKey)
            )
            val index = client.initIndex(indexName = IndexName("Users"))
            var query = Query(searchTerm)
            if (searchTerm.isEmpty()) {
                query = Query()
            }
            val content = index.search(query)


            val users = content.hits.map { hit ->
                val uid = hit["objectID"].toString()
                val username = hit["username"].toString()
                val phoneNumber = hit["phonenumber"].toString()
                User(uid.trim('"'), username.trim('"'), phoneNumber.trim('"'))

            }

            searchuserAdapater = SearchUserRecyclerAdapter(users, this)
            searchuserRecyclerView.layoutManager = LinearLayoutManager(this)
            searchuserRecyclerView.adapter = searchuserAdapater
            searchuserAdapater.setUsers(users)
            searchuserAdapater.notifyDataSetChanged()
            if (searchuserAdapater.itemCount > 0) {
                nohomietextView.visibility = View.GONE
            } else if (searchuserAdapater.itemCount == 0 && users.isEmpty()) {
                nohomietextView.visibility = View.VISIBLE

            }


        } catch (_: AlgoliaClientException) {


        }
    }


}