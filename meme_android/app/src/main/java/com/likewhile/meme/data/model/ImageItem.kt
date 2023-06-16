package com.likewhile.meme.data.model

import android.graphics.Bitmap

data class ImageItem (
    var id: Long = -1L,
    var type : ImageType,//바로 촬영된 이미지인지, 갤러리에서 가져온 이미지인지
    var uri : String,
    var bitmap : Bitmap?
    )

enum class ImageType (val typeValue : Int){
    URI(0),
    BITMAP(1);
}