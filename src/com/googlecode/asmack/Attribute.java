/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.googlecode.asmack;

/**
 * A XML tag attribute, a pair of {name, namespace, value}.
 */
public class Attribute {

    /**
     * The name of the attribute.
     */
    private final String name;

    /**
     * The namespace of the attribute.
     */
    private final String namespace;

    /**
     * The value of the attribute. 
     */
    private final String value;

    /**
     * Create a new attribute instance by name, namespace, value.
     * The null namespace will be transformed into the empty namespace.
     * @param name The attribute name.
     * @param namespace The attribute namespace.
     * @param value The attribute value.
     */
    public Attribute(String name, String namespace, String value) {
        this.name = name;
        this.namespace = namespace == null ? "" : namespace;
        this.value = value;
    }

    /**
     * Retrieve the attribute name.
     * @return The attribute name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the attribute namespace.
     * @return The attribute namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Retrieve the attribute value.
     * @return The attribute value.
     */
    public String getValue() {
        return value;
    }

}
