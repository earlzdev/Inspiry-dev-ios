//
//  InstrumentsModelHelperApple.swift
//  iosApp
//
//  Created by rst10h on 10.03.22.
//

import Foundation
import shared

class InstrumentsModelHelperApple {
    
    let instrumentsManager: InstrumentsManager
    let templateChangedAction: (() -> ())?
    
    init (instrumentsManager: InstrumentsManager, templateChangedAction: (() -> ())?) {
        self.instrumentsManager = instrumentsManager
        self.templateChangedAction = templateChangedAction
    }
    
    func getTextModel() -> TextInstrumentsModelApple? {
        guard let model = instrumentsManager.getTextModel() else { return nil }
        return TextInstrumentsModelApple(model: model)
    }
    
    func getDefaultModel() -> DefaultInstrumentsModelApple? {
        guard let model = instrumentsManager.getDefaultModel() else { return nil }
        return DefaultInstrumentsModelApple(model: model)
    }
    
    func getColorModel() -> ColorDialogModelApple? {
        guard let model = instrumentsManager.getColorModel() else { return nil }
        return ColorDialogModelApple(model, notifyTemplteChanged: templateChangedAction ?? { } )
    }
    
    func getFormatModel() -> FormatInstrumentModelApple? {
        guard let model = instrumentsManager.getFormatModel() else { return nil }
        return FormatInstrumentModelApple(model: model)
    }
    
    func getSizeModel() -> TextSizeViewModelApple? {
        guard let model = instrumentsManager.getSizeModel() else { return nil }
        return TextSizeViewModelApple(model: model)
    }
    
    func getFontsModel() -> FontsViewModelApple? {
        guard let model = instrumentsManager.getFontModel() else { return nil }
        return FontsViewModelApple(model)
    }
    
    func getAddModel() -> AddInstrumentsModelApple? {
        guard let model = instrumentsManager.getAddViewsModel() else { return nil }
        return AddInstrumentsModelApple(model: model)
    }
    
    func getMediaModel() -> MediaInstrumentsModelApple? {
        guard let model = instrumentsManager.getMediaModel() else {return nil}
        return MediaInstrumentsModelApple(model: model)
    }
    
    func getShapesModel() -> ShapeInstrumentsModelApple? {
        guard let model = instrumentsManager.getShapesModel() else {return nil}
        return ShapeInstrumentsModelApple(model: model)
    }
    func getMoveAnimModel(wrapperHelper: EditWrapperHelperApple) -> MoveAnimInstrumentModelApple? {
        guard let model = instrumentsManager.getMoveModel() else { return nil }
        return MoveAnimInstrumentModelApple(model: model, wrapperHelper: wrapperHelper)
    }
    func getMusicModel() -> MusicEditModelApple {
        let music = instrumentsManager.templateView.template_.music
        let musicModel = MusicEditModelApple(musicPlayerModel: MusicPlayerViewModel(isPlaying: false, selectedTrack: music?.url), templateView: instrumentsManager.templateView) {
                self.instrumentsManager.mayRemoveAdditionalPanel()
                self.instrumentsManager.selectFullScreenTool(newState: .music, closePanel: true)
            }
        return musicModel
    }
    
   func getVideoEditModel() -> VideoEditModelApple? {
       guard let model = instrumentsManager.getVideoEditModel() else { return nil }
       return VideoEditModelApple(model: model)
   }
    
    func getSlidesModel() -> SlidesInstrumentViewModelApple? {
        guard let model = instrumentsManager.getSlidesModel() else { return nil }
        return SlidesInstrumentViewModelApple(model: model)
    }
}
