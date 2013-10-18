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

IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

t = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

SS = extractEventsWithSessionId(t, 'session-started');
SF = extractEventsWithSessionId(t, 'session-finished');

j1 = JOIN SS BY sId FULL, SF BY sId;
j2 = FILTER j1 BY SS::sId IS NOT NULL AND SF::sId IS NOT NULL;
j3 = FOREACH j2 GENERATE SS::ws AS ws, SS::user AS user, SS::dt AS ssDt, SF::dt AS sfDt;
A = FOREACH j3 GENERATE ws, user, ssDt AS dt, SecondsBetween(sfDt, ssDt) AS delta;

--
-- The rest of the sessions
--
k1 = FOREACH t GENERATE ws, user, dt;
k2 = JOIN k1 BY (ws, user) LEFT, j3 BY (ws, user);
k3 = FILTER k2 BY (j3::ws IS NULL) OR MilliSecondsBetween(j3::ssDt, k1::dt) > 0 OR MilliSecondsBetween(j3::sfDt, k1::dt) < 0;
k4 = FOREACH k3 GENERATE k1::ws AS ws, k1::user AS user, k1::dt AS dt;
B = productUsageTimeList(k4, '$inactiveInterval');

j1 = UNION A, B;
j2 = LOAD '$LOAD_DIR' USING PigStorage() AS (user : chararray, firstName: chararray, lastName: chararray, company: chararray, phone : chararray, job : chararray);
j3 = JOIN j1 BY user LEFT, j2 BY user;
j4 = FILTER j3 BY j2::company IS NOT NULL AND j2::company != '';

R1 = FOREACH j4 GENERATE j2::company AS company, j1::delta AS delta;
R2 = GROUP R1 BY company;

result = FOREACH R2 GENERATE group, TOBAG(SUM(R1.delta) / 60, COUNT(R1.delta));
