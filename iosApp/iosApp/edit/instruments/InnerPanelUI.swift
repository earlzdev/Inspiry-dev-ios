//
//  InnerPanelView.swift
//  iosApp
//
//  Created by rst10h on 5.02.22.
//

import SwiftUI
import shared

struct InnerPanelUI: View {
    @EnvironmentObject
    var editModel: EditViewModelApple
    
    var body: some View {
        let currentInnerInstrument = editModel.instrumentState.currentAdditionalInstrument
        Group {
            switch currentInnerInstrument {
            case InstrumentAdditional.format:
                if let model = editModel.getInnerModelHelper().getFormatModel() {
                    FormatInstrumentUI(model: model)
                }
            case InstrumentAdditional.color:
                if let model = editModel.getInnerModelHelper().getColorModel() {
                    ColorDialogUI(model: model)
                }
            case InstrumentAdditional.back:
                if let model = editModel.getInnerModelHelper().getColorModel() {
                    ColorDialogUI(model: model)
                }
            case InstrumentAdditional.size:
                if let model = editModel.getInnerModelHelper().getSizeModel() {
                    SizeInstrumentUI(model: model)
                }
            case InstrumentAdditional.font:
                if let model = editModel.getInnerModelHelper().getFontsModel() {
                    FontDialogView(viewModel: model)
                }
            case InstrumentAdditional.editMusic:
                MusicPanelUI(model: editModel.getInnerModelHelper().getMusicModel())
            case InstrumentAdditional.shape:
                if let model = editModel.getInnerModelHelper().getShapesModel() {
                    ShapesPanel(model: model)
                }
            case InstrumentAdditional.move:
                if let model =
                    editModel.getInnerModelHelper()
                    .getMoveAnimModel(wrapperHelper: editModel.wrapperHelper) {
                    MoveAnimPanel(model: model)
                }
            case InstrumentAdditional.trim:
                if let model = editModel.getInnerModelHelper().getVideoEditModel() {
                    TrimVideoPanel(model: model)
                }
            case InstrumentAdditional.volume:
                if let model = editModel.getInnerModelHelper().getVideoEditModel() {
                    VideoVolumePanel(model: model)
                }
            case InstrumentAdditional.slide:
                if let model = editModel.getInnerModelHelper().getSlidesModel() {
                    SlidesPanelUI(model: model)
                }
            case nil: EmptyView()
            default:
                Text("not implemented state " + (currentInnerInstrument?.name ?? "nil"))
                    .foregroundColor(.white)
                    .frame(height: 160.cg ,alignment: .center)
            }
        }
        //        .transition(.move(edge: .bottom))
    }
}

struct InnerPanelView_Previews: PreviewProvider {
    static let tv = InspTemplateViewApple.fakeInitializedTemplate()
    static let model = ColorDialogModelApple(tv){}
    
    static var previews: some View {
        InnerPanelUI()
            .environmentObject(EditViewModelApple.modelForPreviews())
    }
}
