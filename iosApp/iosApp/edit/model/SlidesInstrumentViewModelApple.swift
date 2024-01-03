//
//  SlidesInstrumentViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 1.09.22.
//

import Foundation
import shared

class SlidesInstrumentViewModelApple: MovableListDelegate, ObservableObject {
    
    let coreModel: SlideInstrumentViewModel
    
    @Published
    var slidesList: [String] = []
   
    @Published
    var mediaResult: [PickMediaResult] = [] {
        didSet {
            if (mediaResult.count > 0) {
                self.coreModel.onNewSlideAppend(newSlides: mediaResult)
                mediaResult = []
            }
        }
    }
    
    @Published
    var mediaPickerActive: Bool = false
    
    @Published
    var draggedItem: String? = nil {
        didSet {
            print("slide dragged item = \(draggedItem) old = \(oldValue)")
            if (oldValue != nil && draggedItem == nil) {
                onMoveFinished(movedID: oldValue!)
            }
        }
    }
    
    @Published
    var isTargeted: Bool? = false {
        didSet {

        }
    }
    
    @Published
    var selectedSLideId: String? = nil
    
    init(model: SlideInstrumentViewModel) {
        self.coreModel = model
        
        slidesList = model.slideList.value as! [String]
        selectedSLideId = model.selectedSlideID.value as? String

        CoroutineUtil.onReceived(state: model.slideList, onValueReceived: { [weak self] in
            self?.slidesList = model.slideList.value as! [String]
        })
        
        CoroutineUtil.watch(state: model.selectedSlideID, allowNilState: true) { [weak self] in
            self?.selectedSLideId = $0
        }
    }

    func selectSlide(id: String) {
        print("slide select slide \(id)")
        coreModel.setSelected(id: id)
        invalidateTemplate()
    }
    
    
    private func invalidateTemplate() {
        print("slide invalidate template")
        let template = (coreModel.currentView.value as? InspMediaView)?.templateParent
        template?.objectWillChanged()
        
    }
       
    func getUrlByID(id: String) -> URL? {
        guard let path = coreModel.getUrlOrNull(id: id) else { return nil }
        return URL(string: path)
    }
    
    private func onMoveFinished(movedID: String) {
            print("slide move finished \(movedID)")
            print("slide move originalList: \(coreModel.slideList.value as! [String])")
            print("slide move localList: \(slidesList)")
            guard let index = slidesList.firstIndex(of: movedID) else { return }
            print("slide move \(movedID) newIndex \(index)")
            coreModel.replaceSlides(id: movedID, newIndex: index.int32) {
                print("slide set selected \(movedID)")
                self.coreModel.setSelected(id: movedID)
                print("slide template invalidate")
                let template = (self.coreModel.currentView.value as? InspMediaView)?.templateParent.setFrameForEdit()
                    self.invalidateTemplate()
        }
    }
    
    func onMoveFinished() {
        print("slide move finished")
        let originalList = self.coreModel.slideList.value as! [String]
        for (index, item) in slidesList.enumerated() {
            if item != originalList[index] {
                DispatchQueue.main.async {
                    self.draggedItem = nil
                }

                return
            }
        }
    }
}
