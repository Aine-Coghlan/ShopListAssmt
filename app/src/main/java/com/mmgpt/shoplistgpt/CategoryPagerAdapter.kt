package com.mmgpt.shoplistgpt

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CategoryPagerAdapter(
    activity: FragmentActivity,
    private val viewModel: ShoppingListViewModel,
    private val planMode: Boolean = false
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int =
        (viewModel.categories.value?.size ?: 0) + 1

    override fun getItemId(position: Int): Long {
        val categories = viewModel.categories.value.orEmpty()

        return when {
            planMode && position == 0 -> CATEGORY_LIST_PAGE_ID
            !planMode && position == categories.size -> CATEGORY_LIST_PAGE_ID
            else -> categories
                .getOrNull(if (planMode) position - 1 else position)
                ?.id ?: -1L
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        if (itemId == CATEGORY_LIST_PAGE_ID) return true
        return viewModel.categories.value.orEmpty().any { it.id == itemId }
    }

    override fun createFragment(position: Int): Fragment {
        val categories = viewModel.categories.value.orEmpty()

        return if (planMode) {
            if (position == 0) {
                PlanCategoriesFragment()
            } else {
                PlanCategoryFragment.newInstance(categories[position - 1].id)
            }
        } else {
            if (position == categories.size) {
                ShopCategoriesFragment()
            } else {
                CategoryFragment.newInstance(categories[position].id)
            }
        }
    }

    private companion object {
        const val CATEGORY_LIST_PAGE_ID = Long.MIN_VALUE
    }
}
