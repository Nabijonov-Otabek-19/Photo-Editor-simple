package uz.gita.myphotoeditor_bek.data

sealed interface AddViewData {

    data class EmojiData(
        val imageResID: Int,
        val defHeight: Int,
        val defWidth: Int
    ) : AddViewData

    data class TextData(
        val st: String,
        val defTextSize: Int
    ) : AddViewData
}