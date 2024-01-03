#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source texture
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;// Source
uniform vec2 uPixelSize_0_;

// Mask texture atlas
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_2_;// Alpha mask
varying vec2 vTextureCoord_3_;// Overlay mask

vec4 clampToBorder(vec2 textureCoord);


void main() {
    vec4 source = clampToBorder(vTextureCoord_0_);
    vec4 overlayMask = texture2D(sTexture_1_, vTextureCoord_3_);
    float alpha = texture2D(sTexture_1_, vTextureCoord_2_).r;

    gl_FragColor = mix(source, overlayMask, _alpha_);
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}