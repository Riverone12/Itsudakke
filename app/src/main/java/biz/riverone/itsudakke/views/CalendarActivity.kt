package biz.riverone.itsudakke.views

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import biz.riverone.itsudakke.R
import biz.riverone.itsudakke.common.ApplicationControl
import biz.riverone.itsudakke.models.TaskItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * CalendarActivity.kt: 履歴のカレンダー表示アクティビティ
 * Copyright (C) 2018 J.Kawahara
 * 2018.3.21 J.Kawahara 新規作成
 */

class CalendarActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY_TASK_ITEM = "taskItem"
        private const val MAX_MONTHS = 13
    }

    private lateinit var mAdView: AdView
    private lateinit var taskItem: TaskItem

    private val pager: ViewPager by lazy { findViewById<ViewPager>(R.id.pager) }
    private lateinit var pagerAdapter: CalendarPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // 画面をポートレートに固定する
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        taskItem = if (intent.hasExtra(EXTRA_KEY_TASK_ITEM)) {
            intent.getParcelableExtra(EXTRA_KEY_TASK_ITEM)
        } else {
            TaskItem()
        }

        // タイトルを設定する
        title = taskItem.title

        // ページャーの準備
        prepareFragments()

        initializeControls()

        // AdMob
        MobileAds.initialize(this, "ca-app-pub-1882812461462801~4066766979")
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun initializeControls() {

        // 一覧表示へのリンクの準備
        val linkToList = findViewById< FrameLayout>(R.id.linkToList)
        linkToList.setOnClickListener {
            val sendIntent = Intent(this, HistoryActivity::class.java)
            sendIntent.putExtra(HistoryActivity.EXTRA_KEY_TASK_ITEM, taskItem)
            startActivity(sendIntent)
        }
    }

    private fun prepareFragments() {
        // データがある月からのみ表示する

        val minYmd = TaskItem.minimamDoneDate(ApplicationControl.database, taskItem.id)
        pagerAdapter = CalendarPagerAdapter(this.supportFragmentManager)
        pager.adapter = pagerAdapter
        pagerAdapter.initialize(taskItem, MAX_MONTHS, minYmd)
    }

    override fun onResume() {
        super.onResume()

        prepareFragments()

        val pageCount = pagerAdapter.count
        if (pageCount > 0) {
            pager.currentItem = pageCount - 1
        }
    }
}
