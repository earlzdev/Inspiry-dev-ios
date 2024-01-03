//
//  FakeTemplateViewContent.swift
//  iosApp
//
//  Created by vlad on 5/11/21.
//

import SwiftUI
import AVKit
import shared

struct FakeTemplateContentView: View {
    
    
    var body: some View {
        VStack {
            
            VideoView(url: MR.assetsVideosPromo().remove_back.url)
               .frame(maxWidth: .infinity, maxHeight: .infinity)
            
            VideoView(url: MR.assetsVideosPromo().remove_back.url)
               .frame(maxWidth: .infinity, maxHeight: .infinity)
            
            
            VideoView(url: MR.assetsVideosPromo().remove_back.url)
               .frame(maxWidth: .infinity, maxHeight: .infinity)
            
            /**
             
             VideoView(url: MR.assetsVideos().subscribe.url)
                 .frame(maxWidth: .infinity, maxHeight: .infinity)
             
             VideoView(url: MR.assetsVideosPromo().remove_back.url)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            
            
            VideoView(url: MR.assetsVideosOnboarding().page_1.url)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
             */
        }
    }
}

struct FakeTemplateViewContent_Previews: PreviewProvider {
    static var previews: some View {
        FakeTemplateContentView()
    }
}

func getTestTemplate() -> Template {
    let templateReadWrite: TemplateReadWrite = Dependencies.resolveAuto()
    let res = MR.assetsTemplatesGrid().Grid3x1Template
    return templateReadWrite.loadTemplateFromPath(path: PredefinedTemplatePath(res: res))
}
