package com.likewhile.meme.ui.view

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.likewhile.meme.BuildConfig
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.R
import com.likewhile.meme.data.model.TextMemoItem
import com.likewhile.meme.databinding.ActivityMemoEditBinding
import com.likewhile.meme.ui.viewmodel.TextMemoViewModel
import com.likewhile.meme.util.DateFormatUtil
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MemoEditActivity : AppCompatActivity() {
    private val READ_EXTERNAL_STORAGE_CODE=99
    private val binding by lazy { ActivityMemoEditBinding.inflate(layoutInflater) }
    private lateinit var memoViewModel: TextMemoViewModel
    private val itemId by lazy { intent.getLongExtra(MemoWidgetProvider.EXTRA_MEMO_ID, -1) }
    private var insertedId : Long = 0L
    private var isMenuVisible = true
    private var fileUri : String =""
    private var imeageSettingMode : String = "uri"
    private lateinit var bitmap : Bitmap
    private var isImageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        memoViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
            TextMemoViewModel::class.java)

        initMemoData()
        initSave()
        initCancel()
        initToolbar()
        initImageBtn()
    }


    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, MemoWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        // 위젯 업데이트를 요청합니다.
        val intent = Intent(this, MemoWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        sendBroadcast(intent)
    }

    private fun setReadMode() {
        binding.title.editTextTitle.isEnabled = false
        binding.title.editTextTitle.alpha = 1f
        binding.title.editTextTitle.setTextColor(Color.BLACK)
        binding.content.editTextContent.isEnabled = false
        binding.content.editTextContent.alpha = 1f
        binding.content.editTextContent.setTextColor(Color.BLACK)
        binding.bottomBtnEdit.checkBoxFix.isEnabled = false
        binding.bottomBtnEdit.checkBoxFix.setTextColor(Color.BLACK)
        binding.bottomBtnEdit.buttonSave.visibility = View.GONE
        binding.bottomBtnEdit.buttonCancel.visibility = View.GONE
        binding.bottomBtnAddImage.visibility=View.GONE
        unregisterForContextMenu(binding.image.root)
        isMenuVisible = true
        imeageSettingMode = "uri"
        isImageChanged = false
        invalidateOptionsMenu()
    }


    private fun setEditMode() {
        binding.title.editTextTitle.isEnabled = true
        binding.content.editTextContent.isEnabled = true
        binding.bottomBtnEdit.checkBoxFix.isEnabled = true
        binding.bottomBtnEdit.buttonSave.visibility = View.VISIBLE
        binding.bottomBtnEdit.buttonCancel.visibility = View.VISIBLE
        binding.bottomBtnAddImage.visibility=View.VISIBLE
        registerForContextMenu(binding.image.root)
        isMenuVisible = false
        invalidateOptionsMenu()
    }

    private fun initToolbar() {
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT,
            Gravity.START
        )
        setSupportActionBar(binding.toolbar.toolbar)
        val blackColor = ContextCompat.getColor(this, android.R.color.black)
        binding.toolbar.logo.setColorFilter(blackColor, PorterDuff.Mode.SRC_IN)
        binding.toolbar.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        binding.toolbar.toolbar.layoutParams = params
        binding.toolbar.toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initImageBtn(){
        binding.buttonAddImage.setOnClickListener {
            if(checkPermission()){
                showDialog()
            }else{
                requestPermission()
            }
        }
    }

    private fun initSave() {
        binding.bottomBtnEdit.buttonSave.setOnClickListener {
            val title = binding.title.editTextTitle.text.toString()
            val content = binding.content.editTextContent.text.toString()
            val isFixed = binding.bottomBtnEdit.checkBoxFix.isChecked
            Log.d("setting mode","$imeageSettingMode")
            if(imeageSettingMode=="bitmap" && isImageChanged == true){
                if(saveImageInLocalStorage()==false){
                    Toast.makeText(this, getString(R.string.image_save_failed), Toast.LENGTH_SHORT).show()
                    binding.image.root.visibility=View.GONE
                    fileUri=""
                }
            }

            val memoItem = TextMemoItem(
                id = itemId,
                title = title,
                content = content,
                uri = fileUri,
                date = Date(),
                isFixed = isFixed,
            )

            if (title.isBlank() || content.isBlank())
                Toast.makeText(this, getString(R.string.incomplete_input_message), Toast.LENGTH_SHORT).show()
            else {
                if (itemId != -1L) {
                    memoViewModel.updateMemo(memoItem)
                    setReadMode()
                    updateWidget()
                } else if (insertedId != 0L) {
                    memoViewModel.setItemId(insertedId)
                    memoItem.id = insertedId
                    memoViewModel.updateMemo(memoItem)
                    setReadMode()
                    updateWidget()
                } else {
                    insertedId = memoViewModel.insertMemo(memoItem)
                    setReadMode()
                    val focusedView = currentFocus
                    focusedView?.clearFocus()

                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(focusedView?.windowToken, 0)
                }
            }
        }
    }


    private fun initCancel() {
        binding.bottomBtnEdit.buttonCancel.setOnClickListener { finish() }
    }

    private fun initMemoData() {
        if (itemId != -1L) {
            memoViewModel.setItemId(itemId)
            setReadMode()
        } else {
            setEditMode()
        }

        memoViewModel.memo.observe(this) { memo ->
            if (memo != null) {
                binding.title.editTextTitle.setText(memo.title)
                binding.content.editTextContent.setText(memo.content)
                binding.bottomBtnEdit.checkBoxFix.isChecked = memo.isFixed
                if(memo.uri!=""){
                    fileUri=memo.uri
                    setImageView()
                }
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_image_long_click, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
             R.id.button_delete_image->{
                 binding.image.root.visibility=View.GONE
                 fileUri=""
                 imeageSettingMode="uri"
                 isImageChanged = true
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.button_edit_mode -> {
                setEditMode()
                return true
            }
            R.id.button_memo_delete -> {
                val deleteResult = memoViewModel.deleteMemo(itemId)
                if (deleteResult) {
                    finish()
                } else {
                    Toast.makeText(this, "메모 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.button_edit_mode)
        menuItem.isVisible = isMenuVisible
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setImageView() {//이미지 뷰에 사진을 세팅합니다
        if(imeageSettingMode=="uri"){
            Log.d("uri","$fileUri")
            Glide
                .with(this)
                .load(fileUri)
                .error(R.drawable.baseline_hide_image_24)
                .fitCenter()
                .into(binding.image.imageView)

        }else{
            binding.image.imageView.setImageBitmap(bitmap)
        }
        binding.image.root.visibility=View.VISIBLE
    }

    private fun showDialog(){
        val navigationOptions = arrayOf(
            getString(R.string.taking_pictures),
            getString(R.string.select_a_picture_from_gallery)
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_a_screen))
            .setItems(navigationOptions) { _, which ->
                val intent = when (navigationOptions[which]) {
                    getString(R.string.taking_pictures) -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        requestCameraImageLauncher.launch(intent)
                    }
                    getString(R.string.select_a_picture_from_gallery) ->{
                        requestGalleryImageLauncher.launch("image/*")
                    }
                    else -> null
                }
            }
        builder.create().show()
    }

    private fun saveImageInLocalStorage() : Boolean{//촬영한 이미지를 external storage에 저장합니다
        Log.d("save image","save")
        var filePath : String =""
        val uuid = UUID.randomUUID()
        val externalFileDir : String? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        if(externalFileDir!=null){
            val cal = Calendar.getInstance()
            val dateString = cal.get(Calendar.YEAR).toString()+
                    cal.get(Calendar.MONTH+1).toString()+
                    cal.get(Calendar.DATE).toString()+
                    cal.get(Calendar.HOUR).toString()+
                    cal.get(Calendar.MINUTE).toString()+
                    cal.get(Calendar.SECOND).toString()+
                    uuid.toString()
            filePath=externalFileDir+"/$dateString"+".png"
            val imageFile= File(filePath)
            try {
                imageFile.createNewFile()
                val out = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.close()
                fileUri= FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider", imageFile).toString()
                return true
            } catch (e:java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(getApplicationContext(), getString(R.string.image_save_failed), Toast.LENGTH_SHORT).show();
                return false
            }
        }
        return false
    }

    private fun checkPermission() : Boolean{
        val readPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return readPermission == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            READ_EXTERNAL_STORAGE_CODE ->{
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    showDialog()
                }
                else{
                    Toast.makeText(this, getString(R.string.authorization_is_required), Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private val requestCameraImageLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode== RESULT_OK) {
            bitmap= it.data?.extras?.get("data") as Bitmap
            imeageSettingMode="bitmap"
            isImageChanged = true
            setImageView()
        }
    }

    private val requestGalleryImageLauncher=registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        if(it!=null){
            fileUri=it.toString()
            imeageSettingMode="uri"
            isImageChanged = true
            setImageView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        memoViewModel.closeDB()
    }
}