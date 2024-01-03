//
//  ExportProgressView.swift
//  iosApp
//
//  Created by vlad on 8/11/21.
//

import SwiftUI
import shared

struct ExportProgressView: View {
    let progress: Float
    var colors: EditColors = EditColorsLight()
    var dimens: EditDimens = EditDimensPhone()

    var body: some View {
        
        VStack(spacing: CGFloat(dimens.exportBottomPanelProgressPaddingBetweenTextProgress)) {
            
            HStack(spacing: 10) {
                Text(MR.strings().saving_activity_progress_title.localized())
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(colors.exportProgressText.toSColor())
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                Text(ExportStateKt.progressFloatToString(progress: progress))
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(colors.exportProgressText.toSColor())
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            
            
            ZStack {
                GeometryReader { geometry in
                    
                    ZStack {
                        LinearGradient(colors: [colors.exportProgressStart.toSColor(), colors.exportProgressEnd.toSColor()], startPoint: .leading, endPoint: .trailing)
                        
                    }.frame(width: geometry.size.width * CGFloat(progress), height: 4)
                        .clipShape(RoundedCorner(radius: 4))
                    
                }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                    .padding(.top, 1)
                    .padding(.horizontal, 1)
            }
            .frame(maxWidth: .infinity)
                .frame(height: 6)
                .background(Color.white)
                .clipShape(RoundedCorner(radius: 4))
            
            
        }.padding(.horizontal, CGFloat(dimens.exportBottomPanelPaddingHorizontal))
            .padding(.top, CGFloat(dimens.exportBottomPanelProgressPaddingTop))        .frame(height: CGFloat(dimens.exportBottomPanelHeightProgress), alignment: .top)
        .exportBottomPanelShape(colors: colors, dimens: dimens)
    
        
    }
}

struct ExportProgressView_Previews: PreviewProvider {
    static var previews: some View {
        let colors = EditColorsLight()
        let dimens = EditDimensPhone()
        
        ZStack {
            ExportProgressView(progress: 0.5, colors: colors, dimens: dimens)
        }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: Alignment.bottom)
       
    }
}
