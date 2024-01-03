//
//  StringUtils.swift
//  MusicFeatureIos
//
//  Created by vlad on 8/4/21.
//

import Foundation
import shared
import UIKit

extension StringResource {
    func localized() -> String {
        return NSLocalizedString(resourceId, bundle: bundle, comment: "")
    }
    
    func localized(lang: String) -> String? {
        guard let localizedBundlePath = self.bundle.path(forResource: lang, ofType: "lproj") else {
            return nil
        }
        let localizedBundle = Bundle(path: localizedBundlePath)
        
        return localizedBundle?.localizedString(forKey: self.resourceId, value: nil, table: nil)
    }
}

extension String {
    
    func isUnknownArtist() -> Bool {
        return self == "unknown" || self.isEmpty
    }
    
    func replaceImageSizeItunes(size: Int) -> String {
        return self.replacingOccurrences(of: "{w}", with: String(size)).replacingOccurrences(of: "{h}", with: String(size))
    }
    
    func getFileName() -> String {
        return URL(string: self)?.deletingPathExtension().lastPathComponent ?? "unknown"
    }
    
    func removeSheme() -> String {
        return self.replacingOccurrences(of: #".+\/\/"#, with: "", options: [.regularExpression])
    }
    
    func getSheme() -> String {
        return self.replacingOccurrences(of: #"\/\/.+"#, with: "", options: [.regularExpression]).trim()
    }
    
    func trim(using characterSet: CharacterSet = .whitespacesAndNewlines) -> String {
        return trimmingCharacters(in: characterSet)
    }
    
    func labelSize(font: UIFont, size: CGSize) -> CGSize {
        let attributes = [NSAttributedString.Key.font: font]
        let option = NSStringDrawingOptions.usesLineFragmentOrigin
        
        let rect = self.boundingRect(with: size,
                                options: option,
                                attributes: attributes,
                                context: nil)
        return rect.size
    }
}
