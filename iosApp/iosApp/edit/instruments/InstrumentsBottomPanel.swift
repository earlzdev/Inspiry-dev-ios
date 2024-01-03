//
//  InstrumentsBottomPanel.swift
//  iosApp
//
//  Created by vlad on 12/1/22.
//

import SwiftUI
import shared

struct InstrumentsBottomPanel: View {
    
    let colors: EditColors
    let dimens: EditDimens
    
    @EnvironmentObject var model: EditViewModelApple
    
    var body: some View {
        let currentInstrument = model.instrumentState.currentMainInstrument
        
        VStack(spacing: 0) {
            if (currentInstrument != nil) {
                InnerPanelUI()
                    .transition(.move(edge: .bottom))
            }
            switch currentInstrument {
            case InstrumentMain.default_:
                if let defaultModel = model.getInnerModelHelper().getDefaultModel() {
                    DefaultInstrumentsUI(model: defaultModel)
                }
            case InstrumentMain.text:
                if let textModel = model.getInnerModelHelper().getTextModel() {
                    TextInstrumentsUI(model: textModel)
                }
            case InstrumentMain.debug:
                DebugPanelUI()
            case InstrumentMain.addViews:
                if let addModel = model.getInnerModelHelper().getAddModel() {
                    AddPanel(model: addModel)
                }
            case InstrumentMain.media:
                if let mediaModel = model.getInnerModelHelper().getMediaModel() {
                    MediaPanel(model: mediaModel)
                }
            case InstrumentMain.movable:
                if let colorModel = model.getInnerModelHelper().getColorModel() {
                    ColorDialogUI(model: colorModel)
                }
            case InstrumentMain.timeline:
                TimeLinePanelApple(model: TimeLineViewModelApple(template: model.templateView))
            case nil: EmptyView()
            default :
                if let defaultModel = model.getInnerModelHelper().getDefaultModel() {
                    DefaultInstrumentsUI(model: defaultModel)
                }
                //                Text("not implemented state " + (currentInstrument?.name ?? "nil"))
                //                    .foregroundColor(.white)
                //                    .frame(height: 160.cg, alignment: .center)
                
            }
            colors.instrumentsBar.toSColor()
                .frame(height: getBottomScreenInset())
        }.frame(alignment: Alignment.center)
            .frame(maxWidth: .infinity)
            .background(colors.instrumentsBar.toSColor())
            .padding(.top, 12)
            .animation(.easeInOut)
            .environmentObject(model)
        
    }
}

struct InstrumentsBottomPanel_Previews: PreviewProvider {
    static var previews: some View {
        InstrumentsBottomPanel(colors: EditColorsLight(), dimens: EditDimensPhone())
            .environmentObject(EditViewModelApple.modelForPreviews())
    }
}
