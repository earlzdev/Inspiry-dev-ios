//
//  FontsListView.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import SwiftUI
import shared

struct FontsListView: View {
    
    let colors: FontDialogColors
    let dimens: FontDialogDimens
    let fonts: [FontPath]
    let isUpload: Bool
    let selectedFont: FontPath
    let onSelectedChanged: (FontPath) -> ()
    
    let fontObtainer: PlatformFontObtainerImpl = Dependencies.diContainer.resolveAuto()
    
    @EnvironmentObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    func uploadFontClick() {
        //TODO: implement
    }
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            
            HStack(spacing: 0) { //lazyHStack is preferable, but it has a bug with animation
                
                Spacer()
                    .frame(width: CGFloat(dimens.fontsListContentPadding))
                
                if isUpload {
                    
                    Button(action: uploadFontClick, label: {
                        HStack(spacing: 0) {
                            Image("ic_fonts_upload")
                                .padding(.trailing, 9)
                            
                            Text(MR.strings().upload_font.localized())
                                .lineLimit(2)
                                .foregroundColor(colors.categoryTextInactive.toSColor())
                                .font(.system(size: CGFloat(dimens.uploadLabelTextSize)))
                        }
                    }).frame(maxHeight: .infinity)
                        .padding(.trailing, 3)
                        .padding(.horizontal, 4)
                    
                }
                
                ForEach(fonts, id: \.self.path) { item in
                    
                    let isAvailable = licenseManagerWrapper.hasPremium || !item.forPremium
                    
                    Button(action: { onSelectedChanged(item) }, label: {
                        ZStack {
                            
                            let isSelected = item == selectedFont
                            
                            let font = fontObtainer.getFromPathToastOnError(path: item, style: .regular, onError: { it in
                                
                                it.printStackTrace()
                                
                            })!.withSize(CGFloat(dimens.fontTextSize))
                            
                            Text(" " + item.displayName) //space is needed so that font "Chalista" is not cut off+
                                .lineLimit(1)
                                .foregroundColor(isSelected ? colors.fontTextActive.toSColor() : colors.fontTextInactive.toSColor())
                                .font(Font(font))
                                .padding(.trailing, CGFloat(dimens.fontItemPaddingHorizontal))
                            
                            if !isAvailable {
                                SVGImage(svgName: "ic_fonts_lock", contentMode: .topRight)
                                    .offset(x: -4, y: CGFloat(dimens.fontLockOffsetY))
                            }
                        }
                    })
                }
                
                Spacer()
                    .frame(width: CGFloat(dimens.fontsListContentPadding))
            }
            
            
        }.frame(height: CGFloat(dimens.fontsListHeight))
            .frame(maxWidth: .infinity)
    }
}

struct FontsListView_Previews: PreviewProvider {
    static var previews: some View {
        let defaultFont = Dependencies.diContainer.resolve(PlatformFontPathProvider.self)!.defaultFont()
        let fontsManager = Dependencies.diContainer.resolve(FontsManager.self)!
        
        FontsListView(colors: FontDialogColorsDark(),
                      dimens: FontDialogDimensPhone(), fonts: fontsManager.allPredefinedFonts, isUpload: true, selectedFont: defaultFont, onSelectedChanged: { _ in })
            .padding()
            .background(FontDialogColorsDark().backgroundColor.toSColor())
    }
}
