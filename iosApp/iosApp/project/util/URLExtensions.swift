//
//  URLExtensions.swift
//  iosApp
//
//  Created by rst10h on 29.11.22.
//

import Foundation

extension URL {
    var isDirectory: Bool {
       (try? resourceValues(forKeys: [.isDirectoryKey]))?.isDirectory == true
    }
}
