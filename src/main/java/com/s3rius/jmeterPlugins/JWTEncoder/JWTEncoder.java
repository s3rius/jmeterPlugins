package com.s3rius.jmeterPlugins.JWTEncoder;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.log4j.*;
import org.apache.jmeter.engine.util.CompoundVariable;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.google.gson.*;

/**
 * JWT encoder class.
 * 
 * It generates JWT variable and puts it
 * into the context.
 *
 */
public class JWTEncoder extends ConfigTestElement implements LoopIterationListener {

    private static final Logger log = Logger.getLogger(JWTEncoder.class);

    public static final String VARIABLE_NAME_PROPERTY = "variableName";
    public static final String CLAIMS_PROPERTY = "claims";
    public static final String JWT_KEY_PROPERTY = "jwtKey";

    /***
     * Set variable name.
     * 
     * @param variableName JMETER variable name.
     */
    public void setVariableName(String variableName) {
        setProperty(VARIABLE_NAME_PROPERTY, variableName);
    }

    /***
     * Get variable name to use when storing JWT token.
     * 
     * @return variable name.
     */
    public String getVariableName() {
        return getPropertyAsString(VARIABLE_NAME_PROPERTY);
    }

    /***
     * Set value of a JWT key.
     * 
     * @param jwtKey actual key.
     */
    public void setJwtKey(String jwtKey) {
        setProperty(JWT_KEY_PROPERTY, jwtKey);
    }

    /***
     * Get value of JWT key.
     * 
     * @return get jwt key.
     */
    public String getJwtKey() {
        return getPropertyAsString(JWT_KEY_PROPERTY);
    }

    /***
     * Sets and prettyfies jwtClaims.
     * 
     * if the claims are not valid it will
     * save it as is.
     * 
     * @param jwtClaims
     */
    public void setJwtClaims(String jwtClaims) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonElement element = JsonParser.parseString(jwtClaims);
            setProperty(CLAIMS_PROPERTY, gson.toJson(element));
        } catch (Exception e) {
            setProperty(CLAIMS_PROPERTY, jwtClaims);
        }
    }

    /***
     * Chechs if current claims are valid JSON object.
     * 
     * @return true or false.
     */
    public Boolean isValidClaims() {
        try {
            JsonParser.parseString(getJwtClaims()).getAsJsonObject();
            return true;
        } catch (JsonSyntaxException | IllegalStateException e) {
            log.error("Claims are invalid.");
            return false;
        }
    }

    public String getJwtClaims() {
        return getPropertyAsString(CLAIMS_PROPERTY);
    }

    /***
     * Parse claims as a JSON Object.
     * 
     * claims must be a valid JSON object
     * so we can easily convert it to HashMap<String, Object>.
     * 
     * @return HashMap<String, Object>
     */
    private Map<String, Object> getClaimsMap() {
        Map<String, Object> claims = new HashMap<>();
        CompoundVariable variable = new CompoundVariable();
        try {
            variable.setParameters(getJwtClaims());
        } catch (Exception e) {
            log.error("Can't parse variables in claims.");
            return claims;
        }
        JsonObject parsedJson = JsonParser.parseString(variable.execute()).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : parsedJson.entrySet()) {
            claims.put(entry.getKey(), Utils.jsonToObject(entry.getValue()));
        }
        return claims;
    }

    /***
     * Actually generates JSON Web Token with
     * given parameters.
     * 
     * @return token string.
     */
    public String generateJwt() {
        Algorithm algo = Algorithm.HMAC256(getJwtKey());
        return JWT.create().withPayload(getClaimsMap()).sign(algo);
    }

    /***
     * Called when load testing iteration
     * is about to start.
     */
    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        JMeterContextService.getContext().getVariables().put(getVariableName(), this.generateJwt());
    }
}
