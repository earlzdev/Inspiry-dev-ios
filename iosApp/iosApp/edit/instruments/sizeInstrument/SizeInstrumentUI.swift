//
//  SizeInstrumentUI.swift
//  iosApp
//
//  Created by rst10h on 11.03.22.
//

import SwiftUI
import shared

struct SizeInstrumentUI: View {
    @StateObject
    var model: TextSizeViewModelApple
    
    var body: some View {
        VStack {
            HStack(alignment: .center) {
                Image("sub_instr_text_size")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 26.cg, height: model.dimens.lineIconHeight.cg)
                    .padding(.leading, model.dimens.linestartSpacer.cg)
                    .padding(.trailing, 7.cg)
                    .colorMultiply(model.colors.textAndIcons.toSColor())
                InspSlider(progress:  $model.textSize)
                .frame(height: model.dimens.lineHeight.cg)
                Text("\(Int((model.textSize * 100).rounded()))")
                    .foregroundColor(model.colors.textAndIcons.toSColor())
                    .font(.system(size: model.dimens.textSize.cg))
                    .frame(width: 28.cg)
                    .padding(.trailing, model.dimens.lineEndSpacer.cg)

                
            }
            HStack {
                Image("sub_instr_char_spacing")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24.cg, height: model.dimens.lineIconHeight.cg)
                    .padding(.leading, model.dimens.linestartSpacer.cg)
                    .padding(.trailing, 7.cg)
                    .colorMultiply(model.colors.textAndIcons.toSColor())
                InspSlider(progress: $model.letterSpacing)
                .frame(height: model.dimens.lineHeight.cg)
                Text("\(Int((model.letterSpacing * 100).rounded()))")
                    .foregroundColor(model.colors.textAndIcons.toSColor())
                    .font(.system(size: model.dimens.textSize.cg))
                    .frame(width: 28.cg)
                    .padding(.trailing, model.dimens.lineEndSpacer.cg)
            }
            HStack {
                Image("sub_instr_line_spacing")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 25.cg, height: model.dimens.lineIconHeight.cg)
                    .padding(.leading, model.dimens.linestartSpacer.cg)
                    .padding(.trailing, 7.cg)
                    .colorMultiply(model.colors.textAndIcons.toSColor())
                InspSlider(progress: $model.lineSpacing)
                .frame(height: model.dimens.lineHeight.cg)
                Text("\(Int((model.lineSpacing * 100).rounded()))")
                    .foregroundColor(model.colors.textAndIcons.toSColor())
                    .font(.system(size: model.dimens.textSize.cg))
                    .frame(width: 28.cg)
                    .padding(.trailing, model.dimens.lineEndSpacer.cg)
            }
        }                    .frame(height: model.dimens.barHeight.cg)
            .padding(.vertical)
    }
}

struct SizeInstrumentUI_Previews: PreviewProvider {
    static var previews: some View {
        SizeInstrumentUI(model: TextSizeViewModelApple())
            .preferredColorScheme(.dark)
    }
}
