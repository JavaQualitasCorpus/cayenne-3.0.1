/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.enhancer;

import org.apache.cayenne.CayenneRuntimeException;

/**
 * An exception thrown from within enhancer visitors to indicate that the class is already
 * enhanced and enhancement run should be stopped.
 * 
 * @since 3.0
 */
public class DoubleEnhanceException extends CayenneRuntimeException {

    public DoubleEnhanceException() {
    }

    public DoubleEnhanceException(String message) {
        super(message);
    }

    public DoubleEnhanceException(Throwable cause) {
        super(cause);
    }

    public DoubleEnhanceException(String message, Throwable cause) {
        super(message, cause);
    }

}
