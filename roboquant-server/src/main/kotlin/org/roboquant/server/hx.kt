/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.roboquant.server

import kotlinx.html.FORM
import kotlinx.html.HTMLTag

/*
fun HTMLTag.hxGet(value: String) {
    attributes += "data-hx-get" to value
}

fun HTMLTag.hxSwap(value: String) {
    attributes += "data-hx-swap" to value
}

fun HTMLTag.hxTarget(value: String) {
    attributes += "data-hx-target" to value
}

fun FORM.hxPost(value: String) {
    attributes += "data-hx-post" to value
}

fun HTMLTag.hxBoost(value: Boolean) {
    attributes += "data-hx-boost" to value.toString()
}

fun HTMLTag.hxExt(value: String) {
    attributes += "data-hx-ext" to value
}
*/

var FORM.hxPost: String
    get() = attributes["data-hx-post"] ?: ""
    set(value) {
        attributes["data-hx-post"] = value
    }

var HTMLTag.hxExt: String
    get() = attributes["data-hx-ext"] ?: ""
    set(value) {
        attributes["data-hx-ext"] = value
    }


var HTMLTag.hxGet: String
    get() = attributes["data-hx-get"] ?: ""
    set(value) {
        attributes["data-hx-get"] = value
    }


var HTMLTag.hxConfirm: String
    get() = attributes["data-hx-confirm"] ?: ""
    set(value) {
        attributes["data-hx-confirm"] = value
    }

var HTMLTag.hxTarget: String
    get() = attributes["data-hx-target"] ?: ""
    set(value) {
        attributes["data-hx-target"] = value
    }