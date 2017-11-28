#version 330


layout (location = 0) in vec3 position;
layout (location = 1) in vec3 vertexNormal;

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

struct PointLight
{
    vec3 color;
    // light position in view coördinates.
    vec3 position;
    float intensity;
};

const int MAX_POINT_LIGHTS = 10;

uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform vec3 ambientLight;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

smooth out vec4 fragColor;

vec3 calculateLighting(vec3 P, vec3 N, vec3 eye, PointLight light){

    vec3 result = vec3(0.0, 0.0, 0.0);

	vec3 lightDirection = normalize(light.position.xyz - P); //vector towards light source
    // diffuse component
    float intensity = max(0.0, dot(N, lightDirection));
    result += intensity * light.color.xyz * material.diffuse.xyz;

	vec3 reflection = (reflect(lightDirection, N));
	vec3 virtualLightPosition = normalize(-reflection);

	// specular component
    float shine = pow( max(0.0, dot(virtualLightPosition, eye)), material.reflectance);
    //float shine = pow( max(0.0, dot(N, HalfAngle) ), mat.shininess );
    result += shine * light.color.xyz;


	return result;
}

void main()
{
    vec4 mvPosition4 = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPosition4;

    vec3 mvPosition = mvPosition4.xyz;
    vec3 mvNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    vec3 cameraPosition = normalize(-mvPosition); //position of camera in View space

    vec3 diffuseSpecularComponent = vec3(0.0, 0.0, 0.0);
    for (int i=0; i < MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0 ) {
            diffuseSpecularComponent += calculateLighting(mvPosition, mvNormal, cameraPosition, pointLights[i]);
        }
    }

    fragColor = material.ambient * vec4(ambientLight, 1.0) + vec4(diffuseSpecularComponent, 0.0);
}