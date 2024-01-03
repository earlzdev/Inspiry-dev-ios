//
//  RuntimeError.swift
//  iosApp
//
//  Created by vlad on 5/11/21.
//

import Foundation

enum InspError: Error {
  case IllegalArguments(String)
  case IllegalState(String)
  case RuntimeError(String)
  case Unknown
}
