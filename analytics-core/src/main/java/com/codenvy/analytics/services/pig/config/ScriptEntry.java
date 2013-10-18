/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.services.pig.config;

import java.util.ArrayList;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ScriptEntry {

    private String name;

    private ArrayList<ScriptParameterEntry> parameters;

    /**
     * Empty constructor. 
     */
    public ScriptEntry() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the parameters
     */
    public ArrayList<ScriptParameterEntry> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(ArrayList<ScriptParameterEntry> parameters) {
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(50);
        sb.append("  {ScriptEntry : ").append(name).append(" : ");
        
        for (ScriptParameterEntry parameterEntry : parameters) {
            sb.append(parameterEntry.toString());
        }
        
        sb.append("}");
        
        return sb.toString();
    }
}
