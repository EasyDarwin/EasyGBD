import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easygbs.easygbd.R
import java.io.File

class ViewPagerAdapter(
    private val context: Context,
    private val videoPages: List<List<File>>,
    private val root: View,
    private val onVideoClick: (File) -> Unit,
    private val onDeleteCompleted: () -> Unit
) : RecyclerView.Adapter<ViewPagerAdapter.PageViewHolder>() {

    var mVideoGridAdapter: VideoGridAdapter? = null

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.rvVideoGrid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_page_video_list, parent, false)
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        mVideoGridAdapter =
            VideoGridAdapter(context, videoPages[position], root, onVideoClick, onDeleteCompleted)
        holder.recyclerView.layoutManager = GridLayoutManager(context, 3) // 网格布局，每行3列
        holder.recyclerView.adapter = mVideoGridAdapter
    }

    override fun getItemCount(): Int = videoPages.size
}
