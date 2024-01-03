//
//  PreviewOverlayUI.swift
//  iosApp
//
//  Created by vlad on 12/1/22.
//

import SwiftUI
import shared

struct PreviewOverlayUI: View {
    let displayProLabel: Bool
    let onBackPressed: () -> ()
    
    let shadowColors: [SwiftUI.Color] = [Color.black.opacity(0.58), Color.clear]
    let shadowHeight: CGFloat = CGFloat(80)
    
    @StateObject
    var vm: PreviewViewModel
    
    init (displayProLabel: Bool, templateView: InspTemplateView, onBackPressed: @escaping () -> ()) {
        self.displayProLabel = displayProLabel
        self.onBackPressed = onBackPressed
        _vm = StateObject(wrappedValue: PreviewViewModel(templateView: templateView))
    }
    
    var body: some View {
        ZStack(alignment: .top) {
            
            VStack(spacing: 0) {
                
                PreviewProgress(colors: vm.colors, dimens: vm.dimens, progress: vm.frameProgress)
                    .padding(.horizontal, CGFloat(vm.dimens.progressStartEndPadding))
                    .padding(.top, CGFloat(vm.dimens.progressTopPadding))
                
                
                HStack(spacing: 0) {
                    
                    if vm.instLayoutVisible {
                        vm.colors.storiesIconCircle.toSColor()
                            .frame(width: CGFloat(vm.dimens.storiesIconSize), height: CGFloat(vm.dimens.storiesIconSize))
                            .clipShape(Circle())
                            .overlay(Circle()
                                        .stroke(Color.white, lineWidth: CGFloat(vm.dimens.defaultBorderWidth)))
                        
                        Text(MR.strings().preview_template_stories_profile.localized())
                            .font(.system(size: CGFloat(vm.dimens.storiesProfileText), weight: .bold))
                            .foregroundColor(Color.white)
                            .padding(.leading, CGFloat(vm.dimens.storiesProfileStartPadding))
                            .lineLimit(1)
                            .fixedSize()
                            .frame(maxWidth: .infinity, alignment: .leading)
                    } else {
                        Spacer()
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    
                    Button(action: onBackPressed) {
                        Image("ic_preview_close").padding(.all, CGFloat(vm.dimens.closeIconClickablePadding))
                    }.padding(.trailing, CGFloat(vm.dimens.closeIconEndPadding))
                    
                    
                }.padding(.leading, CGFloat(vm.dimens.IGTopLayoutStartPadding))
                    .padding(.top, 5)
                    .frame(maxWidth: .infinity)
                
            }.frame(maxWidth: .infinity)
                .frame(height: shadowHeight, alignment: .top)
                .background(LinearGradient(colors: shadowColors, startPoint: UnitPoint(x: 0, y: 0), endPoint: UnitPoint(x: 0, y: 1)))
            
            if vm.instLayoutVisible {
                ZStack(alignment: .bottom) {
                    
                    
                    LinearGradient(colors: shadowColors, startPoint: UnitPoint(x: 0, y: 1), endPoint: UnitPoint(x: 0, y: 0)).frame(maxWidth: .infinity)
                        .frame(height: shadowHeight)
                    
                    
                    HStack {
                        Image("ic_photo_stories").padding(.leading, 14)
                        
                        ZStack {
                            
                            Image("ic_context_stories")
                                .padding(.trailing, CGFloat(vm.dimens.storiesCommentsEndPadding) - 7)
                            
                        }.frame(maxWidth: .infinity, alignment: .trailing)
                            .frame(height: 34)
                            .overlay(RoundedRectangle(cornerRadius: 100).stroke(Color.white, lineWidth: 1))
                        
                            .padding(.leading, CGFloat(vm.dimens.storiesCommentsStartPadding))
                            .padding(.trailing, CGFloat(vm.dimens.storiesCommentsEndPadding) - 5)
                        
                        Image("ic_direct_stories").padding(.trailing, 20)
                        
                    }.padding(.bottom, 10)
                    
                }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                    .padding(.bottom, getBottomScreenInset())
            }
            
            
        }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            .background(displayProLabel ? AnyView(PreviewProLabel(colors: vm.colors, dimens: vm.dimens)) : AnyView(Spacer()))
        .contentShape(Rectangle())
        .onLongPressGesture {
            vm.onLongPress()
        }.onAppear {
            vm.mayShowToast()
        }
        .clipped()
    }
}

struct PreviewOverlayUI_Previews: PreviewProvider {
    static var previews: some View {
        PreviewOverlayUI(displayProLabel: true, templateView: InspTemplateViewApple.fakeInitializedTemplate(), onBackPressed: {}).background(Color.black)
    }
}
