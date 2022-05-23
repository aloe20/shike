package com.aloe.shike.ui.excel

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.aloe.excel.ExcelAdapter
import com.aloe.bean.QuoteBean
import com.aloe.shike.R

class MyAdapter(size: Rect) : ExcelAdapter<QuoteTop, QuoteBean, MyAdapter.Companion.Holder>(size) {
    /**
     * 表格的列.
     */
    private lateinit var sortTop: QuoteTop

    /**
     * 是否倒序排序(从大到小).
     */
    private var isSortReverse = true

    init {
        setExcelClickListener { v, row, column ->
            if (row == RecyclerView.NO_POSITION && column != RecyclerView.NO_POSITION) {
                if (sortTop == topData[column]) {
                    updateSort(sortTop, !isSortReverse)
                } else {
                    updateSort(topData[column], true)
                }
            } else {
                Toast.makeText(v.context, "$row, $column", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun createHolder(itemView: View): Holder = Holder(itemView)

    override fun convertItem(viewHolder: Holder, row: Int, column: Int) {
        if (row != RecyclerView.NO_POSITION && column != RecyclerView.NO_POSITION) {
            viewHolder.itemView.findViewById<TextView?>(R.id.text)?.text = getCell(topData[column], data[row])
        } else if (row != RecyclerView.NO_POSITION) {
            val item = data[row]
            viewHolder.itemView.findViewById<TextView?>(R.id.tvName)?.text = item.name
            viewHolder.itemView.findViewById<TextView?>(R.id.tvMarket)?.text = item.market
            viewHolder.itemView.findViewById<TextView?>(R.id.tvCode)?.text = item.code
        } else if (column != RecyclerView.NO_POSITION) {
            viewHolder.itemView.findViewById<TextView?>(R.id.tvName)?.text = topData[column].showName
            val ivIcon = viewHolder.itemView.findViewById<ImageView?>(R.id.ivIcon)
            if (this::sortTop.isInitialized && sortTop == topData[column]) {
                ivIcon?.setImageResource(if (isSortReverse) R.drawable.ic_sort_reverse else R.drawable.ic_sort_normal)
            } else {
                ivIcon?.setImageResource(R.drawable.ic_sort_default)
            }
        } else {
            viewHolder.itemView.findViewById<TextView>(R.id.tvTitle).text = "名称"
        }
    }

    private fun getCell(topItem: QuoteTop, startItem: QuoteBean): String {
        return when (topItem) {
            QuoteTop.LAST_PRICE -> if (startItem.lastPrice.isNaN()) "--"
            else String.format("%.2f", startItem.lastPrice)
            QuoteTop.UP_DOWN -> if (startItem.upDown.isNaN()) "--" else String.format("%.2f", startItem.upDown)
            QuoteTop.TURNOVER_RATE -> if (startItem.turnoverRate.isNaN()) "--"
            else String.format("%.2f", startItem.turnoverRate)
            QuoteTop.VOLUME -> if (startItem.volume == Int.MIN_VALUE) "--"
            else String.format("%.2f万", startItem.volume / 1_0000.0)
            QuoteTop.AMOUNT -> if (startItem.amount.isNaN()) "--"
            else String.format("%.2f亿", startItem.amount / 1_0000_0000)
            QuoteTop.RATIO -> if (startItem.ratio.isNaN()) "--" else String.format("%.2f", startItem.ratio)
            QuoteTop.SA -> if (startItem.sa.isNaN()) "--" else String.format("%.2f", startItem.sa)
            QuoteTop.UP_DOWN_SPEED -> if (startItem.upDownSpeed.isNaN()) "--"
            else String.format("%.2f", startItem.upDownSpeed)
            QuoteTop.PE_RATIO -> if (startItem.peRatio.isNaN()) "--" else String.format("%.2f", startItem.peRatio)
            QuoteTop.TOT_VAL -> if (startItem.totVal.isNaN()) "--"
            else String.format("%.2f亿", startItem.totVal / 1_0000_0000)
            QuoteTop.CIR_VAL -> if (startItem.cirVal.isNaN()) "--"
            else String.format("%.2f亿", startItem.cirVal / 1_0000_0000)
            else -> ""
        }
    }

    /**
     * 排序.
     * @param sortTop 需要排序的列
     * @param isSortReverse 是否倒序(从大到小)
     */
    fun updateSort(sortTop: QuoteTop, isSortReverse: Boolean) {
        this.sortTop = sortTop
        this.isSortReverse = isSortReverse
        data.sortWith { o1, o2 ->
            val pair: Pair<Double, Double> = when (sortTop) {
                QuoteTop.LAST_PRICE -> Pair(o1.lastPrice, o2.lastPrice)
                QuoteTop.UP_DOWN -> Pair(o1.upDown, o2.upDown)
                QuoteTop.TURNOVER_RATE -> Pair(o1.turnoverRate, o2.turnoverRate)
                QuoteTop.VOLUME -> Pair(o1.volume.toDouble(), o2.volume.toDouble())
                QuoteTop.AMOUNT -> Pair(o1.amount, o2.amount)
                QuoteTop.RATIO -> Pair(o1.ratio, o2.ratio)
                QuoteTop.SA -> Pair(o1.sa, o2.sa)
                QuoteTop.UP_DOWN_SPEED -> Pair(o1.upDownSpeed, o2.upDownSpeed)
                QuoteTop.PE_RATIO -> Pair(o1.peRatio, o2.peRatio)
                QuoteTop.TOT_VAL -> Pair(o1.totVal, o2.totVal)
                QuoteTop.CIR_VAL -> Pair(o1.cirVal, o2.cirVal)
                else -> Pair(0.0, 0.0)
            }
            if (isSortReverse) if (pair.first.isNaN()) 1 else pair.second.compareTo(pair.first)
            else pair.first.compareTo(pair.second)
        }
        notifyItemRangeChanged(0, itemCount)
        updateSticky()
    }

    override fun createView(parent: ViewGroup, viewType: ViewType): View {
        val layout = when (viewType) {
            ViewType.START_TOP -> R.layout.item_title
            ViewType.START_BOTTOM -> R.layout.item_start
            ViewType.END_TOP -> R.layout.item_top
            ViewType.END_BOTTOM -> R.layout.item_cell
        }
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }

    companion object {
        class Holder(item: View) : RecyclerView.ViewHolder(item)
    }
}
