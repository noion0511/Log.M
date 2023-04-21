package com.likewhile.meme.ui.view

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.likewhile.meme.BuildConfig
import com.likewhile.meme.R
import com.likewhile.meme.data.model.ImageMemoItem
import com.likewhile.meme.data.model.TextMemoItem
import com.likewhile.meme.databinding.ActivityImageMemoEditBinding
import com.likewhile.meme.ui.view.widget.MemoWidgetProvider
import com.likewhile.meme.ui.viewmodel.ImageMemoViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ImageMemoEditActivity : AppCompatActivity() {
    private lateinit var filePath : String
    private var fileUri : String =""
    private var imeageSettingMode : String = "uri"
    private lateinit var bitmap : Bitmap

    private val binding : ActivityImageMemoEditBinding by lazy { ActivityImageMemoEditBinding.inflate(layoutInflater) }
    private lateinit var imageMemoViewModel: ImageMemoViewModel
    private val itemId by lazy { intent.getLongExtra(MemoWidgetProvider.EXTRA_MEMO_ID, -1) }
    private var isMenuVisible =true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        imageMemoViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(ImageMemoViewModel::class.java)

        initMemoData()
        initCancel()
        initToolbar()
        initSave()
    }

    private fun setReadMode(){
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
        binding.imageContent.root.isClickable=false
        binding.imageAddButton.root.isClickable=false
        isMenuVisible = true
        invalidateOptionsMenu()
    }
    private fun setEditMode(){
        binding.title.editTextTitle.isEnabled = true
        binding.content.editTextContent.isEnabled = true
        binding.bottomBtnEdit.checkBoxFix.isEnabled = true
        binding.bottomBtnEdit.buttonSave.visibility = View.VISIBLE
        binding.bottomBtnEdit.buttonCancel.visibility = View.VISIBLE
        isMenuVisible = false
        invalidateOptionsMenu()
        initImageBtn()
    }
    private fun initMemoData() {
        if(itemId != -1L){
            imageMemoViewModel.setItemId(itemId)
            setReadMode()
        }else{
             setEditMode()
        }
        imageMemoViewModel.memo.observe(this){ memo ->
            if(memo !=null){
                binding.title.editTextTitle.setText(memo.title)
                binding.content.editTextContent.setText(memo.content)
                binding.bottomBtnEdit.checkBoxFix.isChecked = memo.isFixed
            }
        }
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

    private fun initCancel() {
        binding.bottomBtnEdit.buttonCancel.setOnClickListener { finish() }
    }

    private fun initSave() {
        binding.bottomBtnEdit.buttonSave.setOnClickListener {
            val title = binding.title.editTextTitle.text.toString()
            val content = binding.content.editTextContent.text.toString()
            val isFixed = binding.bottomBtnEdit.checkBoxFix.isChecked

            if(imeageSettingMode=="bitmap"){
                makePathName()
                if(saveImageInLocalStorage()==false){
                    Toast.makeText(this, "이미지 저장에 실패했습니다", Toast.LENGTH_SHORT).show()
                    binding.imageAddButton.root.visibility = View.VISIBLE
                    binding.imageContent.root.visibility = View.GONE
                    fileUri=""
                }
            }

            if (title.isBlank() || content.isBlank())
                Toast.makeText(this, "제목과 상세내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            else if(fileUri==""){
                Toast.makeText(this, "사진을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }else{
                saveMemo(title, content, isFixed)
            }
        }
    }
    private fun saveMemo(title : String, content : String, isFixed :Boolean){

        val memoItem = ImageMemoItem(
            id = itemId,
            title = title,
            content = content,
            uri = fileUri.toString(),
            date = Date(),
            isFixed = isFixed
        )

        if (itemId != -1L) {
            imageMemoViewModel.updateMemo(memoItem)
            setReadMode()
            //updateWidget()
        } else {
            imageMemoViewModel.insertMemo(memoItem)
            setReadMode()
            val focusedView = currentFocus
            focusedView?.clearFocus()

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(focusedView?.windowToken, 0)
        }
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
            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun initImageBtn(){
        binding.imageAddButton.imageAddButton.setOnClickListener {
            showDialog()
        }
        binding.imageContent.imageView.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog(){
        val navigationOptions = arrayOf(
            "사진 촬영",
            "갤러리에서 사진 선택"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_a_screen))
            .setItems(navigationOptions) { _, which ->
                val intent = when (navigationOptions[which]) {
                    "사진 촬영" -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        requestCameraImageLauncher.launch(intent)
                    }
                    "갤러리에서 사진 선택" ->{
                        requestGalleryImageLauncher.launch("image/*")
                    }
                    else -> null
                }
            }
        builder.create().show()
    }

    private fun setImageView() {//이미지 뷰에 사진을 세팅합니다
        if(imeageSettingMode=="uri"){
            binding.imageContent.imageView.setImageURI(Uri.parse(fileUri))
        }else{
            binding.imageContent.imageView.setImageBitmap(bitmap)
        }
        binding.imageAddButton.root.visibility = View.GONE
        binding.imageContent.root.visibility = View.VISIBLE
    }

    private fun saveImageInLocalStorage() : Boolean{//촬영한 이미지를 external storage에 저장합니다
        val imageFile= File(filePath)
        try {
            imageFile.createNewFile()
            val out =FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
            fileUri=FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider", imageFile).toString()
            return true
        } catch (e:java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(getApplicationContext(), "이미지 저장 실패", Toast.LENGTH_SHORT).show();
            return false
        }
    }

    private fun makePathName() {//촬영한 이미지를 저장할 경로를 만듭니다
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

        }
    }

    private val requestCameraImageLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode== RESULT_OK) {
            bitmap= it.data?.extras?.get("data") as Bitmap
            imeageSettingMode="bitmap"
            setImageView()
        }
    }

    private val requestGalleryImageLauncher=registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        if(it!=null){
            fileUri=it.toString()
            imeageSettingMode="uri"
            setImageView()
        }
    }
//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        val menuItem = menu.findItem(R.id.button_edit_mode)
//        menuItem.isVisible = isMenuVisible
//        return super.onPrepareOptionsMenu(menu)
//    }

    override fun onDestroy() {
        super.onDestroy()
        imageMemoViewModel.closeDB()
    }

}