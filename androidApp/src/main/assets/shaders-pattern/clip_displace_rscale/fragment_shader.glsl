#extension GL_OES_EGL_image_external : require
precision mediump float;

const float DISPLACEMENT_PIXEL_STEP = 60.0;
const float RED_CHANNEL_SCALE = 0.95;

// Source texture
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;// Source
uniform vec2 uPixelSize_0_;

// Mask texture atlas
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_2_;// Overlay mask
varying vec2 vTextureCoord_3_;// Alpha mask
varying vec2 vTextureCoord_4_;// Displacement mask

vec2 displacement(vec2 textureCoord, vec2 pixelSize, vec2 offset, float maxPixelOffsetX, float maxPixelOffsetY);
vec4 clampToBorder(vec2 textureCoord);
vec2 scale(vec2 textureCoord, float scaleFactor);


void main() {
    vec2 displaceMask = texture2D(sTexture_1_, vTextureCoord_4_).xy;

    vec2 gbCoord = displacement(vTextureCoord_0_, uPixelSize_0_, displaceMask, DISPLACEMENT_PIXEL_STEP, DISPLACEMENT_PIXEL_STEP);
    vec2 rCoord = scale(gbCoord, RED_CHANNEL_SCALE);

    vec4 source = clampToBorder(gbCoord);
    source.r = clampToBorder(rCoord).r;

    vec4 overlayMask = texture2D(sTexture_1_, vTextureCoord_2_);
    float alpha = texture2D(sTexture_1_, vTextureCoord_3_).r;
    gl_FragColor = mix(source, overlayMask, alpha);
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}

vec2 scale(vec2 textureCoord, float scaleFactor) {

    vec2 newTextureCoord = ((textureCoord - 0.5) * scaleFactor) + 0.5;

    if (newTextureCoord.x > 1.0 || newTextureCoord.x < 0.0) {
        newTextureCoord.x = textureCoord.x;
    }
    if (newTextureCoord.y > 1.0 || newTextureCoord.y < 0.0) {
        newTextureCoord.y = textureCoord.y;
    }

    return newTextureCoord;
}

vec2 displacement(vec2 textureCoord, vec2 pixelSize, vec2 offset, float maxPixelOffsetX, float maxPixelOffsetY) {
    vec2 newTextureCoord = textureCoord + vec2(maxPixelOffsetX, maxPixelOffsetY) * offset * pixelSize;
    if (newTextureCoord.x > 1.0 || newTextureCoord.x < 0.0) {
        newTextureCoord.x = textureCoord.x;
    }
    if (newTextureCoord.y > 1.0 || newTextureCoord.y < 0.0) {
        newTextureCoord.y = textureCoord.y;
    }
    return newTextureCoord;
}
