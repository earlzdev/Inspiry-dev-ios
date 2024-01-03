#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source texture
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;// Source
uniform vec2 uBlurSize_0_;

// Mask texture atlas
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_1_;// Template mask

vec4 clampToBorder(vec2 textureCoord);
vec4 blur9(vec2 textureCoord, vec2 direction, vec2 uBlurSize);

void main() {
    vec4 templateMask = texture2D(sTexture_1_, vTextureCoord_1_);
    vec4 source = clampToBorder(vTextureCoord_0_);

    float mask = 1.0;

    if (templateMask.r == 1.0)
    mask = 0.0;

    vec2 scaledSourceCoord = vTextureCoord_0_ * vec2(0.9, 0.9) + vec2(0.05, 0.05);
    vec4 scaledSource = texture2D(sTexture_0_, scaledSourceCoord);

    vec4 blurredSource = blur9(vTextureCoord_0_, vec2(0.0, 1.0), uBlurSize_0_);
    vec4 result = mix(source, blurredSource, mask);

    gl_FragColor = result;
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}

vec4 blur9(vec2 textureCoord, vec2 direction, vec2 uBlurSize) {
    vec4 color = vec4(0);
    vec2 offset1 = vec2(1.3846153846) * direction;
    vec2 offset2 = vec2(3.2307692308) * direction;
    vec2 tc = vTextureCoord0;
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture_0_, tc) * 0.2270270270;
    } else {
        return vec4(1.0, 1.0, 1.0, 1.0);
    }
    tc = textureCoord + (offset1 * direction * uBlurSize);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture_0_, tc) * 0.3162162162;
    }
    tc = textureCoord - (offset1 * direction * uBlurSize);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture_0_, tc) * 0.3162162162;
    }
    tc = textureCoord + (offset2 * direction * uBlurSize);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture_0_, tc) * 0.0702702703;
    }
    tc = textureCoord - (offset2 * direction * uBlurSize);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture_0_, tc) * 0.0702702703;
    }
    return color;
}