package me.debaxom.disang.layout

import me.debaxom.disang.model.KeyModel

class KeyboardLayoutEngine {

    fun layout(
        rows: List<List<KeyModel>>,
        width: Int,
        keyHeight: Float,
        gap: Float,
        startY: Float
    ) {

        var y = startY

        // --------------------------------
        // SIDE PADDING (prevents edge touch)
        // --------------------------------
        val sidePadding = gap * 2f
        val usableWidth = width - (sidePadding * 2f)

        // Global reference width (largest row)
        val maxWeight = rows.maxOf { row ->
            row.sumOf { it.weight.toDouble() }.toFloat()
        }

        val totalGapForMaxRow = (maxWeight - 1f) * gap
        val baseKeyUnit =
            (usableWidth - totalGapForMaxRow) / maxWeight

        rows.forEachIndexed { index, row ->

            val rowWeight =
                row.sumOf { it.weight.toDouble() }.toFloat()

            // Rows that should fill full width:
            val shouldFillWidth =
                index == 0 ||       // numbers row
                index == 1 ||       // qwerty row
                index == rows.size - 1 ||   // bottom row
                index == rows.size - 2      // second-last row

            val rowWidth =
                if (shouldFillWidth) {
                    usableWidth
                } else {
                    (rowWeight * baseKeyUnit) +
                    ((row.size - 1) * gap)
                }

            var x =
                if (shouldFillWidth) {
                    sidePadding
                } else {
                    sidePadding + (usableWidth - rowWidth) / 2f
                }

            // Stretch keys only for full-width rows
            val unit =
                if (shouldFillWidth) {
                    (usableWidth - (row.size - 1) * gap) / rowWeight
                } else {
                    baseKeyUnit
                }

            row.forEach { key ->

                val w = key.weight * unit

                key.bounds.set(
                    x,
                    y,
                    x + w,
                    y + keyHeight
                )

                x += w + gap
            }

            y += keyHeight + gap
        }
    }
}
