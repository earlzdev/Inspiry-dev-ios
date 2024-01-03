package app.inspiry.core.media

enum class Alignment {
    top_start, top_center, top_end, center_start, center, center_end, bottom_start, bottom_center, bottom_end;

    enum class Horizontal {
        Start, Center, End;
    }
    enum class Vertical {
        Top, Center, Bottom;

        operator fun plus(horizontal: Horizontal): Alignment {
            return when (this) {
                Top -> {
                    when (horizontal) {
                        Horizontal.Start -> top_start
                        Horizontal.Center -> top_center
                        Horizontal.End -> top_end
                    }
                }
                Center -> {
                    when (horizontal) {
                        Horizontal.Start -> center_start
                        Horizontal.Center -> Alignment.center
                        Horizontal.End -> center_end
                    }
                }
                Bottom -> {
                    when (horizontal) {
                        Horizontal.Start -> bottom_start
                        Horizontal.Center -> bottom_center
                        Horizontal.End -> bottom_end
                    }
                }
            }
        }
    }
}