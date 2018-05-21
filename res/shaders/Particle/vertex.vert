#version 330

layout (location = 0) in vec3 relativePos;
layout (location = 1) in vec3 middle; // initial position of middle
layout (location = 2) in vec4 rotation; // (x, y, z, angle)
layout (location = 3) in vec3 movement;
layout (location = 4) in vec4 color;
layout (location = 5) in vec2 beginEndTime; // (beginTime, endTime)

uniform mat4 viewProjectionMatrix;
uniform float currentTime;

smooth out vec4 fragColor;

void main() {
    if (currentTime < beginEndTime.y) {
        // abbreviations
        vec3 rot = rotation.xyz;
        vec3 rel = relativePos;
        float t = currentTime - beginEndTime.x;

        // rotate the relative positions by angle
        float angle = rotation.w * t;
        float sin = sin(angle);
        float cos = cos(angle);
        float dot = dot(rel, rot.xyz);

        rel = vec3(
            rel.x * cos + sin * (rot.y * rel.z - rot.z * rel.y) + (1.0 - cos) * dot * rot.x,
            rel.y * cos + sin * (rot.z * rel.x - rot.x * rel.z) + (1.0 - cos) * dot * rot.y,
            rel.z * cos + sin * (rot.x * rel.y - rot.y * rel.x) + (1.0 - cos) * dot * rot.z
        );

        // calculate vertex position in world-space
        vec4 position = vec4(middle + rel + (movement * t), 1.0);
        gl_Position = viewProjectionMatrix * position;

    } else {
        // remove particle by making the surface 0
        gl_Position = vec4(middle, 1.0);
    }

    fragColor = color;
}