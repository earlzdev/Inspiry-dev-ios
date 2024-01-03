#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;
uniform vec2 uPixelSize_0_;

// Mask
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_1_;

vec4 clampToBorder(vec2 textureCoord);

void main() {
    vec4 source = clampToBorder(vTextureCoord_0_);
    vec4 mask = texture2D(sTexture_1_, vTextureCoord_1_);

    gl_FragColor = source + mask;
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}