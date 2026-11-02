package ind.finance.enersense.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ind.finance.enersense.data.remote.dto.ConsumptionDto
import ind.finance.enersense.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (ConsumptionDto) -> Unit
) : ListAdapter<ConsumptionDto, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
        private val onItemClick: (ConsumptionDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ConsumptionDto) {
            val rawDate = item.date ?: item.timestamp ?: ""
            binding.tvHistoryDate.text = formatDate(rawDate)

            val wh = item.consumption ?: item.totalConsumption ?: 0.0
            val kwh = wh / 1000.0
            binding.tvHistoryConsumption.text = String.format(Locale.US, "%.2f kWh", kwh)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun formatDate(dateStr: String): String {
            if (dateStr.length >= 10) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                    val parsed = inputFormat.parse(dateStr.substring(0, 10))
                    if (parsed != null) {
                        return outputFormat.format(parsed)
                    }
                } catch (e: Exception) {
                    // fallback
                }
            }
            return dateStr
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ConsumptionDto>() {
        override fun areItemsTheSame(oldItem: ConsumptionDto, newItem: ConsumptionDto): Boolean {
            return (oldItem.date == newItem.date) && (oldItem.timestamp == newItem.timestamp)
        }

        override fun areContentsTheSame(oldItem: ConsumptionDto, newItem: ConsumptionDto): Boolean {
            return oldItem == newItem
        }
    }
}
