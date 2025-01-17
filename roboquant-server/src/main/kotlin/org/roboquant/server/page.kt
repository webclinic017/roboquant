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

import kotlinx.html.*

fun HTML.page(title: String, bodyFn: BODY.() -> Unit) {
    lang = "en"
    head {
        meta { charset = "utf-8" }
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1, shrink-to-fit=no"
        }
        script { src = "https://cdnjs.cloudflare.com/ajax/libs/htmx/1.9.5/htmx.min.js" }
        script { src = "https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js"}
        script { src = "/static/htmx-extensions.js"}
        title { +title }
        link {
            href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
            rel = "stylesheet"
        }
        link {
            href = "/static/custom.css"
            rel = "stylesheet"
        }
    }
    body(classes = "container") {
        h1 { +title }
        bodyFn()
    }

}