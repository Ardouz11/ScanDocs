package ocr.mobileVision.scanDocs

import android.util.SparseArray
import collections.forEach
import com.google.android.gms.vision.text.TextBlock
import java.util.regex.Pattern

class Helpers {
    fun removeUnuseful(items: SparseArray<TextBlock>?, string: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        items!!.forEach { i, _ ->
            if (!Pattern.matches(string, items.valueAt(i).value)) {
                arrayList.add(items.valueAt(i).value)
            }
        }
        return arrayList
    }
}
