vec3 refract(vec3 i, vec3 n, float eta)
{
    float cosi = dot(-i, n);
    float cost2 = 1.0 - eta * eta * (1.0 - cosi*cosi);
    vec3 t = eta*i + ((eta*cosi - sqrt(abs(cost2))) * n);
    return t * vec3(cost2 > 0.0);
}

//
// fresnel approximation
// F(a) = F(0) + (1- cos(a))^5 * (1- F(0))
//
// Calculate fresnel term. You can approximate it 
// with 1.0-dot(normal, viewpos).	
//
float fast_fresnel(vec3 I, vec3 N, vec3 fresnelValues)
{
    float bias = fresnelValues.x;
    float power = fresnelValues.y;
    float scale = 1.0 - bias;
    return bias + pow(1.0 - dot(I, N), power) * scale;
}

float very_fast_fresnel(vec3 I, vec3 N)
{ return 1.0 - dot(N, I); }

uniform vec3 fresnelValues;
uniform vec3 IoR_Values;
uniform samplerCube envMap;
uniform sampler2D glossMap;
uniform sampler2D baseMap;
varying vec3 normal;
varying vec3 E;

void main(void)
{
  //------ normalize incoming vectors
  //
  vec3 N = normalize(normal);
  vec3 I = normalize(E);
  
  //------ Find the reflection
  //
  vec3 reflVec = normalize(reflect(I, N));
  vec3 reflectColor = textureCube(envMap, reflVec).xyz;
    
  //------ Find the refraction
  //
  vec3 refractColor;
  refractColor.x = textureCube(envMap, refract(I, N, IoR_Values.x)).x;
  refractColor.y = textureCube(envMap, refract(I, N, IoR_Values.y)).y;
  refractColor.z = textureCube(envMap, refract(I, N, IoR_Values.z)).z;
  
  vec3 base_color = texture2D(baseMap, gl_TexCoord[0].st).rgb;
  
  //------ Do a gloss map look up and compute the reflectivity.
  //
  vec3 gloss_color = texture2D(glossMap, gl_TexCoord[0].st).rgb;
  float reflectivity = (gloss_color.r + gloss_color.g + gloss_color.b)/3.0;
    
  //------ Find the Fresnel term
  //
  float fresnelTerm = fast_fresnel(-I, N, fresnelValues);
  //float fresnelTerm = very_fast_fresnel(-I, N);
  
  //------ Write the final pixel.
  //
  vec3 color = mix(refractColor, reflectColor, fresnelTerm);
  gl_FragColor = vec4(mix(base_color, color, reflectivity), 1.0);
}