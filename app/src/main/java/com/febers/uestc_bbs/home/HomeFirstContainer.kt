package com.febers.uestc_bbs.home

import android.os.Bundle
import com.febers.uestc_bbs.R
import com.febers.uestc_bbs.base.BaseFragment
import com.febers.uestc_bbs.module.post.view.PListPagerFragment

class HomeFirstContainer : BaseFragment() {

    override fun setContentView(): Int {
        return R.layout.container_home_first
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (findChildFragment(PListPagerFragment::class.java) == null) {
            loadRootFragment(R.id.home_first_container, PListPagerFragment())
        }
    }
}