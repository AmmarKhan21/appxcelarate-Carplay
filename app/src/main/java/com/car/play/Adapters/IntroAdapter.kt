package com.car.play.android.app.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.car.play.android.app.data_classes.Intro
import com.car.play.android.app.databinding.ItemIntroBinding


class IntroAdapter(private val mContext: Context, private val mListScreen: List<Intro>) :
    PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val binding = ItemIntroBinding.inflate(inflater, container, false)

        binding.textTitle.text = mListScreen[position].title
        binding.textDescription.text = mListScreen[position].description
        binding.imgSlideIcon.setImageResource(mListScreen[position].icon)
        container.addView(binding.root)

        return binding.root
    }

    override fun getCount(): Int {
        return mListScreen.size
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}