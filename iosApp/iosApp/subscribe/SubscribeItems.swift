//
//  SubscribeItems.swift
//  iosApp
//
//  Created by rst10h on 7.12.22.
//

import SwiftUI
import shared
import Toaster

struct SubscribeItems: View {
    var colors: SubscribeColors
    var dimens: SubscribeDimens
    @ObservedObject
    var model: SubscribeViewModel
    let onSubscribe: () -> Void
    
    var body: some View {
        ZStack {
            switch model.uiState {
            case is InspResponseLoading<AnyObject>:
                Text("Loading..")
            case is InspResponseData<SubscribeUiState>:
                SubscribeContent(colors: colors, dimens: dimens, model: model) {
                    onSubscribe()
                }
            default:
                Text("default")
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity)
        //.frame(height: 233)
        //.background(Color.gray)
    }
}

struct SubscribeContent: View {
    var colors: SubscribeColors
    var dimens: SubscribeDimens
    @ObservedObject
    var model: SubscribeViewModel
    let onSubscribe: () -> Void
    
    var body: some View {
        if let state = model.uiState as? InspResponseData<SubscribeUiState>,
           let data = state.data {
            let options = data.options
            VStack(spacing: dimens.optionATopPadding.cg) {
                ForEach(options.indices, id: \.self) { index in
                    let selected = index == data.selectedOptionPos
                    Button(action: {model.onOptionSelected(pos: index.int32)}, label: {
                        HStack(alignment: .center) {
                            ZStack(alignment: .center) {
                                let pad = dimens.radioStrokeWidth.cg
                                Circle()
                                    .fill(selected ? Color.white : colors.radioButtonBorderInactiveColor.toSColor())
                                if (selected) {
                                    Circle()
                                        .fill(LinearGradient(colors: [colors.gradient1Start.toSColor(), colors.gradient1End.toSColor()], startPoint: UnitPoint.bottomLeading, endPoint: UnitPoint.topTrailing))
                                        .padding(pad)
                                } else {
                                    Circle()
                                        .fill(Color.white)
                                        .padding(pad)
                                }
                                //    }
                                
                            }
                            .frame(width: dimens.radioButtonDotSize.cg, height: dimens.radioButtonDotSize.cg, alignment:.center)
                            .padding(dimens.radioButtonPadding.cg)
                            if (options[index].period == .month) {
                                Text("\(decodeOptionText(productModel: options[index]))")
                                    .font(.system(size: dimens.optionText.cg))
                                    .foregroundColor(colors.textOptionDarkColorB.toSColor())
                                    .padding(.leading, 5)
                                    .frame(height: dimens.optionBHeight.cg)
                                Spacer()
                            } else {
                                VStack(alignment: .leading, spacing: 10) {
                                    HStack(spacing: 0) {
                                        Text("\(decodeOptionText(productModel: options[index]))")
                                            .font(.system(size: dimens.optionText.cg))
                                            .fontWeight(.heavy)
                                            .foregroundColor(colors.textOptionDarkColorB.toSColor())
                                        Text(" \(options[index].localizedPrice)")
                                            .font(.system(size: dimens.optionText.cg))
                                            .foregroundColor(colors.textOptionDarkColorB.toSColor())
                                    }
                                    Text(MR.strings().subscribe_price_per_month.format(args_: [data.yearPerMonthPrice!]).localized())
                                        .font(.system(size: dimens.optionText.cg))
                                        .foregroundColor(colors.textOptionDarkColorB.toSColor())
                                }
                                .padding(.leading, 5)
                                .frame(height: dimens.optionBWithLabelHeight.cg - 10.cg)
                                Spacer()
                                if (selected) {
                                    let saveString = MR.strings().subscription_save.format(args_: [""]).localized().uppercased()
                                    let amount = data.yearSaveAmount
                                    Text("\(saveString)\n\(amount ?? "")%")
                                        .font(.system(size: 15))
                                        .fontWeight(.heavy)
                                        .foregroundColor(Color.white)
                                        .multilineTextAlignment(.center)
                                        .lineSpacing(1.5)
                                        .frame(width: dimens.optionBWithLabelHeight.cg - 22.cg, height: dimens.optionBWithLabelHeight.cg - 22.cg)
                                        .background(
                                            LinearGradient(colors: [colors.gradient1Start.toSColor(), colors.gradient1End.toSColor()], startPoint: UnitPoint.leading, endPoint: UnitPoint.trailing)
                                        )
                                        .cornerRadius(15)
                                        .padding(.trailing, 6)
                                    
                                }
                                
                            }
                        }
                        .padding(.leading, 15)
                        .background(selected ? colors.optionBgColorB.toSColor() : Color.clear)
                        .overlay (
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(selected ? colors.optionBorderColorB.toSColor() : Color.clear, lineWidth: 3)
                        )
                        .cornerRadius(20)
                        .padding(.horizontal, dimens.optionStartEndPadding.cg)
                    }
                    )
                }
                
                //if (data.selectedOptionTrialDays > 0) {
                if (options.count > 0) {
                    HStack(spacing: 0) {
                        Text(MR.strings().subscribe_try_days_b.format(args_: [getDaysFree(days: data.selectedOptionTrialDays)]).localized())
                            .font(.system(size: dimens.trialInfoText.cg))
                            .bold()
                            .foregroundColor(colors.textOptionDarkColorB.toSColor())
                        Text(MR.strings().subscribe_after_trial_end.format(args_: [options[data.selectedOptionPos.int].localizedPrice]).localized())
                            .font(.system(size: dimens.trialInfoText.cg))
                            .foregroundColor(colors.textOptionDarkColorB.toSColor())
                    }
                    .visibility(data.selectedOptionTrialDays > 0 ? ViewVisibility.Visible : ViewVisibility.Invisible )
                    .frame(height: 20)
                    .padding(.top, 20.cg)
                    //}
                    if (DebugManager().isDebug) {
                        Button(action: {
                        }, label: { ZStack {
                            Text(MR.strings().subscribe_continue_button.localized().capitalized)
                                .font(.system(size: dimens.subscribeButtonText.cg))
                                .bold()
                                .foregroundColor(Color.white)
                        }
                        .frame(minWidth: 0, maxWidth: .infinity)
                        .frame(height: dimens.subscribeButtonHeight.cg)
                        .background(LinearGradient(colors: [colors.gradient1Start.toSColor(), colors.gradient1End.toSColor()], startPoint: UnitPoint.leading, endPoint: UnitPoint.trailing))
                        .cornerRadius(20)
                        .padding(.horizontal, dimens.optionStartEndPadding.cg)
                        })
                        .simultaneousGesture(LongPressGesture(minimumDuration: 2).onEnded { value in
                            
                            if ( DebugManager().isDebug && value ) {
                                model.onDebugSubscribeLongClick()
                                Toast(text: "Premium is Enabled for debug //\(value)").show()
                                onSubscribe()
                            }
                        })
                        .highPriorityGesture(TapGesture()
                            .onEnded { _ in
                                model.onSubscribeTap { subscribed in
                                    withAnimation {
                                        model.subscribeProcess.toggle()
                                    }
                                    //onSubscribe()
                                }
                            })
                    } else {
                        Button(action: {
                            withAnimation {
                                model.subscribeProcess = true
                            }
                            model.onSubscribeTap { subscribed in
                                withAnimation {
                                    model.subscribeProcess = false
                                    onSubscribe()
                                }
                            }
                        }, label: { ZStack {
                            Text(MR.strings().subscribe_continue_button.localized().capitalized)
                                .font(.system(size: dimens.subscribeButtonText.cg))
                                .bold()
                                .foregroundColor(Color.white)
                        }
                        .frame(minWidth: 0, maxWidth: .infinity)
                        .frame(height: dimens.subscribeButtonHeight.cg)
                        .background(LinearGradient(colors: [colors.gradient1Start.toSColor(), colors.gradient1End.toSColor()], startPoint: UnitPoint.leading, endPoint: UnitPoint.trailing))
                        .cornerRadius(20)
                        .padding(.horizontal, dimens.optionStartEndPadding.cg)
                        })
                    }
                }
            }
        }
    }
    
    private func getDaysFree(days: Int32) -> String {
        let p: PluralsResource = MR.plurals().subscribe_try_days_plural
        
        let pd = PluralFormattedStringDesc(pluralsRes: p, number: days, args: [days])
        
        let localized = pd.localized()
        return localized
    }
    
    private func decodeOptionText(productModel: DisplayProduct) -> String {
        switch productModel.period {
        case .month:
            return MR.strings().subscription_1_month.format(args_: [productModel.localizedPrice]).localized()
        case .year:
            return MR.strings().subscription_12_months.format(args_: [productModel.localizedPrice]).localized()
        default:
            fatalError("unknown period!")
        }
    }
}

struct SubscribeItems_Previews: PreviewProvider {
    @State
    static var isLoading = false
    static var previews: some View {
        SubscribeItems(colors: SubscribeColorsLight(), dimens: SubscribeDimensPhoneH700(), model: SubscribeViewModel(source: "debug_preview")) {}
    }
}
