//
//  PreviewUtils.swift
//  iosApp
//
//  Created by rst10h on 11.02.22.
//

import Foundation
import shared

extension EditViewModelApple {
    static func modelForPreviews() -> EditViewModelApple {
        let res = MR.assetsTemplatesGrid().Grid3x1Template
        let path = PredefinedTemplatePath(res: res)
        return EditViewModelApple(templatePath: path, initialOriginalData: nil)
    }
}
