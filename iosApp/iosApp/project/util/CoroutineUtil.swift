//
//  CoroutineUtil.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import Foundation
import shared

class CoroutineUtil {
    static func watch<T>(state: Kotlinx_coroutines_coreStateFlow, allowNilState: Bool = true, onValueReceived: @escaping (T) -> ()) {
        
        let flow = CommonFlowKt.asCommonFlow(state)
        
        flow.watch { (it: AnyObject?) in
            
            if (it != nil || allowNilState) {
                let obj = it as! T
                onValueReceived(obj)
            }
        }
    }
    static func onReceived(state: Kotlinx_coroutines_coreStateFlow, onValueReceived: @escaping () -> ()) {
        
        let flow = CommonFlowKt.asCommonFlow(state)
        
        flow.watch { (it: AnyObject?) in
            
                onValueReceived()
            }
        }
}
