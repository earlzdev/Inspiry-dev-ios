#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source 1
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;

// Source 2
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_1_;

// Background 1
uniform samplerExternalOES sTexture_2_;
varying vec2 vTextureCoord_2_;

// Background 2
uniform samplerExternalOES sTexture_3_;
varying vec2 vTextureCoord_3_;

// Mask 1
uniform samplerExternalOES sTexture_4_;
varying vec2 vTextureCoord_4_;

// Mask 2
uniform samplerExternalOES sTexture_5_;
varying vec2 vTextureCoord_5_;

vec4 clampToBorder0(vec2 textureCoord);
vec4 clampToBorder1(vec2 textureCoord);

void main() {
    vec4 templateMask0 = texture2D(sTexture_4_, vTextureCoord_4_);
    vec4 templateMask1 = texture2D(sTexture_5_, vTextureCoord_5_);

    vec4 source0 = clampToBorder0(vTextureCoord_0_);
    vec4 source1 = clampToBorder1(vTextureCoord_1_);

    vec4 bg0 = texture2D(sTexture_2_, vTextureCoord_2_);
    vec4 bg1 = texture2D(sTexture_3_, vTextureCoord_3_);

    float brightness0 = (0.299 * templateMask0.r + 0.587 * templateMask0.g + 0.114 * templateMask0.b);
    float brightness1 = (0.299 * templateMask1.r + 0.587 * templateMask1.g + 0.114 * templateMask1.b);

    vec4 mix0 = mix(bg0, source0, brightness0);
    vec4 mix1 = mix(bg1, source1, brightness1);

    gl_FragColor = mix(mix1, mix0, mix0.a);
}

vec4 clampToBorder0(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}
vec4 clampToBorder1(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_1_, textureCoord);
    }
}