package com.ajaha.notes

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.settings_toolbar)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat, pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader, pref.fragment!!
        ).apply {
            arguments = args
            @Suppress("DEPRECATION")
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction().replace(R.id.settings, fragment)
            .addToBackStack(null).commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
            findPreference<ListPreference>("app_theme")?.setOnPreferenceChangeListener { _, newValue ->
                when (newValue) {
                    "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }

                true
            }
        }

    }


    /** Testing
    private class TestAPI(private val context: Context) {
    fun getUserFiles(apiKey: String, callback: Callback<ResponseBody>) {
    if (!Regex("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}").matches(
    apiKey
    )
    ) return
    val encodedApiKey =
    Base64.encodeToString(":$apiKey".toByteArray(), Base64.DEFAULT).trim()
    val file = context.getDatabasePath("database.db")
    Retrofit.Builder().baseUrl("https://pixeldrain.com/api/")
    .addConverterFactory(GsonConverterFactory.create()).build()
    .create(APIService::class.java).upload(
    "Basic $encodedApiKey", MultipartBody.Part.createFormData(
    "file",
    file.name,
    RequestBody.create(MediaType.get("application/vnd.sqlite3"), file)
    )
    ).enqueue(callback)
    }

    data class HTTPError(
    val success: String,
    val value: String,
    val message: String,
    val extra: HashMap<String, String>? = null
    )

    data class UserFile(
    val id: String,
    var name: String,
    var size: Long,
    var view: Int,
    var bandwidthUsed: Int,
    var bandwidthUsedPaid: Int,
    var download: Int,
    var dateUpload: String,
    var dateLastView: String,
    var mimeType: String,
    var thumbnailHref: String,
    var hashSHA256: String,
    var deleteAfterDate: String,
    var deleteAfterDownloads: Int,
    var availability: String,
    var availabilityMessage: String,
    var abuseType: String,
    var abuseReporterName: String,
    var canEdit: Boolean,
    var showAds: Boolean,
    var allowVideoPlayer: Boolean,
    var downloadSpeedLimit: Int
    ) {

    }

    interface APIService {

    @GET("user/files/")
    fun getUserFiles(@Header("Authorization") apiKey: String): Call<HashMap<String, List<UserFile>>>

    @Multipart
    @POST("file")
    fun upload(
    @Header("Authorization") apiKey: String, @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    @PUT("file/{name}")
    fun upload2(@Path("name") fileName: String): Call<ResponseBody>

    @GET("file/{id}")
    fun getFile(@Path("id") fileId: String): Call<ResponseBody>

    @GET("file/{id}/info")
    fun getFileInfo(@Path("id") fileId: String): Call<UserFile>

    @GET("file/{id}/thumbnail?width={w}&height={h}")
    fun getFileThumbnail(
    @Path("id") fileId: String, w: Int? = null, h: Int? = null
    ): Call<File>

    @DELETE("file/{id}")
    fun deleteFile(@Path("id") fileId: String): Call<ResponseBody>
    }
    }*/
}