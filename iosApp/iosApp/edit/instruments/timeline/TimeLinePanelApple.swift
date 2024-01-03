//
//  TimeLinePanelApple.swift
//  iosApp
//
//  Created by rst10h on 25.08.22.
//

import SwiftUI
import shared

struct TimeLinePanelApple: View {
    
    var model: TimeLineViewModelApple
    
    var body: some View {
        VStack {
            HStack {
                Spacer()
                TimePresets(model: model)
                    .padding(.leading, 20.cg)
                Spacer()
                TimeDurationLabel(model: model)
                    .font(.system(size: 10))
                    .foregroundColor(0xffdadada.ARGB)
                    .padding(3.cg)
                    .background(0xff3d3d3d.ARGB)
                    .cornerRadius(4)
                    .padding(.trailing, 20.cg)
                    
            }
            .padding(.top, 7)
           // .padding(.bottom, 6)
       TrimVideoSliderUI(model: model)
                .frame(height: 70.cg)
        }
        .frame(height: 120.cg)
    }
}

struct TimePresets: View {

    @ObservedObject
    var model: TimeLineViewModelApple
    
    var body: some View {
        HStack {
            ForEach(TimeLineViewModelApple.predefinedDurationsSec, id: \.self) { sec in
                Spacer()
                    .frame(width: 30.cg)
                Text("\(sec)s")
                    .font(.system(size: 15))
                    .fontWeight(.bold)
                    .foregroundColor(Color.white)
                    .padding(5.cg)
                    .background(0xff555555.ARGB)
                    .cornerRadius(10)
                    .onTapGesture {
                        let new = Float(sec) / Float(TimeLineViewModelApple.templateMaxDurationSec)
                        model.onTrimRight(newValue: new)
                    }
            }
        }
    }
}

struct TimeDurationLabel: View {
    
    @ObservedObject
    var model: TimeLineViewModelApple
    
    var body: some View {
        Text(TrackUtils.init().convertTimeToString(durationMs: model.templateDurationMs))
    }
    
    
}
