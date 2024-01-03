//
//  EmptyMyStories.swift
//  iosApp
//
//  Created by rst10h on 30.12.21.
//

import SwiftUI
import shared

struct EmptyMyStories: View {
    @EnvironmentObject var viewModel: MainScreenViewModelApple
    var body: some View {
        let colors = viewModel.coreModel.getMainScreenColors()
        let dimens = viewModel.coreModel.getMainScreenDimens()
        VStack {
            Text(MR.strings().empty_my_stories.localized())
                .font(.system(size: CGFloat(dimens.emptyStoriesText_)))
                .fontWeight(.medium)
                .foregroundColor(colors.emptyStoriesText.toSColor())
                .multilineTextAlignment(.center)
                .padding(.bottom, 7)
                .padding(.horizontal, 50)
            Button(action: {
                viewModel.selectNewPage(newPage: .templates)
            }, label: {
                Text(MR.strings().create_new_story.localized())
                    .font(.system(size: CGFloat(dimens.newStoryButtonText)))
                    .fontWeight(.bold)
                    .textCase(.uppercase)
                    .padding(.vertical, 10)
                    .padding(.horizontal, 15)
                    .foregroundColor(Color.white)
                    .background(colors.newStoryButtonBack.toSColor())
                    .clipShape(RoundedRectangle(cornerRadius: 7))
            })
        }
    }

}

struct EmptyMyStories_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            EmptyMyStories()
                .environmentObject(MainScreenViewModelApple.Create())
        }
    }
}
