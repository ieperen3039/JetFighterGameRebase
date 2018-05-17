#version 330

layout (location = 0) in vec3 relativePos;
layout (location = 1) in vec3 middle;
layout (location = 2) in vec4 rotation; // (x, y, z, angle)
layout (location = 3) in vec3 movement;
layout (location = 4) in vec4 color;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 cameraPosition;
uniform float time;

smooth out vec4 fragColor;

void main()
{
    // abbreviations
    vec3 rot = rotation.xyz;
    vec3 rel = relativePos;

    // rotate the relative positions by angle
    float angle = rotation.w * time;
    float sin = sin(angle);
    float cos = cos(angle);
    float dot = dot(rel, rot.xyz);

    rel = vec3(
        rel.x * cos + sin * (rot.y * rel.z - rot.z * rel.y) + (1.0 - cos) * dot * rot.x,
        rel.y * cos + sin * (rot.z * rel.x - rot.x * rel.z) + (1.0 - cos) * dot * rot.y,
        rel.z * cos + sin * (rot.x * rel.y - rot.y * rel.x) + (1.0 - cos) * dot * rot.z
    );

    // calculate world-position and store
    vec4 position = vec4(rel + middle, 1.0);
    vec4 mvPosition4 = modelViewMatrix * position;
    gl_Position = projectionMatrix * mvPosition4;
    fragColor = color;
}