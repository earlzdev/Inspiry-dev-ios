//
//  ABTemplateAvailabilityEmpty.swift
//  iosApp
//
//  Created by vlad on 2/11/21.
//

import Foundation
import shared

class ABTemplateAvailabilityEmpty: ABTemplateAvailability {
    
    static let sharedInstance = ABTemplateAvailabilityEmpty()
    
    func getTemplateAvailability(
        current: TemplateAvailability,
        originalTemplatePath: AssetResource
    ) -> TemplateAvailability {
        return current
    }
}
