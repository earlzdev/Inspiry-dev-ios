//
//  iosAppTests.swift
//  iosAppTests
//
//  Created by vlad on 16/9/21.
//

import XCTest
@testable import FirebaseAnalytics
@testable import iosApp

class iosAppTests: XCTestCase {

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testTexts() throws {
        TemplateValidator.validateTexts()
    }
    
    func testStickers() throws {
        TemplateValidator.validateStickers()
    }

    func testTemplates() throws {
        
        measure {
            TemplateValidator.validateTemplates()
        }
    }

}
