// @author Geert van Ieperen

#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 vertexNormal;

struct PointLight
{
    vec3 colour;
    // light position in view coordinates.
    vec3 position;
    float intensity;
};

// normal of the vertex
out vec3 mvVertexNormal;
// position of the vertex
out vec3 mvVertexPosition;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform PointLight pointLights[MAX_POINT_LIGHTS];


void main() {

	vec4 mvPosition = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPosition;

    // set light position
    for (int i=0; i < MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0 ) {
            pointLights.position = (modelViewMatrix * vec4(pointLights.position, 0.0)).xyz;
        }
    }

	mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    mvVertexPosition = mvPosition.xyz;
}