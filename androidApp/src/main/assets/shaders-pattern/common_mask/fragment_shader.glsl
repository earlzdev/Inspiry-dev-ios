#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;

// Mask
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_1_;

// Overlay
uniform samplerExternalOES sTexture_2_;
varying vec2 vTextureCoord_2_;

vec4 clampToBorder(vec2 textureCoord);

void main() {
    vec4 source = clampToBorder(vTextureCoord_0_);
    vec4 templateMask = texture2D(sTexture_1_, vTextureCoord_1_);
    vec4 overlay = texture2D(sTexture_2_, vTextureCoord_2_);

    float brightness = (0.299 * templateMask.r + 0.587 * templateMask.g + 0.114 * templateMask.b);
    gl_FragColor = mix(overlay, source, brightness);
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}