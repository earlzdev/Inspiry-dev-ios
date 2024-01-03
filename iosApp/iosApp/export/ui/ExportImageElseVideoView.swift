//
// Created by vlad on 9/12/21.
//

import SwiftUI
import shared

struct ExportImageElseVideoView: View {
    let colors: EditColors
    let dimens: EditDimens
    let imageElseVideo: Bool
    let onChange: (Bool) -> ()

    var body: some View {
        ZStack {

            let shape = RoundedRectangle(cornerRadius: CGFloat(dimens.exportImageElseVideoButtonCornerRadius))
            let buttonWidth = CGFloat(dimens.exportImageElseVideoButtonWidth)

            let buttonOffset = imageElseVideo ? buttonWidth / 2 : buttonWidth / -2
            colors.exportImageElseVideoSelectedBg.toSColor()
                    .clipShape(shape)
                    .frame(maxHeight: .infinity)
                    .frame(width: buttonWidth)
                    .offset(x: buttonOffset)
                    .animation(.easeInOut, value: imageElseVideo)

            let selectedTextColor = colors.exportImageElseVideoSelectedText.toSColor()
            let unselectedTextColor = colors.exportImageElseVideoSelectedBg.toSColor()

            let firstButtonColor = imageElseVideo ? unselectedTextColor : selectedTextColor
            let secondButtonColor = imageElseVideo ? selectedTextColor : unselectedTextColor

            HStack(spacing: 0) {

                Text(MR.strings().animation_enabled.localized())
                        .foregroundColor(Color.white)
                        .colorMultiply(firstButtonColor)
                        .frame(maxHeight: .infinity)
                        .frame(width: buttonWidth)
                        .contentShape(shape)
                        .onTapGesture(perform: {
                            onChange(false)
                        })
                        .font(.system(size: 14, weight: .bold))
                        .lineLimit(1)
                        .animation(.easeInOut, value: imageElseVideo)

                Text(MR.strings().animation_disabled.localized())
                        .foregroundColor(Color.white)
                        .colorMultiply(secondButtonColor)
                // contentShape make the whole area clickable. Otherwise only opaque area is available for onTapGesture
                        .frame(maxHeight: .infinity)
                        .frame(width: buttonWidth)
                        .contentShape(shape)
                        .onTapGesture(perform: {
                            onChange(true)
                        })
                        .font(.system(size: 14, weight: .bold))
                        .lineLimit(1)
                        .animation(.easeInOut, value: imageElseVideo)

            }.frame(maxWidth: .infinity, maxHeight: .infinity)
        }
                .padding(.horizontal, CGFloat(dimens.exportImageElseVideoOuterPaddingHorizontal))
                .padding(.vertical, CGFloat(dimens.exportImageElseVideoPaddingVertical))

                .frame(width: CGFloat(dimens.exportImageElseVideoWidth),
                        height: CGFloat(dimens.exportImageElseVideoHeight))
                .background(colors.exportImageElseVideoBg.toSColor())
                .clipShape(RoundedRectangle(cornerRadius: CGFloat(dimens.exportImageElseVideoCornerRadius)))
    }
}

struct ExportImageElseVideoView_Previews: PreviewProvider {
    @State
    static var imageElseVideo = false

    static var previews: some View {

        ExportImageElseVideoView(colors: EditColorsLight(),
                dimens: EditDimensPhone(),
                imageElseVideo: imageElseVideo, onChange: { it in imageElseVideo = it })
    }
}
