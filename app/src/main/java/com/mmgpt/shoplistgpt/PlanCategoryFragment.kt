package com.mmgpt.shoplistgpt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmgpt.shoplistgpt.adapter.ItemAdapter
import com.mmgpt.shoplistgpt.databinding.FragmentCategoryBinding

class PlanCategoryFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by activityViewModels()
    private var categoryId: Long = 0L
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getLong(ARG_CATEGORY_ID) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chkSelectAll.visibility = View.VISIBLE

        lateinit var adapter: ItemAdapter
        val dragHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
                    return false
                }

                val itemFrom = adapter.currentList.getOrNull(from)
                val itemTo = adapter.currentList.getOrNull(to)
                if (itemFrom?.isPlaceholder == true || itemTo?.isPlaceholder == true) {
                    return false
                }

                val currentIndex = viewModel.categories.value
                    .orEmpty()
                    .indexOfFirst { it.id == categoryId }
                if (currentIndex < 0) return false

                viewModel.reorderItem(currentIndex, from - 1, to - 1)
                adapter.moveItem(from, to)
                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) = Unit

            override fun isLongPressDragEnabled(): Boolean = false
        })

        adapter = ItemAdapter(
            onAction = { item, action ->
                when (action) {
                    ItemAdapter.Action.CHECK -> viewModel.checkItem(item)
                    ItemAdapter.Action.EDIT -> showEditDialog(item)
                    ItemAdapter.Action.DELETE -> viewModel.deleteItem(item)
                    else -> Unit
                }
            },
            mode = ItemAdapter.Mode.PLAN,
            dragStart = { viewHolder -> dragHelper.startDrag(viewHolder) },
            onAdd = { position, name ->
                val currentIndex = viewModel.categories.value
                    .orEmpty()
                    .indexOfFirst { it.id == categoryId }

                if (currentIndex >= 0) {
                    viewModel.addItem(currentIndex, position - 1, name)
                }
            }
        )

        binding.recyclerView.adapter = adapter
        dragHelper.attachToRecyclerView(binding.recyclerView)

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryIndex = categories.indexOfFirst { it.id == categoryId }
            val category = categories.getOrNull(categoryIndex)
                ?: return@observe

            binding.tvHeading.text = category.heading
            binding.tvCount.text = ""

            val allChecked = category.items
                .filterNot { it.isHeading }
                .all { it.isChecked }

            binding.chkSelectAll.setOnCheckedChangeListener(null)
            binding.chkSelectAll.isChecked = allChecked
            binding.chkSelectAll.setOnCheckedChangeListener { _, isChecked ->
                val currentIndex = viewModel.categories.value
                    .orEmpty()
                    .indexOfFirst { it.id == categoryId }

                if (currentIndex >= 0) {
                    viewModel.setAllChecked(currentIndex, isChecked)
                }
            }

            val items = mutableListOf<ShoppingItem>()
            items.add(ShoppingItem(-1, "", isPlaceholder = true))
            items.addAll(category.items)
            items.add(ShoppingItem(-2, "", isPlaceholder = true))
            adapter.submitList(items)
        }
    }

    private fun showEditDialog(item: ShoppingItem) {
        val input = android.widget.EditText(requireContext()).apply {
            setText(item.name)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.editItem(item, text)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: Long): PlanCategoryFragment {
            return PlanCategoryFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CATEGORY_ID, categoryId)
                }
            }
        }
    }
}
