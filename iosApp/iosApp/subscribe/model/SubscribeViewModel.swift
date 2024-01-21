//
//  SubscribeViewModel.swift
//  iosApp
//
//  Created by rst10h on 7.12.22.
//

import Foundation
import shared
import Adapty

class SubscribeViewModel: BaseSubscribeViewModel, ObservableObject {
    
    @Published
    var uiState: InspResponse<SubscribeUiState>!
    
    @Published
    var subscribeProcess: Bool = false
    
    
    init(source: String) {

        let licenseManager: LicenseManager = Dependencies.resolveAuto()
        let analyticsManager: AnalyticsManager = Dependencies.resolveAuto()
        let remoteConfig: InspRemoteConfig = Dependencies.resolveAuto()
        print("init subscribe model")
        super.init(remoteConfig: remoteConfig, analyticsManager: analyticsManager, licenseManager: licenseManager)
        self.source = source
        self.uiState = (super.stateFlow.value as! InspResponse<SubscribeUiState>)
        analyticsManager.onSubscribeScreenOpen(source: source)
        CoroutineUtil.watch(state: super.stateFlow) { [weak self] in
            self?.uiState = $0
        }
        
        loadProducts(paywallId: "onboarding_placement_id")
    }
    
    override func loadProducts(paywallId: String) {
//        //DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
//            self.onProductsLoaded(displayProducts: self.debugProductList)
//        //}
        
        Adapty.getPaywall(paywallId, locale: "en") { result in
                    switch result {
                    case let .success(paywall):
                        Adapty.logShowPaywall(paywall)
                        Adapty.getPaywallProducts(paywall: paywall) { result in
                            switch result {
                            case let .success(products):
                                print("Adapty products loaded \(products.count)")
                                if (products.count != 0) {
                                    self.onPaywallLoadedLocal(products: products)
                                } else {
                                    self.onProductsLoaded(displayProducts: self.debugProductList)
                                }
                            case let .failure(error):
                                print("Adapty error get products: \(error.localizedDescription)")
                            }
                        }
                    case let .failure(error):
                        print("adapty error \(error.localizedDescription) for paywallId \(paywallId)")
                    }
                }
                
//        Adapty.getPaywall(paywallId) { result in
//
//            switch result {
//            case .failure(let error):
//                print("adapty error \(error.localizedDescription) for paywallId \(paywallId)")
//            case .success(let result):
//                Adapty.logShowPaywall(result)
//                print("aptempt to load paywall \(paywallId) \(result)")
//                Adapty.getPaywallProducts(paywall: result, fetchPolicy: .default) { productsResult in
//                    switch productsResult {
//                    case .failure(let error):
//                        print("Adapty error get products: \(error.localizedDescription)")
//                    case .success(let products):
//                        print("Adapty products loaded \(products.count)")
//                        if (products.count != 0) {
//                            self.onPaywallLoadedLocal(products: products)
//                        } else {
//                            self.onProductsLoaded(displayProducts: self.debugProductList)
//                        }
//                    }
//
//                }
//            }
//        }
    }
    func onSubscribeTap(onFinished: @escaping (Bool) -> Void) {
        DispatchQueue.main.async {
            self.onSubscribeClick(handler: OnSubscribeHandler()) { value in
                onFinished(value.boolValue)
            }
        }
    }
    private func onPaywallLoadedLocal(products: [AdaptyPaywallProduct]) {
        let productList: [DisplayProduct] = products.map( { product in
           //todo get free days for year period from introductory

            DisplayProduct(
                localizedPrice: product.localizedPrice ?? "",
                price: product.price.doubleValue,
                trialDays: product.subscriptionPeriod?.unit == .year ? 3 : 0, //todo get free days?
                id: product.vendorProductId,
                period: product.subscriptionPeriod?.toInspPeriodModel() ?? .lifetime,
                underlyingModel: product)
        }
        )
        onProductsLoaded(displayProducts: productList)
    }
    
    
    let debugProductList = [
        DisplayProduct(localizedPrice: "$5,99", price: Decimal(5.99).doubleValue, trialDays: 0, id: "test id1", period: .month, underlyingModel: -1),
        DisplayProduct(localizedPrice: "$24,99", price: 24.99, trialDays: 3, id: "test id1", period: .year, underlyingModel: -1),
    ]
}

class OnSubscribeHandler: OnClickSubscribeHandler {
    func makePurchase(product: DisplayProduct, onResult: @escaping (KotlinBoolean, String?) -> Void) {
        if let adaptyProduct = product.underlyingModel as? AdaptyPaywallProduct {
            Adapty.makePurchase(product: adaptyProduct) { result in
                switch result {
                case .success(_):
                    onResult(KotlinBoolean(bool: true), product.id)
                case .failure(_):
                    onResult(KotlinBoolean(bool: false), nil)
                }
            }
        } else {
            onResult(KotlinBoolean(bool: false), "purchases list is empty")
        }
    }
}



extension AdaptyProductSubscriptionPeriod {
    func toInspPeriodModel() -> DisplayProductPeriod? {
        switch self.unit {
        case .month:
            return .month
        case .day:
            return .day
        case .year:
            return .year
        case .week:
            return .week
        default:
            return nil
        }
    }
}

extension Decimal {
    var doubleValue:Double {
        return NSDecimalNumber(decimal:self).doubleValue
    }
}
