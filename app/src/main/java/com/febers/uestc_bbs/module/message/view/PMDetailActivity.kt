package com.febers.uestc_bbs.module.message.view

import androidx.appcompat.widget.Toolbar
import android.util.Log.i
import com.febers.uestc_bbs.MyApp
import com.febers.uestc_bbs.R
import com.febers.uestc_bbs.base.*
import com.febers.uestc_bbs.entity.PMDetailBean
import com.febers.uestc_bbs.entity.PMDetailBean.BodyBean.PmListBean
import com.febers.uestc_bbs.entity.PMSendResultBean
import com.febers.uestc_bbs.module.message.presenter.MessageContract
import com.febers.uestc_bbs.module.message.presenter.PMDetailPresenterImpl
import com.febers.uestc_bbs.utils.PMTimeUtils
import com.febers.uestc_bbs.view.adapter.PMDetailAdapter
import kotlinx.android.synthetic.main.activity_private_detail.*

/**
 * 私信消息的详情
 * 通过mvp模式获取历史消息
 * 通过service实时更新
 */
class PMDetailActivity : BaseActivity(), MessageContract.PMView {

    private val pmList: MutableList<PmListBean.MsgListBean> = ArrayList()
    private lateinit var pmPresenter: MessageContract.PMPresenter
    private lateinit var pmAdapter: PMDetailAdapter
    private var uid = 0
    private var userName = ""
    private var page = 1
    private var isSoftInputShow: Boolean = false
        set(value) {
            field = value
            softInputStatusChange()
        }

    override fun setToolbar(): Toolbar? =toolbar_private_detail

    override fun setView(): Int {
        uid = intent.getIntExtra(USER_ID, 0)
        userName = intent.getStringExtra(USER_NAME) ?: "私信详情"
        return R.layout.activity_private_detail
    }

    override fun initView() {
        toolbar_private_detail.title = userName
        pmPresenter = PMDetailPresenterImpl(this)
        pmAdapter = PMDetailAdapter(this, pmList, MyApp.getUser().uid.toInt(), PMTimeUtils())
        recyclerview_private_detail.apply {
            adapter = pmAdapter
        }
        getPmList()
        btn_pm_send.setOnClickListener { sendPM() }
        /**
         * 通过监听父布局的高度变化，判断输入法是否弹出
         * 测试未弹出时diff为76，弹出后为827
         * 参考 https://blog.csdn.net/adayabetter/article/details/78819183
         */
        relative_layout_pm.viewTreeObserver.addOnGlobalLayoutListener {
            val diffHeight = relative_layout_pm.rootView.height - relative_layout_pm.height
            i("PM", diffHeight.toString())
            if (diffHeight >= 500) {
                isSoftInputShow = true
            }}
        //开启后台线程轮询消息
    }

    private fun getPmList() {
        pmPresenter.pmSessionRequest(uid = uid, page = page)
    }

    /**
     * 将数据显示在界面上。recyclerview要移动到底部，添加新数据的时候从底部添加：
     * pmAdapter.notifyItemInserted(pmList.size-1)
     * 下面一句控制视图的滑动是无效的
     * recyclerview_private_detail.scrollToPosition(pmList.size-1)
     * 需要调用 scroll_view_pm.post {
     * scroll_view_pm.scrollTo(0, linear_layout_pm_content.measuredHeight) }
     */
    override fun showPMSession(event: BaseEvent<PMDetailBean>) {
        event.data.body?.pmList?.get(0)?.msgList?.let { pmList.addAll(it) }
        pmAdapter.notifyItemInserted(pmList.size-1)
        scrollViewScrollToBottom()
    }

    private fun sendPM() {
        val stContent: String = edit_view_pm.text.toString()
        if (stContent.isEmpty()) return
        pmPresenter.pmSendRequest(stContent, "text")
        edit_view_pm.text.clear()
        pmList.add(PmListBean.MsgListBean().apply {
            sender = MyApp.getUser().uid
            content = stContent
            type = "text"
            time = System.currentTimeMillis().toString()
        })
    }

    override fun showPMSendResult(event: BaseEvent<PMSendResultBean>) {
        runOnUiThread {
            pmAdapter.notifyItemInserted(pmList.size)
            scrollViewScrollToBottom()
        }
    }

    private fun softInputStatusChange() {
        if (isSoftInputShow) {
            scrollViewScrollToBottom()
        }
    }

    private fun scrollViewScrollToBottom() = scroll_view_pm.post {
        scroll_view_pm.scrollTo(0, linear_layout_pm_content.measuredHeight)
    }

    override fun showError(msg: String) {
        runOnUiThread {
            showToast(msg)
        }
    }
}