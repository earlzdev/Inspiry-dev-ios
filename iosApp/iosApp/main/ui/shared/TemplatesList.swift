//
//  TemplatesList.swift
//  iosApp
//
//  Created by rst10h on 4.01.22.
//

import SwiftUI
import shared
import Toaster
import Combine

struct TemplatesList: View {
    
    @Binding var visibleCategories: Set<String>
    @EnvironmentObject var viewModel: MainScreenViewModelApple
    
    var body: some View {
        let helper = viewModel.helper
        LazyVGrid(columns: [
            GridItem(spacing: 25, alignment: .top),
            GridItem(spacing: 25, alignment: .top),
        ]) {
            ForEach(viewModel.mainTemplates, id: \.self.id) { category in
                Section(header: CategoryHeader (category: category) ) {
                    let paths = category.templatePaths
                    ForEach(paths, id: \.self) { res in
                        TemplateItem(path: res)
                    }
                }
                .onAppear {
                    visibleCategories.insert(category.id)
                }
                .onDisappear {
                    visibleCategories.remove(category.id)
                }
            }
        }
    }
}

struct TemplateItem: View {
    let path: AssetResource
    @EnvironmentObject
    var viewModel: MainScreenViewModelApple
    @EnvironmentObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    var body: some View {
        let helper = viewModel.helper
        let path = PredefinedTemplatePath(res: path)
        let templateModel = viewModel.getCachedTemplateModel(templatePath: path)
        let template: Template? =  templateModel.template_
        let templateName: String? = helper.getTemplateName(path: path)
        VStack {
            NavigationLink(
                destination: EditView(templatePath: path, originalData: template?.originalData, onNavigationBack: { _, _ in
                    viewModel.fullScreenSelection = nil
                })
                .navigationBarHidden(true), tag: path.path, selection: $viewModel.fullScreenSelection) {
                    
                    
                    let aspect = CGFloat(template?.format.aspectRatio() ?? (TemplateFormat.story.aspectRatio()))
                    
                    TemplateUIView(templateModel: templateModel, autoplay: true, playSound: false)
                        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                        .aspectRatio(aspect, contentMode: .fit)
                        .cornerRadius(6)
                        .shadow(color: Color.black.opacity(0.2), radius: 5, x: 0, y: 0)
                        .overlay(ZStack {
                            if (template != nil) {
                                if (template!.availability != TemplateAvailability.free && !licenseManagerWrapper.hasPremium) {
                                    Image("ic_premium_template")
                                        .resizable()
                                        .scaledToFit()
                                        .frame(height: 27)
                                        .padding(5)
                                }
                            }
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

struct CategoryHeader: View {
    
    let category: TemplateCategory
    
    var body: some View {
        HStack {
            Text(category.displayName.localized())
                .font(.system(size: CGFloat(16)))
                .fontWeight(.medium)
            //.visibility(viewModel.isMyStories ? .Gone : .Visible)
            Spacer()
                .frame(height: 16)
        }
    }
}

struct TemplatesList_Previews: PreviewProvider {
    @State static var visibleCategories: Set<String> = []
    static var previews: some View {
        TemplatesList(visibleCategories: $visibleCategories)
            .environmentObject(MainScreenViewModelApple.Create())
    }
}
