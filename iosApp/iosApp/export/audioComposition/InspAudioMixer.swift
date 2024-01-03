//
//  InspAudioMixer.swift
//  iosApp
//
//  Created by rst10h on 9.08.22.
//

import Foundation
import AVFoundation
import shared

extension OriginalAudioData {
    func mergeTo(videoURL: URL, action: @escaping (AVMutableComposition, AVMutableAudioMix) -> Void) {
        let mixComposition = AVMutableComposition()

        guard let videoCompositionTrack = mixComposition.addMutableTrack(withMediaType: .video, preferredTrackID: Int32(kCMPersistentTrackID_Invalid)) else { fatalError("video composition create failed") }
//        guard let audioFromVideoCompositionTrack = mixComposition.addMutableTrack(withMediaType: .audio, preferredTrackID: Int32(kCMPersistentTrackID_Invalid)) else { return }

        let videoAsset = AVURLAsset(url: videoURL)
        guard let videoTrack = videoAsset.tracks(withMediaType: .video).first else { fatalError("video composition adding failed") }
        
        do {
            try videoCompositionTrack.insertTimeRange(CMTimeRangeMake(start: .zero, duration: videoAsset.duration), of: videoTrack, at: .zero)
        } catch {
            print("video composition add failed")
        }

//        do {
//            try videoCompositionTrack.insertTimeRange(CMTimeRangeMake(start: .zero, duration: videoAsset.duration), of: videoTrack, at: .zero)
//            if let audioFromVideoTrack = videoAsset.tracks(withMediaType: .audio).first {
//                try audioFromVideoCompositionTrack.insertTimeRange(CMTimeRangeMake(start: CMTime.zero, duration: videoAsset.duration), of: audioFromVideoTrack, at: .zero)
//            }
//        } catch {
//        }
        
        var inputParameters = [AVAudioMixInputParameters]()
        
        for audioTrack in self.audioTracks {
            let url = URL(string: audioTrack.path)
            let avAsset = AVURLAsset(url: url!)
            
            let startTime = CMTime(seconds: audioTrack.viewStartPositionUs.double / 1_000_000, preferredTimescale: 1_000_000)
            let contentOffset = CMTime(seconds: audioTrack.contentOffsetUs.double / 1_000_000, preferredTimescale: 1_000_000)
            let contentDuration = CMTime(seconds: audioTrack.viewDurationUs.double / 1_000_000, preferredTimescale: 1_000_000)
            
            do {
                for audio in avAsset.tracks(withMediaType: .audio) {
                    guard let audioComposition = mixComposition.addMutableTrack(withMediaType: .audio, preferredTrackID: Int32(kCMPersistentTrackID_Invalid)) else {fatalError("audio composition create failed") }
                    try audioComposition.insertTimeRange(CMTimeRangeMake(start: contentOffset, duration: contentDuration), of: audio, at: startTime)
                    
                    let inputParameter = AVMutableAudioMixInputParameters(track: audioComposition)
                    inputParameter.setVolume(audioTrack.volume, at: .zero)
                    inputParameters.append(inputParameter)
                }
            } catch {
                fatalError("audio track adding failed")
            }
        }
        ///Adjust volume for tracks:
        let audioMix = AVMutableAudioMix()
        audioMix.inputParameters = inputParameters
        
        action(mixComposition, audioMix)
    }
}
