// @author Geert van Ieperen

#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 vertexNormal;

// normal of the vertex
out vec3 mVertexNormal;
// position of the vertex
out vec3 mVertexPosition;
out vec3 cameraPosition;

uniform mat4 modelMatrix;
uniform mat4 viewProjectionMatrix;


void main() {

	vec4 mPosition = modelMatrix * vec4(position, 1.0);
    gl_Position = viewProjectionMatrix * mPosition;
    cameraPosition = normalize(-mPosition.xyz); //position of camera in View space

	mVertexNormal = normalize(modelMatrix * vec4(vertexNormal, 0.0)).xyz;
    mVertexPosition = mPosition.xyz;
}