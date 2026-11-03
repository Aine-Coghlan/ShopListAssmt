package com.mmgpt.shoplistgpt

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryPagerAdapterTest {

    private val originalCategories = listOf(
        ShoppingCategory(101L, "Produce", emptyList()),
        ShoppingCategory(202L, "Dairy", emptyList())
    )

    private val reorderedCategories = listOf(
        originalCategories[1],
        originalCategories[0]
    )

    @Test
    fun shopMode_categoryPageIdentityFollowsCategoryAfterReorder() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel =
                    ViewModelProvider(activity)[ShoppingListViewModel::class.java]
                viewModel.setCategories(originalCategories)

                val adapter = CategoryPagerAdapter(
                    activity,
                    viewModel,
                    planMode = false
                )
                val producePageId = adapter.getItemId(0)

                viewModel.setCategories(reorderedCategories)

                assertEquals(
                    "Produce's page identity should move with Produce",
                    producePageId,
                    adapter.getItemId(1)
                )
            }
        }
    }

    @Test
    fun planMode_categoryPageIdentityFollowsCategoryAfterReorder() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel =
                    ViewModelProvider(activity)[ShoppingListViewModel::class.java]
                viewModel.setCategories(originalCategories)

                val adapter = CategoryPagerAdapter(
                    activity,
                    viewModel,
                    planMode = true
                )
                // Plan mode has the category list at position 0.
                val producePageId = adapter.getItemId(1)

                viewModel.setCategories(reorderedCategories)

                assertEquals(
                    "Produce's page identity should move with Produce",
                    producePageId,
                    adapter.getItemId(2)
                )
            }
        }
    }
}