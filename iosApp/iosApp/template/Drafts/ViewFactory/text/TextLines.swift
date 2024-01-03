//
//  TextLines.swift
//  iosApp
//
//  Created by rst10h on 18.08.22.
//

import Foundation

class TextLines {
    
    var linesCharIndex: [Int] = [] {
        didSet {
            linesCount = linesCharIndex.count
        }
    }
    
    var linesCount: Int = 0 {
        didSet {
            maxLineIndex = linesCount - 1
        }
    }
    
    private var lastRequestedLine: Int = -1 {
        didSet {
            let next = lastRequestedLine + 1
            if (next > maxLineIndex) {
                nextRequestedLine = nil
            }
            else {
                nextRequestedLine = next
            }
        }
    }
    
    private var nextRequestedLine: Int? = nil
    private var maxLineIndex = 0
    
    /// get line number for char index
    /// no loops here because we get line index for characters sequentially
    
    func lineNumberForCharIndex(charIndex: Int) -> Int {
        if (charIndex == 0) {
            lastRequestedLine = 0
            return 0
        }
        guard let nextRequestedLine = nextRequestedLine else {
            return lastRequestedLine
        }
        
        if (charIndex >= linesCharIndex[lastRequestedLine]) {
            lastRequestedLine = nextRequestedLine
        }
        return lastRequestedLine
    }
}
