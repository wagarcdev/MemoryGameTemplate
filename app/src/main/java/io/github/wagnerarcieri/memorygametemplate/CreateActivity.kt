package io.github.wagnerarcieri.memorygametemplate

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import io.github.wagnerarcieri.memorygametemplate.model.BoardSize
import io.github.wagnerarcieri.memorygametemplate.utils.BitmapScaler
import io.github.wagnerarcieri.memorygametemplate.utils.EXTRA_BOARD_SIZE
import io.github.wagnerarcieri.memorygametemplate.utils.EXTRA_GAME_NAME
import java.io.ByteArrayOutputStream

/**@Deprecated*/
//import io.github.wagnerarcieri.memorygametemplate.utils.isPermissionGranted
//import io.github.wagnerarcieri.memorygametemplate.utils.requestPermission

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val READ_EXTERNAL_PHOTO_CODE = 124
        private const val TAG = "CreateActivity"
        private const val MIN_GAME_NAME_LENGTH = 3
        private const val MAX_GAME_NAME_LENGTH = 14

/**@Deprecated*/
//        private const val PICK_PHOTO_CODE = 115
//        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button
    private lateinit var pbUploading: ProgressBar

    private lateinit var adapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize
    private  var numImagesRequired = -1
    private val chosenImageUris = mutableListOf<Uri>()
    private val storage = Firebase.storage
    private val db = Firebase.firestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)
        pbUploading = findViewById(R.id.pbUploading)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"

        btnSave.setOnClickListener{
            saveDataOnFirebase()
        }
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))
        etGameName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

        })

        adapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object : ImagePickerAdapter.ImageClickListener{
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPlaceHolderClicked() {
/**@Deprecated
 * permission method deprecated
 * app was made with these methods below
 * still here just for educational purpose
 */
//                if (isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION)) {
//                    Log.i(TAG, "onPlaceHolderClicked: PermissionGranted: Click ")
//                    launchIntentForPhotos()
//                } else {
//                    requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTO_CODE)
//                    Log.i(TAG, "onPlaceHolderClicked: RequestPermission: Click ")
//
//                }
//                fun isPermissionGranted(context: Context, permission: String): Boolean {
//                    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
//                }
//
//                fun requestPermission(activity: Activity?, permission: String, requestCode: Int) {
//                }

                when {
                    ContextCompat.checkSelfPermission(
                        this@CreateActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        Log.i(TAG, "onPlaceHolderClicked: PermissionGranted: Click ")
                        launchIntentForPhotos()
                    }

                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        Log.i(TAG, "onPlaceHolderClicked: RequestPermission: Click ")

                    }
                }
            }
        })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        /**@Deprecated
         * Old method used to handle onRequestPermissionsResult
         */
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode  == READ_EXTERNAL_PHOTO_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                launchIntentForPhotos()
//            } else {
//                Toast.makeText(this, "To create a Customized Game, you need to provide acess to your photos", Toast.LENGTH_LONG).show()
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
        when (requestCode) {
            READ_EXTERNAL_PHOTO_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted.
                    launchIntentForPhotos()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(this, "To create a Customized Game, you need to provide acess to your photos", Toast.LENGTH_LONG).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    /**New Methods Implemented
     * @requestPermissionLauncher to request for permission
     * Register the permissions callback, which handles the user's response to the
     * system permissions dialog. Save the return value, an instance of
     * ActivityResultLauncher.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted.
                launchIntentForPhotos()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Toast.makeText(this, "To create a Customized Game, you need to provide acess to your photos", Toast.LENGTH_LONG).show()
            }
        }
/**@Deprecated
 * onActivityResult is deprecated
 * used ActivityResultContract to update
 **/
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode != PICK_PHOTO_CODE || resultCode == Activity.RESULT_OK || data == null) {
//            Log.w(TAG, "onActivityResult: Did not get data back from the launch activity, user likely canceled the flow", )
//            return
//        }
//        val  selectedUri = data.data
//        val clipData = data.clipData
//        if (clipData != null) {
//            Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
//            for (i in 0 until clipData.itemCount) {
//                val clipItem = clipData.getItemAt(i)
//                if (chosenImageUris.size < numImagesRequired) {
//                    chosenImageUris.add(clipItem.uri)
//                }
//            }
//        } else if (selectedUri != null) {
//            Log.i(TAG, "data $selectedUri")
//            chosenImageUris.add(selectedUri)
//        }
//        adapter.notifyDataSetChanged()
//        supportActionBar?.title = "Choose pictures (${chosenImageUris.size} / $numImagesRequired"
//    }

/**New Method Implemented
* @resultLauncher : launch the Intent for Photos
*/
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->

        if (result.resultCode == Activity.RESULT_OK ) {
            //TODO "val data" is never used
            val data: Intent? = result.data
            val selectedUri = data?.data
            val clipData = data?.clipData

            if (clipData != null) {
                Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData")
                for (i in 0 until clipData.itemCount) {
                    val clipItem = clipData.getItemAt(i)
                    if (chosenImageUris.size < numImagesRequired) {
                        chosenImageUris.add(clipItem.uri)
                    }
                }
            } else if (selectedUri != null) {
                Log.i(TAG, "data $selectedUri")
                chosenImageUris.add(selectedUri)
            }
            adapter.notifyDataSetChanged()
            supportActionBar?.title = "Choose pictures (${chosenImageUris.size} / $numImagesRequired)"
            btnSave.isEnabled = shouldEnableSaveButton()
        }

    }

    private fun saveDataOnFirebase() {
        btnSave.isEnabled = false
        Log.i(TAG, "saveDataOnFirebase")
        val customGameName = etGameName.text.toString()

        //Check if it is not overwriting another game that already exists with the name
        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
            if (document != null && document.data !=null) {
                AlertDialog.Builder(this)
                    .setTitle("Name already taken")
                    .setMessage("A game with the name '$customGameName' already exists... Please, chose another name")
                    .setPositiveButton("OK", null)
                    .show()
                btnSave.isEnabled = true

            } else {
                //Saves the game in Firebase
                handleImageUploading(customGameName)
            }
        }.addOnFailureListener{ exception ->
            Log.e(TAG, "Encountered error while saving memory game", exception )
            Toast.makeText(this, "Encountered error while saving memory game", Toast.LENGTH_SHORT).show()
            btnSave.isEnabled = true
        }
    }

    private fun handleImageUploading(gameName: String) {var didEnconterError = false
        pbUploading.visibility = View.VISIBLE //Make progress bar VISIBLE when Uploading Process start
        val uploadImageUrls = mutableListOf<String>()

        //A loop through the selected images, scaling down the images and then uploading them
        for ((index, photoUri) in chosenImageUris.withIndex()) {
            val imageByteArray = getImageByteArray(photoUri)
            val filePath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoReference = storage.reference.child(filePath)
            photoReference.putBytes(imageByteArray)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener { downloadUrlTask ->
                    if (!downloadUrlTask.isSuccessful) {
                        Log.e(TAG, "Exception with Firebase Storage", downloadUrlTask.exception )
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        didEnconterError = true
                        return@addOnCompleteListener
                    }
                    if (didEnconterError) {
                        pbUploading.visibility = View.GONE
                        return@addOnCompleteListener
                    }
                    //Upload images if there is no error
                    val downloadUrl = downloadUrlTask.result.toString()
                    uploadImageUrls.add(downloadUrl)
                    pbUploading.progress = uploadImageUrls.size * 100 / chosenImageUris.size
                    Log.i(TAG, "Finished uploading $photoUri, total uploaded files : ${uploadImageUrls.size}")
                    if (uploadImageUrls.size == chosenImageUris.size) {
                        handleAllImagesUploaded(gameName, uploadImageUrls)
                    }
                }
        }
    }

    private fun handleAllImagesUploaded(gameName: String, imageUrls: MutableList<String>) {
        db.collection("games").document(gameName)
            .set(mapOf("images" to imageUrls))
            .addOnCompleteListener { gameCreationTask ->
                pbUploading.visibility = View.GONE //Hide progress bar after uploading images
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception with game creation", gameCreationTask.exception)
                    Toast.makeText(this, "Failed to create game", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                Log.i(TAG, "Successfully created game $gameName")
                AlertDialog.Builder(this)
                    .setTitle("Upload complete! Let's play your game '$gameName'")
                    .setPositiveButton("OK") { _, _ ->
                        val resultData = Intent()
                        resultData.putExtra(EXTRA_GAME_NAME, gameName)
                        setResult(Activity.RESULT_OK, resultData)
                        finish()
                    }.show()

            }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.i(TAG, "Original width ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "Scaled width ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()


    }

    private fun shouldEnableSaveButton(): Boolean {
        //Check if should enable the SAVE button or not
        if (chosenImageUris.size != numImagesRequired) {
            return false
        }
        if (etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NAME_LENGTH) {
            return false
        }
        return true
    }


    //a button that returns to main activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //TODO Update the declaration of the Intent to modern methods ("val data" in resultLauncher was not used yet)
    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        resultLauncher.launch(intent)

        /**@Deprecated - app was originally made using this method below */
        //startActivityForResult(Intent.createChooser(intent, "Choose pictures"), PICK_PHOTO_CODE)

    }
}