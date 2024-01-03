//
//  AlbumsListView.swift
//  MusicFeatureIos
//
//  Created by vlad on 7/4/21.
//

import SwiftUI
import shared
import Kingfisher

struct AlbumsListView: View {
    let colors: MusicColors
    let albums: [Album]
    
    let selectedAlbumId: Int64
    let onSelectedAlbumIdChange: (Int64) -> ()
    
    func AlbumImage(_ album: Album, _ isSelected: Bool) -> some View {
        return ZStack {
            
            GeometryReader { geo in
                let requiredImageSize = Int(geo.size.width)
                
                ZStack {
                    
                    if (album.image != nil) {
                        
                        let imageUrl = album.image!.replaceImageSizeItunes(size: requiredImageSize)
                        
                        KFImage(URL(string: imageUrl))
                            .placeholder {
                                
                                SVGImage(svgName: "ic_placeholder_album")
                                    .cornerRadius(10)
                                
                            }
                            .downsampling(size: CGSize(width: requiredImageSize, height: requiredImageSize))
                            .cancelOnDisappear(true)
                            .resizable()
                                .cornerRadius(10)
                                .frame(width: 70, height: 65)
                        
                            
                    } else {
                        SVGImage(svgName: "ic_placeholder_album")
                            .cornerRadius(10)
                    }
                    
                    
                }.frame(width: 70, height: 65)
                
            }
            .frame(width: 70, height: 65)
            
            
        }.frame(width: 76, height: 71, alignment: .center)
        .background(isSelected ? colors.albumBorderActive.toSColor() : colors.albumBorderInactive.toSColor())
        .cornerRadius(12)
    }
    
    var body: some View {
        
        ScrollView(.horizontal) {
            LazyHStack(alignment: .center, spacing: 6) {
                
                ForEach(Array(albums.enumerated()), id: \.element) { index, album in
                    
                    let isSelected = album.id == selectedAlbumId
                    
                    let addPaddingLeading: CGFloat = index == 0 ? 11 : 0
                    let addPaddingTrailing: CGFloat = index == albums.count - 0 ? 11 : 0
                    
                    
                    Button(action: {
                        onSelectedAlbumIdChange(album.id)
                    }, label: {
                        
                        VStack(alignment: .leading, spacing: 0) {
                            
                            AlbumImage(album, isSelected)
                            
                            Text(album.name)
                                .lineLimit(1)
                                .font(.system(size: 12, weight: isSelected ? .medium : .regular))
                                .foregroundColor(isSelected ? colors.albumTextActive.toSColor() : colors.albumTextInactive.toSColor())
                                .padding(.leading, 6)
                                .padding(.trailing, 6)
                                .padding(.top, 5)
                            
                            
                        }.padding(.leading, 4)
                        .padding(.trailing, 4)
                        .frame(width: 87)
                        .padding(.leading, addPaddingLeading)
                        .padding(.trailing, addPaddingTrailing)
                    })
                    
                }
            }
        }
        
    }
}


struct AlbumsListView_Previews: PreviewProvider {
    static var previews: some View {
        AlbumsListView(colors: MusicDarkColors(), albums: getTestAlbums(),
                       selectedAlbumId: -1) { (Int64) in
        }
    }
}
