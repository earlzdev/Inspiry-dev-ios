//
//  TemplatesList.swift
//  iosApp
//
//  Created by rst10h on 18.11.22.
//

import SwiftUI
import shared
import Toaster
import Combine

struct MyStoriesList: View {
    
    @EnvironmentObject var viewModel: MainScreenViewModelApple
    
    @ObservedObject
    var storiesModel: MyTemplatesViewModelApple
    
    var body: some View {
        LazyVGrid(columns: [
            GridItem(spacing: 25, alignment: .top),
            GridItem(spacing: 25, alignment: .top),
        ]) {
            ForEach(storiesModel.loadedTemplates, id: \.self) { path in
                MyStoriesItem(path: path)
                    .environmentObject(storiesModel)
            }
        }.padding(.top, 20)
    }
}

struct MyStoriesItem: View {
    let path: TemplatePath
    @EnvironmentObject
    var storiesModel: MyTemplatesViewModelApple
    @EnvironmentObject
    var viewModel: MainScreenViewModelApple
    
    var body: some View {
        let helper = storiesModel.helper
        if let templateModel = storiesModel.templatesCache[path] {
            let template =  templateModel.template_
            let templateName: String? = helper.getTemplateName(path: path)
            VStack {
                NavigationLink(
                    destination: EditView(templatePath: path, originalData: template.originalData, onNavigationBack: { path, saved in
                        if (saved) {
                            storiesModel.reloadTemplate(path: path)
                        }
                        viewModel.fullScreenSelection = nil
                    })
                    .navigationBarHidden(true), tag: path.path, selection: $viewModel.fullScreenSelection) {
                        
                        
                        let aspect = template.format.aspectRatio().cg
                        
                        TemplateUIView(templateModel: templateModel, autoplay: false, playSound: false)
                            .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                            .aspectRatio(aspect, contentMode: .fit)
                            .cornerRadius(6)
                            .shadow(color: Color.black.opacity(0.2), radius: 5, x: 0, y: 0)
                            .overlay(ZStack {
//                                if (template != nil && !viewModel.isMyStories) {
//                                    let pr = template!.availability == TemplateAvailability.premium
//                                    if (pr) {
//                                        Image("ic_premium_template")
//                                            .resizable()
//                                            .scaledToFit()
//                                            .frame(height: 27)
//                                            .padding(5)
//                                    }
//                                }
                                
                                MyTemplateActions {
                                    withAnimation {
                                        viewModel.openMyTemplateAction(path, name: template.name ?? "")
                                    }
                                }
                                .padding(.horizontal, 10)
                                .padding(.vertical, 15)
                            }, alignment: .topTrailing
                            )
                    }
                
                if (templateName != nil) {
                    Text(templateName!)
                        .font(.system(size: 12))
                        .lineLimit(1)
                }
                Spacer().frame(height: 20)
            }
        }
    }
}
