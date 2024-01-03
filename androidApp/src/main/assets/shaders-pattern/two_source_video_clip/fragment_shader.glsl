#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source textures
uniform samplerExternalOES sTexture_0_;
uniform samplerExternalOES sTexture_5_;
varying vec2 vTextureCoord_0_;
varying vec2 vTextureCoord_5_;

// Mask texture atlas
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_2_;// Split mask
varying vec2 vTextureCoord_3_;// Overlay mask
varying vec2 vTextureCoord_4_;// Alpha mask

vec4 clampToBorder(vec2 textureCoord);

void main() {
    bool isFirstSource = texture2D(sTexture_1_, vTextureCoord_2_).r < 0.5;
    vec4 source = isFirstSource ? clampToBorder(vTextureCoord_0_) : texture2D(sTexture_5_, vTextureCoord_5_);
    vec4 overlayMask = texture2D(sTexture_1_, vTextureCoord_3_);
    float alpha = texture2D(sTexture_1_, vTextureCoord_4_).r;
    gl_FragColor = mix(source, overlayMask, alpha);
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}