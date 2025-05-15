import android.content.Context
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.easygbs.easygbd.R
import java.io.File

class VideoGridAdapter(
    private val context: Context,
    private val videoList: List<File>,
    private val root: View,
    private val onItemClick: (File) -> Unit,
    private val onDeleteCompleted: () -> Unit // 删除完成的回调
) : RecyclerView.Adapter<VideoGridAdapter.VideoViewHolder>() {

    private val selectedItems = SparseBooleanArray() // 记录选中状态
    var isSelectionMode = false // 是否进入多选模式

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
        val ivSelect: ImageView = itemView.findViewById(R.id.iv_select)
        val overlay: View = itemView.findViewById(R.id.ll_overlay) // 选中状态的遮罩层

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_video_thumbnail, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoFile = videoList[position]

        // 使用 Glide 加载视频缩略图
        Glide.with(context)
            .load(videoFile)
            .placeholder(R.mipmap.ic_video_placeholder)
            .centerCrop()
            .into(holder.thumbnail)

        // 更新选中状态
        if (selectedItems[position]) {
            holder.ivSelect.setImageResource(R.mipmap.recording_select)
        } else {
            holder.ivSelect.setImageResource(R.mipmap.recording_unselect)
        }

        if (isSelectionMode) {
            holder.overlay.visibility = View.VISIBLE
        } else {
            holder.overlay.visibility = View.GONE
        }

        holder.thumbnail.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(position)
            } else {
                onItemClick(videoFile) // 普通点击事件
            }
        }

        holder.thumbnail.setOnLongClickListener {
            if (!isSelectionMode) {
                changeSelectionMode()
                toggleSelection(position) // 切换选中状态
            }
            true
        }
    }

    override fun getItemCount(): Int = videoList.size

    /**
     * 切换选中状态
     */
    private fun toggleSelection(position: Int) {
        if (selectedItems[position]) {
            selectedItems.delete(position) // 取消选中
        } else {
            selectedItems.put(position, true) // 设置选中
        }
        notifyDataSetChanged() // 更新单项 UI
    }

    /**
     * 去选选中状态
     */
    fun allSelection() {
        selectedItems.clear() // 先清空已有选中状态
        for (i in 0 until itemCount) {
            selectedItems.put(i, true) // 将所有项设置为选中
        }
        notifyDataSetChanged() // 更新整个 RecyclerView 的 UI

    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun changeSelectionMode() {
        isSelectionMode = !isSelectionMode;
        val leftBtn = root.findViewById<ImageView>(R.id.iv_navbar_left)
        val footer = root.findViewById<View>(R.id.ll_footer)
        val footerAllSelect = footer.findViewById<View>(R.id.ll_select_all)
        val footerAllSelectBtn = footer.findViewById<ImageView>(R.id.iv_all_select_btn)
        val footerDeleteBtn = footer.findViewById<View>(R.id.ll_delete_btn)
        if (isSelectionMode) {
            leftBtn.setImageResource(R.mipmap.recording_close)
            footer.visibility = View.VISIBLE

            if (selectedItems.size() == itemCount) {
                footerAllSelectBtn.setImageResource(R.mipmap.ic_select_all)
            }else{
                footerAllSelectBtn.setImageResource(R.mipmap.ic_selete_defalut)
            }

            footerAllSelect.setOnClickListener {
                if (selectedItems.size() != itemCount) {
                    allSelection()
                    footerAllSelectBtn.setImageResource(R.mipmap.ic_select_all)
                } else {
                    footerAllSelectBtn.setImageResource(R.mipmap.ic_selete_defalut)
                    clearSelection()
                }
            }

            footerDeleteBtn.setOnClickListener {
                // 获取选中的视频列表
                val selectedVideos = getSelectedItems()

                if (selectedVideos.isNotEmpty()) {
                    // 遍历删除选中的文件
                    for (video in selectedVideos) {
                        if (video.exists()) {
                            val isDeleted = video.delete() // 删除文件
                            if (!isDeleted) {
                                Toast.makeText(context, "文件 ${video.name} 删除失败", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    // 更新数据源，移除选中的视频
                    val updatedList = videoList.toMutableList().apply {
                        removeAll(selectedVideos)
                    }
                    // 通知适配器更新数据
                    (videoList as MutableList).clear()
                    videoList.addAll(updatedList)
                    clearSelection()
                    // 刷新 UI
                    notifyDataSetChanged()
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                    changeSelectionMode()
                    onDeleteCompleted()

                } else {
                    Toast.makeText(context, "没有选中的视频", Toast.LENGTH_SHORT).show()
                }

                isSelectionMode = false // 是否进入多选模式
            }

        } else {
            leftBtn.setImageResource(R.mipmap.ic_back)
            footer.visibility = View.GONE
        }
    }

    /**
     * 获取选中项列表
     */
    fun getSelectedItems(): List<File> {
        val selectedFiles = mutableListOf<File>()
        for (i in 0 until selectedItems.size()) {
            val key = selectedItems.keyAt(i) // 获取选中项的索引
            if (selectedItems[key]) { // 判断该索引是否被选中
                selectedFiles.add(videoList[key]) // 添加到结果列表中
            }
        }
        return selectedFiles
    }
}

