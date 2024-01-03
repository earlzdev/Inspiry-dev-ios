//
//  SlidesPanelUI.swift
//  iosApp
//
//  Created by rst10h on 1.09.22.
//

import SwiftUI
import UniformTypeIdentifiers
import shared

struct SlidesPanelUI: View {
    @ObservedObject
    var model: SlidesInstrumentViewModelApple
       
    @EnvironmentObject
    var editModel: EditViewModelApple
    
    var body: some View {
        VStack {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(alignment: .center, spacing: 6) {
                    ForEach(model.slidesList, id: \.self) { slide in
                        let isDragged = model.draggedItem == slide
                        let selectedColor = model.selectedSLideId == slide ? 0xff00c2ff.ARGB : Color.clear
                        let borderColor = model.selectedSLideId == slide ? Color.clear : Color.black
                        let path = model.getUrlByID(id: slide)
                        OneSlidePreview(model: SlidePreviewModel(url: path))
                            .padding(1)
                            .background(RoundedRectangle(cornerRadius: 7).fill(borderColor))
                            .padding(2)
                            .background(RoundedRectangle(cornerRadius: 7).fill(selectedColor))
                            .opacity(isDragged ? 0.01 : 1)
                            .onTapGesture {
                                model.selectSlide(id: slide)
                            }
                            .if(model.selectedSLideId == slide) { view in
                                view.onDrag {
                                    model.draggedItem = slide
                                    let itemProvider = MovableItemProvider(object: String(slide) as NSString)
                                    
                                    itemProvider.onFinishedAction = {
                                        model.onMoveFinished()
                                    }
                                    return itemProvider
                                }
                            }
                            .onDrop(of: [UTType.text], delegate: DragRelocateDelegate(item: slide, listData: $model.slidesList, current: $model.draggedItem))
                            .padding(.vertical, 5)
                            .padding(.horizontal, 5)

                    }
                    if (model.coreModel.emptySlidesCount().int > 0) {
                    Button(action: { model.mediaPickerActive = true }) {
                            HStack {
                                
                                CyborgImage(name: "ic_add_slide")
                                    .frame(width: 35, height: 35)
                                Text(MR.strings().instrument_add.localized())
                                    .font(.system(size: 10.cg))
                                    .foregroundColor(Color.white)
                            }
                        }
                            .sheet(isPresented: $model.mediaPickerActive) {
                                MediaPickerUI(mediaResult: $model.mediaResult, isActive: $model.mediaPickerActive, isLoading: $editModel.waitLoading, iCloudProgress: $editModel.loadingProgress, maxMediasCount: model.coreModel.emptySlidesCount().int)
                            }
                    }
                }
            }
            .padding(.leading, 15)
        }
        .frame(height: 60.cg)
        .background(0xff292929.ARGB)
        
    }
}
