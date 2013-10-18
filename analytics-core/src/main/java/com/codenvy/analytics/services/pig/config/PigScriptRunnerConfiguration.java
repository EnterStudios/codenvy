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
public class PigScriptRunnerConfiguration {

    private ArrayList<ScriptEntry> scripts;

    /** Empty constructor. */
    public PigScriptRunnerConfiguration() {
    }

    /** @return the scripts */
    public ArrayList<ScriptEntry> getScripts() {
        return scripts;
    }

    /**
     * @param scripts
     *         the scripts to set
     */
    public void setScripts(ArrayList<ScriptEntry> scripts) {
        this.scripts = scripts;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(500);
        sb.append("{PigScriptRunnerConfiguration : ").append("\n");

        for (ScriptEntry executionEntry : scripts) {
            sb.append(executionEntry.toString()).append("\n");
        }

        sb.append("}");

        return sb.toString();
    }
}
