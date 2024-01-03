//
// Created by vlad on 14/9/21.
//

import Foundation
import shared

class TemplateValidator {

    static func validateTemplates() {
        let templateReadWrite: TemplateReadWrite = Dependencies.resolveAuto()
        let templateCategoryProvider: TemplateCategoryProvider = Dependencies.resolveAuto()
        let externalResourceDao: ExternalResourceDao = Dependencies.resolveAuto()
        let categories = templateCategoryProvider.getTemplateCategories(isPremium: false)
        
        for category in categories {
            for templatePath in category.templatePaths {
                
                let template = templateReadWrite.loadTemplateFromPath(path: PredefinedTemplatePath(res: templatePath))
                template.originalData = OriginalTemplateData(originalCategory: "doesnt matter", originalIndexInCategory: 0, originalPath: templatePath.originalPath)
                
                let newPath = templateReadWrite.saveTemplateToFile(template: template, existingPath: nil, currentTime: Int64(Date().timeIntervalSinceReferenceDate))
                
                templateReadWrite.loadTemplateFromPath(path: newPath)
                templateReadWrite.deleteTemplateFiles(template: template, path: newPath, externalResourceDao: externalResourceDao)
            }
        }
    }
    
    static func validateTexts() {
        let textAnimProvider: TextAnimProvider = Dependencies.resolveAuto()
        let json: Json = Dependencies.resolveAuto()
        let mediaSerializer = MediaSerializer()
        
        for catName in textAnimProvider.getCategories() {
            
            let categoryMedias = textAnimProvider.getAnimations(category: catName)
            
            for media in categoryMedias {
                
                let str = json.encodeToString(serializer: mediaSerializer, value: media.media)
                json.decodeFromString(deserializer: mediaSerializer, string: str)
            }
        }
    }
    
    static func validateStickers() {
        let stickersProvider: StickersProvider = Dependencies.resolveAuto()
        let json: Json = Dependencies.resolveAuto()
        let mediaSerializer = MediaSerializer()
        
        for category in stickersProvider.getCategories() {
            
            let categoryContent = stickersProvider.getStickers(category: category)
            
            for mediaWithPath in categoryContent {
                
                let str = json.encodeToString(serializer: mediaSerializer, value: mediaWithPath.media)
                
                print(mediaWithPath.path)
                print(str)
                
                json.decodeFromString(deserializer: mediaSerializer, string: str)
            }
        }
    }
}
