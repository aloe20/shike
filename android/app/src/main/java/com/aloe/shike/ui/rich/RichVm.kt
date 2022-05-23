package com.aloe.shike.ui.rich

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.LiveData
import androidx.paging.*
import androidx.work.WorkInfo
import com.aloe.shike.BaseVm
import com.aloe.shike.Repository
import com.aloe.shike.ktx.log
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class RichVm @Inject constructor(
    private val app: Application,
    private val repository: Repository
) : BaseVm() {
    fun loadPdf(url: String): LiveData<WorkInfo> = repository.download(url)

    fun getPdfPage(filePath:String): Flow<PagingData<Bitmap>> {
        return Pager(PagingConfig(3, 1, true, 3)) {
            PdfPagingSource(PdfRenderer(ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)))
        }.flow
    }

    companion object {
        class PdfPagingSource(private val pdf: PdfRenderer) : PagingSource<Int, Bitmap>() {
            override fun getRefreshKey(state: PagingState<Int, Bitmap>): Int = 0

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bitmap> {
                return try {
                    val list = mutableListOf<Bitmap>()
                    val start = (params.key ?: -1) + 1
                    val end = start + params.loadSize
                    "---> $start, $end".log()
                    for (index in start until end) {
                        val page = pdf.openPage(index)
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        list.add(bitmap)
                    }
                    LoadResult.Page(list, null, end)
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }
        }
    }
}
