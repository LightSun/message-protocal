/*
 * Copyright 2019
 * heaven7(donshine723@gmail.com)

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.heaven7.java.meshy;

import java.util.List;

/**
 * @author heaven7
 */
public interface MessageProtocolContext {

    List<MemberProxy> getMemberProxies(Class<?> clazz);
    /**
     * get the type adapter for base types. like primitive types with its' wrapper and
     * string type.
     * @param Clazz the base types
     * @return the type adapter of 'Basic' type.
     */
    TypeAdapter getBaseTypeAdapter(Class<?> Clazz);

    /**
     * get the key adapter for target map class.
     * @param type the class
     * @return the type adapter
     * @since 1.0.1
     */
    TypeAdapter getKeyAdapter(Class<?> type);
    /**
     * get the value adapter for target map class.
     * @param type the class
     * @return the type adapter
     * @since 1.0.1
     */
    TypeAdapter getValueAdapter(Class<?> type);
}
